package com.gbuddies.service;

import com.gbuddies.constants.MatchLookupConstants;
import com.gbuddies.constants.MatchRequestConstants;
import com.gbuddies.constants.MatcherConst;
import com.gbuddies.dao.BuddyGraphDao;
import com.gbuddies.dao.MatchLookupDao;
import com.gbuddies.dao.MatchRequestDao;
import com.gbuddies.exception.CustomException;
import com.gbuddies.models.BuddyGraph;
import com.gbuddies.models.MatchLookup;
import com.gbuddies.models.MatchRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.locks.ReentrantLock;

public class FriendRequestTask implements Runnable {
    private static final Logger LOG = LoggerFactory.getLogger(FriendRequestTask.class);

    private int matchRequestId;

    private ReentrantLock friendRequestLock;

    private MatchLookupDao matchLookupDao;

    private MatchRequestDao matchRequestDao;

    private BuddyGraphDao buddyGraphDao;

    FriendRequestTask(int matchRequestId, ReentrantLock friendRequestLock, MatchLookupDao matchLookupDao, MatchRequestDao matchRequestDao, BuddyGraphDao buddyGraphDao) {
        this.matchRequestId = matchRequestId;
        this.friendRequestLock = friendRequestLock;
        this.matchLookupDao = matchLookupDao;
        this.matchRequestDao = matchRequestDao;
        this.buddyGraphDao = buddyGraphDao;
    }

    @Override
    public void run() {
        try {
            LOG.info("start of processing for friend request {}", matchRequestId);
            friendRequestLock.lock();
            LOG.info("lock acquired to process friend request. Fetching match request {}", matchRequestId);
            MatchRequest matchRequest = matchRequestDao.findById(matchRequestId).orElse(null);
            if (Objects.isNull(matchRequest)) {
                LOG.info("there is no match request present with id {}", matchRequestId);
                throw new CustomException(String.format(MatchRequestConstants.NO_REQUEST_IS_PRESENT.getStatus(), matchRequestId));
            }
            if (MatchRequestConstants.ACCEPTED.getStatus().equalsIgnoreCase(matchRequest.getStatus())) {
                LOG.info("request is ACCEPTED. Unreachable state?");
                throw new CustomException(String.format(MatchRequestConstants.REQUEST_IS_ACCEPTED.getStatus(), matchRequestId));
            }
            MatchLookup lookupRequester = matchLookupDao.getById(matchRequest.getLookupRequesterId()).orElse(null);
            if (Objects.isNull(lookupRequester)) {
                LOG.info("there is no match lookup record with id {}", matchRequest.getLookupRequesterId());
                throw new CustomException(MatchLookupConstants.NO_MATCH_LOOKUP_RECORD.getMessage());
            }
            MatchLookup lookupRequestee = matchLookupDao.getById(matchRequest.getLookupRequesteeId()).orElse(null);
            if (Objects.isNull(lookupRequestee)) {
                LOG.info("there is no match lookup record with id {}", matchRequest.getLookupRequesteeId());
                throw new CustomException(MatchLookupConstants.NO_MATCH_LOOKUP_RECORD.getMessage());
            }
            LOG.info("updating match lookup status to MATCHED");
            lookupRequester.setStatus(MatcherConst.MATCHED.getName());
            lookupRequestee.setStatus(MatcherConst.MATCHED.getName());
            LOG.info("updating match request status to ACCEPTED");
            matchRequest.setStatus(MatchRequestConstants.ACCEPTED.getStatus());
            matchLookupDao.saveAll(Arrays.asList(lookupRequester, lookupRequestee));
            matchRequest = matchRequestDao.saveAndFlush(matchRequest);
            LOG.info("adding record to buddy graph table");
            addToBuddyGraph(matchRequest);
            LOG.info("completed processing for friend request {}", matchRequestId);
            Thread.sleep(40000);
        } catch (Exception e) {
            LOG.info("exception occurred {}", e.getMessage());
            e.printStackTrace();
        } finally {
            LOG.info("completed processing friend request {}", matchRequestId);
            friendRequestLock.unlock();
        }
    }

    private void addToBuddyGraph(MatchRequest matchRequest) throws CustomException {
        try {
            BuddyGraph requesterToRequesteeNode = new BuddyGraph();
            requesterToRequesteeNode.setUserId(matchRequest.getUserRequesterId());
            requesterToRequesteeNode.setUserBuddy(matchRequest.getUserRequesteeId());
            requesterToRequesteeNode.setMatchRequestId(matchRequest.getId());
            BuddyGraph requesteeToRequesterNode = new BuddyGraph();
            requesteeToRequesterNode.setUserId(matchRequest.getUserRequesteeId());
            requesteeToRequesterNode.setUserBuddy(matchRequest.getUserRequesterId());
            requesteeToRequesterNode.setMatchRequestId(matchRequest.getId());
            buddyGraphDao.saveAll(Arrays.asList(requesterToRequesteeNode, requesteeToRequesterNode));
        } catch (Exception e) {
            LOG.info("unable to add record to buddy graph with exception {}", e.getMessage());
            throw new CustomException(MatchRequestConstants.UNABLE_TO_ADD_TO_BUDDY_GRAPH.getStatus(), e);
        }
    }
}
