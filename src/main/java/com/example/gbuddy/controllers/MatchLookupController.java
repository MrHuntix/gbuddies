package com.example.gbuddy.controllers;

import com.example.gbuddy.models.protos.MatchLookupProto;
import com.example.gbuddy.service.MatchLookupService;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/match")
public class MatchLookupController {
    private static final Logger logger = LoggerFactory.getLogger(MatchLookupController.class);

    @Autowired
    private MatchLookupService matchLookupService;

    @GetMapping(value = "/test/match")
    public ResponseEntity<String> test() {
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
    @PutMapping(value = "/buddy/requester/{requesterId}/gym/{gymId}/branch/{branchId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> addForLookup(@PathVariable("requesterId") int requesterId, @PathVariable("gymId") int gymId, @PathVariable("branchId") int branchId) throws InvalidProtocolBufferException {
        logger.info("adding for lookup requesterid: {}, gymid: {}, branch id: {}", requesterId, gymId, branchId);
        return ResponseEntity.ok(JsonFormat.printer().print(matchLookupService.addForLookup(requesterId, gymId, branchId)));
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
    @PutMapping(value = "/like/{matchLookupId}/by/{userId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> like(@PathVariable("matchLookupId") int matchLookupId, @PathVariable("userId") int userId) throws InvalidProtocolBufferException {
        logger.info("liking matchLookupId: {}, userId: {}", matchLookupId, userId);
        return ResponseEntity.ok(JsonFormat.printer().print(matchLookupService.like(matchLookupId, userId)));
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
    @PutMapping(value = "/reject/{matchRequestId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> reject(@PathVariable("matchRequestId") int matchRequestId) throws InvalidProtocolBufferException {
        logger.info("rejecting request: {}", matchRequestId);
        return ResponseEntity.ok(JsonFormat.printer().print(matchLookupService.reject(matchRequestId)));
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
    @GetMapping(value = "/all/{requesterId}/gym/{gymId}/branch/{branchId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getSuitableMatches(@PathVariable("requesterId") int requesterId, @PathVariable("gymId") int gymId,
                                                              @PathVariable("branchId") int branchId) throws InvalidProtocolBufferException {
        logger.info("getting suitable matches for user id: {} for gym id: {} and branch id: {}", requesterId, gymId, branchId);
        return ResponseEntity.ok(JsonFormat.printer().print(matchLookupService.getSuitableMatches(requesterId, gymId, branchId)));
    }

    /**
     * for requester id get all the gb -> (gym,branch) id pairs
     * for each gb get lookup info where status is unmatched.
     * use in matches tab
     *
     * @param requesterId
     * @return
     */
    @GetMapping(value = "/derive/{requesterId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> deriveMatches(@PathVariable("requesterId") int requesterId) throws InvalidProtocolBufferException {
        logger.info("deriving matches for user id: {}", requesterId);
        return ResponseEntity.ok(JsonFormat.printer().print(matchLookupService.deriveMatches(requesterId)));
    }

    @GetMapping(value = "/friends/{userId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getFriends(@PathVariable("userId") int userId) throws InvalidProtocolBufferException {
        logger.info("fetching friends for {}", userId);
        return ResponseEntity.ok(JsonFormat.printer().print(matchLookupService.friends(userId)));
    }

    @GetMapping(value = "/requests/{requesterId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getFriendRequests(@PathVariable("requesterId") int requesterId) throws InvalidProtocolBufferException {
        logger.info("fetching friend requests for user {}", requesterId);
        return ResponseEntity.ok(JsonFormat.printer().print(matchLookupService.getFriendRequests(requesterId)));
    }

    @PutMapping(value = "/accept/{matchRequestId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> acceptFriendRequest(@PathVariable("matchRequestId") int matchRequestId) throws InvalidProtocolBufferException {
        logger.info("accepting friend request {}", matchRequestId);
        return ResponseEntity.ok(JsonFormat.printer().print(matchLookupService.acceptFriendRequest(matchRequestId)));
    }
}
