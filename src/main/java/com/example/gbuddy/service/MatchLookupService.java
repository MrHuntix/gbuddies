package com.example.gbuddy.service;

import com.example.gbuddy.dao.*;
import com.example.gbuddy.exception.CustomException;
import com.example.gbuddy.models.constants.CommonConstants;
import com.example.gbuddy.models.constants.MatchLookupConstants;
import com.example.gbuddy.models.entities.Match;
import com.example.gbuddy.models.entities.MatchLookup;
import com.example.gbuddy.models.entities.MatchRequest;
import com.example.gbuddy.models.protos.MatchLookupProto;
import com.example.gbuddy.util.MapperUtil;
import com.example.gbuddy.models.constants.MatcherConst;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;

@Component
@Transactional
public class MatchLookupService {
    private static final Logger LOG = LoggerFactory.getLogger(MatchLookupService.class);

    @Autowired
    private MatchLookupDao matchLookupDao;

    @Autowired
    private MatchDao matchDao;

    @Autowired
    private MatchRequestDao matchRequestDao;

    @Autowired
    private MapperUtil mapperUtil;

    @Autowired
    private LikeProcessor likeProcessor;

    private BiPredicate<MatchLookup, MatchLookup> doesRequestExist = (requester, requestee) -> {
        Optional<MatchRequest> request = matchRequestDao.getMatchRequest(requester.getId(), requestee.getId(), requester.getRequesterId(), requestee.getRequesterId());
        return !request.isPresent();
    };

    public MatchLookupProto.MatchResponse addForLookup(int requesterId, int gymId, int branchId) {
        MatchLookupProto.MatchResponse.Builder builder = MatchLookupProto.MatchResponse.newBuilder();
        try {
            MatchLookup lookup = matchLookupDao.getRequestMatch(gymId, branchId, requesterId).orElse(null);
            if (lookup != null) {
                LOG.info("request already exists(id: {}, status: {})", lookup.getId(), lookup.getStatus());
                throw new CustomException(String.format(CommonConstants.LOOKUP_REQUEST_EXISTS.getMessage(), lookup.getId(), lookup.getStatus()));
            }
            MatchLookup match = new MatchLookup();
            match.setBranchId(branchId);
            match.setGymId(gymId);
            match.setRequesterId(requesterId);
            match.setStatus(MatcherConst.UNMATCHED.getName());
            matchLookupDao.save(match);
            LOG.info("created match(id: {}) for user {}, for gym {}", match.getId(), match.getRequesterId(), match.getGymId());
            builder.setMessage(CommonConstants.REQUEST_RAISED.getMessage())
                    .setResponseCode(HttpStatus.OK.value());
        } catch (Exception e) {
            builder.setMessage(e.getMessage())
                    .setResponseCode(HttpStatus.UNPROCESSABLE_ENTITY.value());
        }
        return builder.build();

    }

    public MatchLookupProto.MatchResponse like(int matchLookupId, int userId) {
        MatchLookupProto.MatchResponse.Builder builder = MatchLookupProto.MatchResponse.newBuilder();
        LOG.info("start of like process for match lookup id {} requested by user id {}", matchLookupId, userId);
        likeProcessor.submitLikeRequest(matchLookupId, userId);
        builder.setMessage(MatchLookupConstants.BUDDY_REQUEST_SENT.getMessage())
                .setResponseCode(HttpStatus.OK.value());
        return builder.build();
    }

    public MatchLookupProto.MatchResponse unmatch(int matchId) {
        MatchLookupProto.MatchResponse.Builder builder = MatchLookupProto.MatchResponse.newBuilder();
        LOG.info("start of unmatch process for match id {}", matchId);
        try {
            Match match = matchDao.findById(matchId).orElse(null);
            if (Objects.isNull(match)) {
                LOG.info("no record in MATCHES found for id: {}", matchId);
                throw new CustomException(CommonConstants.UNMATCH_FAIL.getMessage());
            }
            MatchLookup requester = matchLookupDao.getRequestMatch(match.getGymId(), match.getBranchId(), match.getRequester()).orElse(null);
            if (!Objects.isNull(requester) && !MatcherConst.MATCHED.getName().equalsIgnoreCase(requester.getStatus())) {
                LOG.info("Got requester. Status {}", requester.getStatus());
                throw new CustomException(CommonConstants.UNMATCH_FAIL.getMessage());
            }
            MatchLookup requestee = matchLookupDao.getRequestMatch(match.getGymId(), match.getBranchId(), match.getRequestee()).orElse(null);
            if (!Objects.isNull(requestee) && !MatcherConst.MATCHED.getName().equalsIgnoreCase(requestee.getStatus())) {
                LOG.info("Got requestee. Status {}", requester.getStatus());
                throw new CustomException(CommonConstants.UNMATCH_FAIL.getMessage());
            }
            if (MatcherConst.UNMATCHED.getName().equals(requester.getStatus()) || MatcherConst.UNMATCHED.getName().equals(requestee.getStatus())) {
                LOG.info("record found in MATCH_LOOKUP for (gymId: {}, branchId: {}, requesterId: {}) but status({}) invalid", match.getGymId(), match.getBranchId(), match.getRequester(), requester.getStatus());
                throw new CustomException(CommonConstants.UNMATCH_FAIL.getMessage());
            }
            requester.setStatus(MatcherConst.UNMATCHED.getName());
            requestee.setStatus(MatcherConst.UNMATCHED.getName());
            LOG.info("requester: {}, requestee: {}", requester, requestee);
            matchLookupDao.save(requester);
            matchLookupDao.save(requestee);
            matchDao.delete(match);
            LOG.info("start of unmatch process for match id {}", matchId);
            builder.setMessage(CommonConstants.UNMATCH_SUCCESS.getMessage())
                    .setResponseCode(HttpStatus.OK.value());
        } catch (Exception e) {
            builder.setMessage(e.getMessage())
                    .setResponseCode(HttpStatus.UNPROCESSABLE_ENTITY.value());
        }
        return builder.build();
    }

    public MatchLookupProto.LookupResponse getSuitableMatches(int requesterId, int gymId, int branchId) {
        LOG.info("getting suitable matches for user {}, for gym {} and branch {}", requesterId, gymId, branchId);
        MatchLookupProto.LookupResponse.Builder builder = MatchLookupProto.LookupResponse.newBuilder();
        try {
            List<MatchLookup> matches = matchLookupDao.possibleMatches(gymId, branchId, requesterId, MatcherConst.UNMATCHED.getName());
            if (CollectionUtils.isEmpty(matches)) {
                LOG.info("there are no records in match lookup table");
                throw new CustomException(MatchLookupConstants.NO_MATCH_LOOKUP_RECORD.getMessage());
            }
            LOG.info("found {} records in match lookup table. Building response", matches.size());
            mapperUtil.getResponseFromMatchLookup(matches, builder);
            builder.setMessage(MatchLookupConstants.MATCH_LOOKUP_RESPONSE_CREATED.getMessage())
                    .setResponseCode(HttpStatus.OK.value());
        } catch (Exception e) {
            builder.setMessage(e.getMessage())
                    .setResponseCode(HttpStatus.UNPROCESSABLE_ENTITY.value());
        }
        return builder.build();
    }

    public MatchLookupProto.LookupResponse deriveMatches(int requesterId) {
        return deriveMatches(requesterId, MatcherConst.UNMATCHED);
    }

    private MatchLookupProto.LookupResponse deriveMatches(int requesterId, MatcherConst matcherConst) {
        LOG.info("deriving matches for user id {}", requesterId);
        MatchLookupProto.LookupResponse.Builder builder = MatchLookupProto.LookupResponse.newBuilder();
        try {
            List<MatchLookup> matchLookupsForRequester = matchLookupDao.getMatchesByRequestIdAndStatus(requesterId, matcherConst.getName());
            if (CollectionUtils.isEmpty(matchLookupsForRequester)) {
                LOG.info("there is no match_lookup entries for requester id :{}", requesterId);
                throw new CustomException(MatchLookupConstants.NO_MATCH_LOOKUP_RECORD.getMessage());
            }
            matchLookupsForRequester.forEach(requester -> {
                List<MatchLookup> requesteeMatches = matchLookupDao.possibleMatches(requester.getGymId(), requester.getBranchId(), requesterId, matcherConst.getName());
                if (!CollectionUtils.isEmpty(requesteeMatches)) {
                    List<MatchLookup> newRequestees = requesteeMatches.stream().filter(requestee -> doesRequestExist.test(requester, requestee)).collect(Collectors.toList());
                    if(CollectionUtils.isEmpty(newRequestees))
                        LOG.info("there are no new requestees to derive for matchlookup {}", requester.getId());
                    else
                        mapperUtil.getResponseFromMatchLookup(newRequestees, builder);
                }
            });
            LOG.info("derived {} matches", builder.getLookupsList().size());
            builder.setMessage(MatchLookupConstants.MATCH_LOOKUP_RESPONSE_CREATED.getMessage())
                    .setResponseCode(HttpStatus.OK.value());
        } catch (Exception e) {
            builder.setMessage(e.getMessage())
                    .setResponseCode(HttpStatus.UNPROCESSABLE_ENTITY.value());
        }
        return builder.build();
    }

    public MatchLookupProto.ChatResponse matched(int requesterId) {
        MatchLookupProto.ChatResponse.Builder builder = MatchLookupProto.ChatResponse.newBuilder();
        try {
            List<Match> matches = matchDao.getAllByRequester(requesterId);
            if (CollectionUtils.isEmpty(matches)) {
                LOG.info("there is no matches present for requester id {}", requesterId);
                throw new CustomException(MatchLookupConstants.NO_MATCHES_AVAILABLE.getMessage());
            }
            mapperUtil.getResponseFromMatches(builder, matches);
            LOG.info("completed building chat response");
            builder.setMessage(MatchLookupConstants.MATCH_RESPONSE_CREATED.getMessage())
                    .setResponseCode(HttpStatus.OK.value());
        } catch (Exception e) {
            builder.setMessage(e.getMessage())
                    .setResponseCode(HttpStatus.UNPROCESSABLE_ENTITY.value());
            LOG.info("failed building match response for requester id {}", requesterId);
            e.printStackTrace();
        }
        return builder.build();
    }
}
