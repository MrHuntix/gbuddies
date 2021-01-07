package com.example.gbuddy.service;

import com.example.gbuddy.dao.MatchLookupDao;
import com.example.gbuddy.dao.MatchRequestDao;
import com.example.gbuddy.exception.CustomException;
import com.example.gbuddy.models.constants.CommonConstants;
import com.example.gbuddy.models.constants.MatchRequestConstants;
import com.example.gbuddy.models.entities.MatchLookup;
import com.example.gbuddy.models.entities.MatchRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.concurrent.locks.ReentrantLock;

public class LikeTask implements Runnable {
    private static final Logger LOG = LoggerFactory.getLogger(LikeTask.class);

    private MatchLookupDao matchLookupDao;

    private MatchRequestDao matchRequestDao;

    private ReentrantLock likeLock;

    private int matchLookupId;

    private int userId;

    LikeTask(MatchLookupDao matchLookupDao, MatchRequestDao matchRequestDao, ReentrantLock likeLock, int matchLookupId, int userId) {
        this.matchLookupDao = matchLookupDao;
        this.matchRequestDao = matchRequestDao;
        this.likeLock = likeLock;
        this.matchLookupId = matchLookupId;
        this.userId = userId;
    }

    @Override
    public void run() {
        LOG.info("start of like process for match lookup id {} requested by user id {}", matchLookupId, userId);
        try {
            likeLock.lock();
            LOG.info("acquired lock for match lookup id {} requested by user id {}", matchLookupId, userId);
            MatchLookup requesteeLookup = matchLookupDao.getById(matchLookupId).orElse(null);
            MatchLookup requesterLookup = matchLookupDao.getRequestMatch(requesteeLookup.getGymId(), requesteeLookup.getBranchId(), userId).orElse(null);
            if (Objects.isNull(requesteeLookup) || Objects.isNull(requesterLookup)) {
                LOG.info("no record in MATCH_LOOKUP for matchLookUpId {} and userId {}", matchLookupId, userId);
                throw new CustomException(CommonConstants.CANNOT_LIKE.getMessage());
            }
            if (requesteeLookup.getRequesterId() == userId) {
                LOG.info("user id({}) and requester id({}) is same", Thread.currentThread().getName(), userId, requesteeLookup.getRequesterId());
                throw new CustomException(CommonConstants.CANNOT_LIKE.getMessage());
            }
            MatchRequest requesteeContext = matchRequestDao.getMatchRequest(requesterLookup.getId(), requesteeLookup.getId(), requesterLookup.getRequesterId(), requesteeLookup.getRequesterId()).orElse(null);
            MatchRequest requesterContext = matchRequestDao.getMatchRequest(requesteeLookup.getId(), requesterLookup.getId(), requesteeLookup.getRequesterId(), requesterLookup.getRequesterId()).orElse(null);
            if (!Objects.isNull(requesteeContext) || !Objects.isNull(requesterContext)) {
                int id = requesteeContext == null?requesterContext.getId():requesteeContext.getId();
                LOG.info("request for match already exists with id {}", id);
                throw new CustomException(String.format(MatchRequestConstants.REQUEST_ALREADY_EXISTS.getStatus(), id));
            }
            LOG.info("record found in MATCH_LOOKUP for id {}, with status {}. CREATING MATCH", matchLookupId, requesteeLookup.getStatus());
//            requesteeLookup.setStatus(MatchLookupProto.Status.REQUESTED.name());
//            requesterLookup.setStatus(MatchLookupProto.Status.REQUESTED.name());
//            matchLookupDao.save(requesteeLookup);
//            matchLookupDao.save(requesterLookup);
            LOG.info("updated status to requested for match lookup record {}, {}", requesteeLookup.getId(), requesterLookup.getId());
            matchRequestDao.save(buildmatchRequest(requesteeLookup, requesterLookup));
            LOG.info("match request entry made for lookup id {}", Thread.currentThread().getName(), matchLookupId);
        } catch (Exception e) {
            LOG.info("exception occurred {}", e.getMessage());
            e.printStackTrace();
        } finally {
            LOG.info("completed processing request for like for match lookup id {} requested by user id {}. Releasing lock", matchLookupId, userId);
            likeLock.unlock();
        }
    }

    private MatchRequest buildmatchRequest(MatchLookup requestee, MatchLookup requester) {
        MatchRequest matchRequest = new MatchRequest();
        matchRequest.setLookupRequesteeId(requestee.getId());
        matchRequest.setUserRequesteeId(requestee.getRequesterId());
        matchRequest.setLookupRequesterId(requester.getId());
        matchRequest.setUserRequesterId(requester.getRequesterId());
        matchRequest.setStatus(MatchRequestConstants.REQUESTED.getStatus());
        return matchRequest;
    }
}
