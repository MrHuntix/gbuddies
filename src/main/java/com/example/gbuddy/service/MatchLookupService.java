package com.example.gbuddy.service;

import com.example.gbuddy.dao.BuddyGraphDao;
import com.example.gbuddy.dao.MatchDao;
import com.example.gbuddy.dao.MatchLookupDao;
import com.example.gbuddy.dao.MatchRequestDao;
import com.example.gbuddy.exception.CustomException;
import com.example.gbuddy.models.constants.CommonConstants;
import com.example.gbuddy.models.constants.MatchLookupConstants;
import com.example.gbuddy.models.constants.MatchRequestConstants;
import com.example.gbuddy.models.constants.MatcherConst;
import com.example.gbuddy.models.entities.BuddyGraph;
import com.example.gbuddy.models.entities.MatchLookup;
import com.example.gbuddy.models.entities.MatchRequest;
import com.example.gbuddy.models.protos.MatchLookupProto;
import com.example.gbuddy.util.MapperUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;
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

    @Autowired
    private BuddyGraphDao buddyGraphDao;

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

    public MatchLookupProto.MatchResponse reject(int matchRequestId) {
        MatchLookupProto.MatchResponse.Builder builder = MatchLookupProto.MatchResponse.newBuilder();
        LOG.info("start of reject process for match id {}", matchRequestId);
        try {
            MatchRequest matchRequest = matchRequestDao.findById(matchRequestId).orElseThrow(() -> new CustomException(String.format(MatchRequestConstants.NO_REQUEST_IS_PRESENT.getStatus(), matchRequestId)));
            MatchLookup requestee = matchLookupDao.getById(matchRequest.getLookupRequesteeId()).orElseThrow(() -> new CustomException(String.format(MatchLookupConstants.NO_LOOKUP_RECORD.getMessage(), matchRequest.getLookupRequesteeId())));
            MatchLookup requester = matchLookupDao.getById(matchRequest.getLookupRequesterId()).orElseThrow(() -> new CustomException(String.format(MatchLookupConstants.NO_LOOKUP_RECORD.getMessage(), matchRequest.getLookupRequesterId())));

            if (MatchRequestConstants.ACCEPTED.getStatus().equalsIgnoreCase(matchRequest.getStatus())) {
                throw new CustomException(String.format(MatchLookupConstants.UNACCEPTABLE_STATUS.getMessage(), matchRequestId, matchRequest.getStatus()));
            }
            LOG.info("updating request status to REJECTED for request id {}", matchRequestId);
            matchRequest.setStatus(MatchRequestConstants.REJECTED.getStatus());
            matchRequestDao.save(matchRequest);
            LOG.info("updating lookup status to UNMATCHED for requester: {}, requestee: {}", requester.getId(), requestee.getId());
            requester.setStatus(MatcherConst.UNMATCHED.getName());
            requestee.setStatus(MatcherConst.UNMATCHED.getName());
            matchLookupDao.save(requester);
            matchLookupDao.save(requestee);
            builder.setMessage(CommonConstants.REJECT_SUCCESS.getMessage())
                    .setResponseCode(HttpStatus.OK.value());
        } catch (Exception e) {
            LOG.info("exception while processing reject request");
            e.printStackTrace();
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

    public MatchLookupProto.FriendResponse friends(int userId) {
        MatchLookupProto.FriendResponse.Builder builder = MatchLookupProto.FriendResponse.newBuilder();
        try {
            List<BuddyGraph> friends = buddyGraphDao.getByUserId(userId);
            if (CollectionUtils.isEmpty(friends)) {
                LOG.info("there is no friends present for user {}", userId);
                throw new CustomException(MatchLookupConstants.NO_FRIENDS_PRESNT.getMessage());
            }
            mapperUtil.getResponseFromMatches(builder, friends);
            LOG.info("completed building friend response");
            builder.setMessage(MatchLookupConstants.FRIEND_RESPONSE_CREATED.getMessage())
                    .setResponseCode(HttpStatus.OK.value());
        } catch (Exception e) {
            builder.setMessage(e.getMessage())
                    .setResponseCode(HttpStatus.UNPROCESSABLE_ENTITY.value());
            LOG.info("failed building match response for requester id {}", userId);
            e.printStackTrace();
        }
        return builder.build();
    }

    public MatchLookupProto.FriendRequestsResponse getFriendRequests(int requesteeId) {
        MatchLookupProto.FriendRequestsResponse.Builder builder = MatchLookupProto.FriendRequestsResponse.newBuilder();
        try {
            List<MatchRequest> requests = matchRequestDao.getByUserRequesteeIdAndStatus(requesteeId, MatchRequestConstants.REQUESTED.getStatus());
            if (CollectionUtils.isEmpty(requests)) {
                LOG.info("there are no requests for {}", requesteeId);
                throw new CustomException(String.format(MatchRequestConstants.NO_REQUESTS_PRESENT.getStatus(), requesteeId));
            }
            mapperUtil.buildFriendRequests(builder, requests);
        } catch (Exception e) {
            LOG.info("failed fetching friend requests for {}", requesteeId);
            builder.setMessage(e.getMessage())
                    .setResponseCode(HttpStatus.UNPROCESSABLE_ENTITY.value());
        }
        return builder.build();
    }

    public MatchLookupProto.MatchResponse acceptFriendRequest(int matchRequestId) {
        MatchLookupProto.MatchResponse.Builder builder = MatchLookupProto.MatchResponse.newBuilder();
        LOG.info("sending friend request for id {}", matchRequestId);
        likeProcessor.submitFriendRequest(matchRequestId);
        builder.setMessage(MatchRequestConstants.BUDDY_REQUEST_PLACED.getStatus())
                .setResponseCode(HttpStatus.OK.value());
        return builder.build();
    }
}
