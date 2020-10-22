package com.gbuddies.controllers;

import com.gbuddies.protos.MatchLookupProto;
import com.gbuddies.service.MatchLookupService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("match")
public class MatchLookupController {
    private static final Logger logger = LoggerFactory.getLogger(MatchLookupController.class);

    @Autowired
    private MatchLookupService matchLookupService;

    @GetMapping(path = "/up")
    public ResponseEntity test() {
        return ResponseEntity.ok("match service is up and running");
    }

    /**
     * on click of buddie up
     * if gymid, branchid, requestid present
     * if status == UNMATCHED
     * buddy request already present
     * if status == MATCHED
     * buddy already present
     * else
     * create record in match_lookup with status = UNMATCHED
     *
     * @param requesterId id of the requesting user
     * @param gymId       id of gym
     * @param branchId    id of branch
     * @return
     */
    @PutMapping(path = "/buddy/requester/{requesterId}/gym/{gymId}/branch/{branchId}")
    public MatchLookupProto.MatchResponse addForLookup(@PathVariable("requesterId") int requesterId, @PathVariable("gymId") int gymId, @PathVariable("branchId") int branchId) {
        logger.info("adding for lookup requesterid: {}, gymid: {}, branch id: {}", requesterId, gymId, branchId);
        return matchLookupService.addForLookup(requesterId, gymId, branchId);
    }

    /**
     * if matchLookupId present
     * if status == UNMATCHED
     * set status = MATCHED
     * add record to matches
     * if status == MATCHED
     * buddy already present
     * else
     * should not happen and should be logged as matchLookupId is pk in match_lookup
     *
     * @param matchLookupId pk id of match_lookup
     * @param userId        requester id
     * @return
     */
    @PutMapping(path = "/like/{matchLookupId}/by/{userId}")
    public MatchLookupProto.MatchResponse like(@PathVariable("matchLookupId") int matchLookupId, @PathVariable("userId") int userId) {
        logger.info("liking matchLookupId: {}, userId: {}", matchLookupId, userId);
        return matchLookupService.like(matchLookupId, userId);
    }

    /**
     * if gymid, branchid, requestid present
     * if status == UNMATCHED
     * should not happen and should be logged
     * if status == MATCHED
     * set status = UNMATCHED and remove record from matches
     * else
     * should not happen and should be logged
     *
     * @param matchRequestId pk of MATCH_REQUEST table
     * @return
     */
    @PutMapping(path = "/reject/{matchRequestId}")
    public MatchLookupProto.MatchResponse reject(@PathVariable("matchRequestId") int matchRequestId) {
        logger.info("rejecting request: {}", matchRequestId);
        return matchLookupService.reject(matchRequestId);
    }

    /**
     * use only for getting suitable matches for a single gym,branch,requester id pair
     * if gymid, branchid, requestid present
     * if status == UNMATCHED and resuestid != requesting_user
     * return all matching records
     * else
     * no one is looking for a buddy in the current gym
     *
     * @param requesterId user requesting for buddy
     * @param gymId       id of gym
     * @param branchId    id of branch
     * @return
     */
    @GetMapping(path = "/all/{requesterId}/gym/{gymId}/branch/{branchId}")
    public MatchLookupProto.LookupResponse getSuitableMatches(@PathVariable("requesterId") int requesterId, @PathVariable("gymId") int gymId,
                                                              @PathVariable("branchId") int branchId) {
        logger.info("getting suitable matches for user id: {} for gym id: {} and branch id: {}", requesterId, gymId, branchId);
        return matchLookupService.getSuitableMatches(requesterId, gymId, branchId);
    }

    /**
     * for requester id get all the gb -> (gym,branch) id pairs
     * for each gb get lookup info where status is unmatched.
     * use in matches tab
     *
     * @param requesterId
     * @return
     */
    @GetMapping(path = "/derive/{requesterId}")
    public MatchLookupProto.LookupResponse deriveMatches(@PathVariable("requesterId") int requesterId) {
        logger.info("deriving matches for user id: {}", requesterId);
        return matchLookupService.deriveMatches(requesterId);
    }

    @GetMapping(path = "/friends/{userId}")
    public MatchLookupProto.FriendResponse getFriends(@PathVariable("userId") int userId) {
        logger.info("fetching friends for {}", userId);
        return matchLookupService.friends(userId);
    }

    @GetMapping(value = "/requests/{requesterId}")
    public MatchLookupProto.FriendRequestsResponse getFriendRequests(@PathVariable("requesterId") int requesterId) {
        logger.info("fetching friend requests for user {}", requesterId);
        return matchLookupService.getFriendRequests(requesterId);
    }

    @PutMapping(path = "/accept/{matchRequestId}")
    public MatchLookupProto.MatchResponse acceptFriendRequest(@PathVariable("matchRequestId") int matchRequestId) {
        logger.info("accepting friend request {}", matchRequestId);
        return matchLookupService.acceptFriendRequest(matchRequestId);
    }
}
