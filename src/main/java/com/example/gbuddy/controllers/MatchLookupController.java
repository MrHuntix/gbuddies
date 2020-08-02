package com.example.gbuddy.controllers;

import com.example.gbuddy.models.ChatResponse;
import com.example.gbuddy.models.Match;
import com.example.gbuddy.models.MatchLookup;
import com.example.gbuddy.models.MatchResponse;
import com.example.gbuddy.protos.MatchLookupProto;
import com.example.gbuddy.service.MatchLookupService;
import com.example.gbuddy.util.MatcherConst;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/match")
public class MatchLookupController {
    private static final Logger logger = LoggerFactory.getLogger(MatchLookupController.class);

    @Autowired
    private MatchLookupService matchLookupService;

    @GetMapping("/test/match")
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
    @PutMapping(value = "/buddy/requester/{requesterId}/gym/{gymId}/branch/{branchId}")
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
    @PutMapping(value = "/like/{matchLookupId}/by/{userId}")
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
     * @param matchId pk of MATCH table
     * @return
     */
    @PutMapping(value = "/unmatch/{matchId}")
    public MatchLookupProto.MatchResponse unmatch(@PathVariable("matchId") int matchId) {
        logger.info("unmatching: {}", matchId);
        return matchLookupService.unmatch(matchId);
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
    @GetMapping(value = "/all/{requesterId}/gym/{gymId}/branch/{branchId}")
    public MatchLookupProto.LookupResponse getSuitableMatches(@PathVariable("requesterId") int requesterId, @PathVariable("gymId") int gymId,
                                                                @PathVariable("branchId") int branchId) {
        logger.info("getting suitable matches for user id: {} for gym id: {} and branch id: {}", requesterId, gymId, branchId);
        return matchLookupService.getSuitableMatches(requesterId, gymId, branchId);
    }

    /**
     * for requester id get all the gb -> (gym,branch) id pairs
     * for each gb get lookup info where status is unmatched.
     * use in matches tab
     * @param requesterId
     * @return
     */
    @GetMapping(value = "/derive/{requesterId}")
    public MatchLookupProto.LookupResponse deriveMatches(@PathVariable("requesterId") int requesterId) {
        logger.info("deriving matches for user id: {}", requesterId);
        return matchLookupService.deriveMatches(requesterId);
    }

    @GetMapping(value = "/matched/{requesterId}")
    public MatchLookupProto.ChatResponse getMatched(@PathVariable("requesterId") int requesterId) {
        logger.info("start of matched fetch process for requester {}", requesterId);
        return matchLookupService.matched(requesterId);
    }
}
