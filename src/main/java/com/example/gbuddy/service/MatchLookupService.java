package com.example.gbuddy.service;

import com.example.gbuddy.dao.*;
import com.example.gbuddy.exception.CustomException;
import com.example.gbuddy.models.*;
import com.example.gbuddy.models.constants.CommonConstants;
import com.example.gbuddy.models.constants.MatchLookupConstants;
import com.example.gbuddy.protos.MatchLookupProto;
import com.example.gbuddy.util.MapperUtil;
import com.example.gbuddy.util.MatcherConst;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Component
@Transactional
public class MatchLookupService {
    private static final Logger LOG = LoggerFactory.getLogger(MatchLookupService.class);
    @Autowired
    private MatchLookupDao matchLookupDao;

    @Autowired
    private MatchDao matchDao;

    @Autowired
    private GymDao gymDao;

    @Autowired
    private BranchDao branchDao;

    @Autowired
    private UserDao userDao;

    @Autowired
    private MapperUtil mapperUtil;

    public MatchLookupProto.MatchResponse addForLookup(int requesterId, int gymId, int branchId) {
        MatchLookupProto.MatchResponse.Builder builder = MatchLookupProto.MatchResponse.newBuilder();
        try {
            MatchLookup lookup = matchLookupDao.getRequestMatch(gymId, branchId, requesterId);
            if (lookup!=null && (MatcherConst.MATCHED.getName().equals(lookup.getStatus()) || MatcherConst.UNMATCHED.getName().equals(lookup.getStatus()))) {
                LOG.info("request already exists(id: {}, status: {})", lookup.getId(), lookup.getStatus());
                throw new CustomException(String.format(CommonConstants.LOOKUP_REQUEST_EXISTS.getMessage(), lookup.getId(), lookup.getStatus()));
            }
            if (Objects.isNull(lookup)) {
                MatchLookup match = new MatchLookup();
                match.setBranchId(branchId);
                match.setGymId(gymId);
                match.setRequesterId(requesterId);
                match.setStatus(MatcherConst.UNMATCHED.getName());
                matchLookupDao.save(match);
                LOG.info("created match(id: {}) for user {}, for gym {}", match.getId(), match.getRequesterId(), match.getGymId());
                builder.setMessage(CommonConstants.REQUEST_RAISED.getMessage())
                        .setResponseCode(HttpStatus.OK.value());
            }
        } catch (Exception e) {
            builder.setMessage(e.getMessage())
                    .setResponseCode(HttpStatus.UNPROCESSABLE_ENTITY.value());
        }
        return builder.build();

    }

    public MatchLookupProto.MatchResponse like(int matchLookupId, int userId) {
        MatchLookupProto.MatchResponse.Builder builder = MatchLookupProto.MatchResponse.newBuilder();
        LOG.info("start of like process for match lookup id {} and user id {}", matchLookupId, userId);
        try {
            MatchLookup lookup = matchLookupDao.getById(matchLookupId);
            MatchLookup requesterLookup = matchLookupDao.getRequestMatch(lookup.getGymId(), lookup.getBranchId(), userId);
            if (Objects.isNull(lookup) || Objects.isNull(requesterLookup)) {
                LOG.info("no record in MATCH_LOOKUP for matchLookUpId {} and userId {}", matchLookupId, userId);
                throw new CustomException(CommonConstants.CANNOT_LIKE.getMessage());
            }
            if (lookup.getRequesterId() == userId) {
                LOG.info("user id({}) and requester id({}) is same", userId, lookup.getRequesterId());
                throw new CustomException(CommonConstants.CANNOT_LIKE.getMessage());
            }
            if (MatcherConst.MATCHED.getName().equals(lookup.getStatus())) {
                LOG.info("record found in MATCH_LOOKUP for id {}, with status {}. SHOULD NOT HAPPEN", matchLookupId, lookup.getStatus());
                throw new CustomException(CommonConstants.IMPOSSIBLE_STATE.getMessage());
            }
            LOG.info("record found in MATCH_LOOKUP for id {}, with status {}. CREATING MATCH", matchLookupId, lookup.getStatus());
            lookup.setStatus(MatcherConst.MATCHED.getName());
            requesterLookup.setStatus(MatcherConst.MATCHED.getName());
            matchLookupDao.save(lookup);
            matchLookupDao.save(requesterLookup);
            Match match = new Match();
            match.setRequester(userId);
            match.setLookupId(lookup.getId());
            match.setGymId(lookup.getGymId());
            match.setBranchId(lookup.getBranchId());
            match.setRequestee(lookup.getRequesterId());
            matchDao.save(match);
            LOG.info("like process completed for match lookup id {} by user id {}", matchLookupId, userId);
            builder.setMessage(CommonConstants.LIKED.getMessage())
                    .setResponseCode(HttpStatus.OK.value());
        } catch (Exception e) {

        }
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
            MatchLookup requester = matchLookupDao.getRequestMatch(match.getGymId(), match.getBranchId(), match.getRequester());
            if(!Objects.isNull(requester) && !MatcherConst.MATCHED.getName().equalsIgnoreCase(requester.getStatus())) {
                LOG.info("Got requester. Status {}", requester.getStatus());
                throw new CustomException(CommonConstants.UNMATCH_FAIL.getMessage());
            }
            MatchLookup requestee = matchLookupDao.getRequestMatch(match.getGymId(), match.getBranchId(), match.getRequestee());
            if(!Objects.isNull(requestee) && !MatcherConst.MATCHED.getName().equalsIgnoreCase(requestee.getStatus())) {
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
            if(CollectionUtils.isEmpty(matches)) {
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
            List<MatchLookup> derivedMatches = new ArrayList<>();
            List<MatchLookup> matchLookupsForRequester = matchLookupDao.getMatchesByRequestIdAndStatus(requesterId, matcherConst.getName());
            if(CollectionUtils.isEmpty(matchLookupsForRequester)) {
                LOG.info("there is no match_lookup entries for requester id :{}", requesterId);
                throw new CustomException(MatchLookupConstants.NO_MATCH_LOOKUP_RECORD.getMessage());
            }
            matchLookupsForRequester.forEach(matchLookup -> {
                List<MatchLookup> matches = matchLookupDao.possibleMatches(matchLookup.getGymId(), matchLookup.getBranchId(), requesterId, matcherConst.getName());
                if(!CollectionUtils.isEmpty(matches))
                    mapperUtil.getResponseFromMatchLookup(matches, builder);
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

    public List<ChatResponse> matched(int requesterId) {
        List<Match> matches = matchDao.getMatched(requesterId);
        List<ChatResponse> chatResponses = new ArrayList<>();
        for (Match match: matches) {
            LOG.info("deriving chatresponse for {}", match);
            ChatResponse chatResponse = new ChatResponse();
            chatResponse.setMatch_id(match.getId());
            chatResponse.setLookup_id(match.getLookupId());
            Gym gym = gymDao.findById(match.getGymId()).orElse(null);

            if(gym == null) {
                LOG.info("for match {}, no gym exists with id {}", match.getId(), match.getGymId());
                continue;
            }
            Branch branch = branchDao.findById(match.getBranchId()).orElse(null);
            if(branch == null) {
                LOG.info("for match {}, no branch exists with id {}", match.getId(), match.getGymId());
                continue;
            }
            int userId = 0;
            if(match.getRequester() == requesterId) {
                LOG.info("using requestee id");
                userId = match.getRequestee();
            }
            if(match.getRequestee() == requesterId) {
                LOG.info("using requester id");
                userId = match.getRequester();
            }
            User user = userDao.
                    getByUserId(userId).orElse(null);
            if(user == null) {
                LOG.info("for match {}, no user exists with id {}", match.getId(), match.getGymId());
                continue;
            }
            chatResponse.setGymName(gym.getName());
            chatResponse.setWebsite(gym.getWebsite());
            chatResponse.setBranch(branch);
            chatResponse.setUser(user);
            chatResponses.add(chatResponse);
        }
        return chatResponses;
    }
}
