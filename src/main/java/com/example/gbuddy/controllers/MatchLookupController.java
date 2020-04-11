package com.example.gbuddy.controllers;

import com.example.gbuddy.models.MatchLookup;
import com.example.gbuddy.service.MatchLookupService;
import com.example.gbuddy.util.MatcherUtil;
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

    @Autowired
    private MatcherUtil matcherUtil;

    @GetMapping("/test/match")
    public ResponseEntity test() {
        return ResponseEntity.ok("Hello from gym-matcher-service" + System.getProperty("server.port"));
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
    @PutMapping("/buddy/requester/{requesterId}/gym/{gymId}/branch/{branchId}")
    public ResponseEntity<String> addForLookup(@PathVariable("requesterId") int requesterId, @PathVariable("gymId") int gymId, @PathVariable("branchId") int branchId) {
        logger.info("adding for lookup requesterid: {}, gymid: {}, branch id: {}", requesterId, gymId, branchId);
        return matchLookupService.addForLookup(requesterId, gymId, branchId) ?
                ResponseEntity.ok("lookup reference created") :
                ResponseEntity.ok("lookup reference already exists");
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
    @PutMapping("/like/{matchLookupId}/by/{userId}")
    public ResponseEntity<String> like(@PathVariable("matchLookupId") int matchLookupId, @PathVariable("userId") int userId) {
        logger.info("linking matchLookupId: {}, userId: {}", matchLookupId, userId);
        return matchLookupService.like(matchLookupId, userId) ?
                ResponseEntity.ok("like request created") :
                ResponseEntity.ok("like request already exists");
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
    @PutMapping("/dislike/{matchId}")
    public ResponseEntity<String> unmatch(@PathVariable("matchId") int matchId) {
        logger.info("unmatching: {}", matchId);
        return matchLookupService.disike(matchId) ?
                ResponseEntity.ok("unmatched") : ResponseEntity.ok("no like request present");
    }

    /**
     * in matches tab
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
    @GetMapping("/all/{requesterId}/gym/{gymId}/branch/{branchId}")
    public ResponseEntity<List<MatchLookup>> getSuitableMatches(@PathVariable("requesterId") int requesterId, @PathVariable("gymId") int gymId, @PathVariable("branchId") int branchId) {
        List<MatchLookup> matches = matchLookupService.getSuitableMatches(requesterId, gymId, branchId);
        return Objects.nonNull(matches)? ResponseEntity.ok(matches)
                : ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
