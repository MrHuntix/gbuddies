package com.example.gbuddy.service;

import com.example.gbuddy.dao.BuddyGraphDao;
import com.example.gbuddy.dao.MatchLookupDao;
import com.example.gbuddy.dao.MatchRequestDao;
import com.example.gbuddy.exception.CustomException;
import com.example.gbuddy.models.constants.*;
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
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Arrays;
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
            //TODO: validate to ensure requesterId, gymId, branchId are valid and matchLookupDao.getRequestMatch(gymId, branchId, requesterId) should be null
            MatchLookup lookup = matchLookupDao.getRequestMatch(gymId, branchId, requesterId).orElse(null);
            if (lookup != null) {
                LOG.info("request already exists(lookup: {}, status: {})", lookup.getId(), lookup.getStatus());
                throw new CustomException(String.format(CommonConstants.LOOKUP_REQUEST_EXISTS.getMessage(), lookup.getId(), lookup.getStatus()));
            }
            MatchLookup match = new MatchLookup();
            match.setBranchId(branchId);
            match.setGymId(gymId);
            match.setRequesterId(requesterId);
            match.setStatus(MatcherConst.UNMATCHED.getName());
            matchLookupDao.save(match);
            LOG.info("created match(match: {}) for user {}, for gym {}", match.getId(), match.getRequesterId(), match.getGymId());
            builder.setMessage(ResponseMessageConstants.LOOKUP_CREATED.getMessage())
                    .setResponseCode(HttpStatus.OK.value());
        } catch (Exception e) {
            LOG.info("exception occurred while adding for lookup for requester {}, gym {}, branch {}", requesterId, gymId, branchId);
            e.printStackTrace();
            builder.setMessage(ResponseMessageConstants.FAILED_TO_CREATE_LOOKUP.getMessage())
                    .setResponseCode(HttpStatus.BAD_REQUEST.value());
        }
        return builder.build();

    }

    public MatchLookupProto.MatchResponse like(SseEmitter emitter, int matchLookupId, int userId) {
        MatchLookupProto.MatchResponse.Builder builder = MatchLookupProto.MatchResponse.newBuilder();
        LOG.info("start of like process for match lookup {} requested by user {}", matchLookupId, userId);
        likeProcessor.submitLikeRequest(emitter, matchLookupId, userId);
        builder.setMessage(ResponseMessageConstants.BUDDY_REQUEST_SENT.getMessage())
                .setResponseCode(HttpStatus.OK.value());
        return builder.build();
    }

    public MatchLookupProto.MatchResponse reject(int matchRequestId) {
        MatchLookupProto.MatchResponse.Builder builder = MatchLookupProto.MatchResponse.newBuilder();
        LOG.info("start of reject process for match request {}", matchRequestId);
        try {
            MatchRequest matchRequest = matchRequestDao.findById(matchRequestId).orElseThrow(() -> new CustomException(String.format(MatchRequestConstants.NO_REQUEST_IS_PRESENT.getStatus(), matchRequestId)));
            MatchLookup requestee = matchLookupDao.getById(matchRequest.getLookupRequesteeId()).orElseThrow(() -> new CustomException(String.format(MatchLookupConstants.NO_LOOKUP_RECORD.getMessage(), matchRequest.getLookupRequesteeId())));
            MatchLookup requester = matchLookupDao.getById(matchRequest.getLookupRequesterId()).orElseThrow(() -> new CustomException(String.format(MatchLookupConstants.NO_LOOKUP_RECORD.getMessage(), matchRequest.getLookupRequesterId())));

            if (MatchRequestConstants.ACCEPTED.getStatus().equalsIgnoreCase(matchRequest.getStatus())) {
                throw new CustomException(String.format(MatchLookupConstants.UNACCEPTABLE_STATUS.getMessage(), matchRequestId, matchRequest.getStatus()));
            }
            LOG.info("updating request status to REJECTED for match request {}", matchRequestId);
            matchRequest.setStatus(MatchRequestConstants.REJECTED.getStatus());
            matchRequestDao.save(matchRequest);
            LOG.info("updating lookup status to UNMATCHED for requester: {}, requestee: {}", requester.getId(), requestee.getId());
            requester.setStatus(MatcherConst.UNMATCHED.getName());
            requestee.setStatus(MatcherConst.UNMATCHED.getName());
            matchLookupDao.saveAll(Arrays.asList(requester, requestee));
            builder.setMessage(ResponseMessageConstants.REJECT_SUCCESS.getMessage())
                    .setResponseCode(HttpStatus.OK.value());
        } catch (Exception e) {
            LOG.info("exception occurred while processing reject request for match request {}", matchRequestId);
            e.printStackTrace();
            builder.setMessage(ResponseMessageConstants.REJECT_FAILED.getMessage())
                    .setResponseCode(HttpStatus.BAD_REQUEST.value());
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
            builder.setMessage(ResponseMessageConstants.FOUND_SUITABLE_BUDDIES.getMessage())
                    .setResponseCode(HttpStatus.OK.value());
        } catch (Exception e) {
            LOG.info("exception occurred while getting suitable matches for requester {}, gym {}, branch {}", requesterId, gymId, branchId);
            e.printStackTrace();
            builder.setMessage(ResponseMessageConstants.FAILED_TO_FIND_SUITABLE_MATCHES.getMessage())
                    .setResponseCode(HttpStatus.BAD_REQUEST.value());
        }
        return builder.build();
    }

    public MatchLookupProto.LookupResponse deriveMatches(int requesterId) {
        return deriveMatches(requesterId, MatcherConst.UNMATCHED);
    }

    private MatchLookupProto.LookupResponse deriveMatches(int requesterId, MatcherConst matcherConst) {
        LOG.info("deriving matches for user {}", requesterId);
        MatchLookupProto.LookupResponse.Builder builder = MatchLookupProto.LookupResponse.newBuilder();
        try {
            List<MatchLookup> matchLookupsForRequester = matchLookupDao.getMatchesByRequestIdAndStatus(requesterId, matcherConst.getName());
            if (CollectionUtils.isEmpty(matchLookupsForRequester)) {
                LOG.info("there is no match_lookup entries for requester {}", requesterId);
                throw new CustomException(MatchLookupConstants.NO_MATCH_LOOKUP_RECORD.getMessage());
            }
            matchLookupsForRequester.forEach(requester -> {
                List<MatchLookup> requesteeMatches = matchLookupDao.possibleMatches(requester.getGymId(), requester.getBranchId(), requesterId, matcherConst.getName());
                if (!CollectionUtils.isEmpty(requesteeMatches)) {
                    List<MatchLookup> newRequestees = requesteeMatches.stream()
                            .filter(requestee -> (doesRequestExist.test(requester, requestee)) && doesRequestExist.test(requestee, requester))
                            .collect(Collectors.toList());
                    if(CollectionUtils.isEmpty(newRequestees))
                        LOG.info("there are no new requestees to derive for matchlookup {}", requester.getId());
                    else
                        mapperUtil.getResponseFromMatchLookup(newRequestees, builder);
                }
            });
            LOG.info("derived {} matches", builder.getLookupsList().size());
            builder.setMessage(ResponseMessageConstants.DERIVED_BUDDIES.getMessage())
                    .setResponseCode(HttpStatus.OK.value());
        } catch (Exception e) {
            LOG.info("failed to derive matches for user {}", requesterId);
            e.printStackTrace();
            builder.setMessage(ResponseMessageConstants.FAILED_TO_DERIVE_MATCHES.getMessage())
                    .setResponseCode(HttpStatus.BAD_REQUEST.value());
        }
        return builder.build();
    }

    public MatchLookupProto.FriendResponse friends(int userId) {
        LOG.info("getting friends for user {}", userId);
        MatchLookupProto.FriendResponse.Builder builder = MatchLookupProto.FriendResponse.newBuilder();
        try {
            List<BuddyGraph> friends = buddyGraphDao.getByUserId(userId);
            if (CollectionUtils.isEmpty(friends)) {
                LOG.info("there is no friends present for user {}", userId);
                throw new CustomException(MatchLookupConstants.NO_FRIENDS_PRESENT.getMessage());
            }
            mapperUtil.getResponseFromMatches(builder, friends);
            LOG.info("completed building friend response");
            builder.setMessage(ResponseMessageConstants.DERIVE_FRIENDS.getMessage())
                    .setResponseCode(HttpStatus.OK.value());
        } catch (Exception e) {
            LOG.info("exception occurred while getting friends for user {}", userId);
            e.printStackTrace();
            builder.setMessage(e.getMessage())
                    .setResponseCode(HttpStatus.BAD_REQUEST.value());
            LOG.info("failed building match response for requester id {}", userId);
        }
        return builder.build();
    }

    public MatchLookupProto.FriendRequestsResponse getFriendRequests(int requesteeId) {
        MatchLookupProto.FriendRequestsResponse.Builder builder = MatchLookupProto.FriendRequestsResponse.newBuilder();
        LOG.info("getting friend requests for user {}", requesteeId);
        try {
            List<MatchRequest> requests = matchRequestDao.getByUserRequesteeIdAndStatus(requesteeId, MatchRequestConstants.REQUESTED.getStatus());
            if (CollectionUtils.isEmpty(requests)) {
                LOG.info("there are no requests for {}", requesteeId);
                throw new CustomException(String.format(MatchRequestConstants.NO_REQUESTS_PRESENT.getStatus(), requesteeId));
            }
            mapperUtil.buildFriendRequests(builder, requests);
        } catch (Exception e) {
            LOG.info("failed fetching friend requests for user{}", requesteeId);
            e.printStackTrace();
            builder.setMessage(ResponseMessageConstants.FAILED_TO_GET_FRIEND_REQUESTS.getMessage())
                    .setResponseCode(HttpStatus.BAD_REQUEST.value());
        }
        return builder.build();
    }

    public MatchLookupProto.MatchResponse acceptFriendRequest(int matchRequestId) {
        MatchLookupProto.MatchResponse.Builder builder = MatchLookupProto.MatchResponse.newBuilder();
        LOG.info("accepting friend request for match request {}", matchRequestId);
        likeProcessor.submitFriendRequest(matchRequestId);
        builder.setMessage(ResponseMessageConstants.BUDDY_REQUEST_ACCEPTED.getMessage())
                .setResponseCode(HttpStatus.OK.value());
        return builder.build();
    }
}
