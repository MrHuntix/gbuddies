package com.example.gbuddy.service;

import com.example.gbuddy.dao.MatchLookupDao;
import com.example.gbuddy.dao.MatchRequestDao;
import com.example.gbuddy.exception.CustomException;
import com.example.gbuddy.models.constants.CommonConstants;
import com.example.gbuddy.models.constants.MatchRequestConstants;
import com.example.gbuddy.models.entities.MatchLookup;
import com.example.gbuddy.models.entities.MatchRequest;
import com.example.gbuddy.models.protos.MatchLookupProto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.concurrent.locks.ReentrantLock;

public class LikeTask implements Runnable {
    private static final Logger LOG = LoggerFactory.getLogger(LikeTask.class);

    private MatchLookupDao matchLookupDao;

    private MatchRequestDao matchRequestDao;

    private ReentrantLock reentrantLock;

    private int matchLookupId;

    private int userId;

    LikeTask(MatchLookupDao matchLookupDao, MatchRequestDao matchRequestDao, ReentrantLock reentrantLock, int matchLookupId, int userId) {
        this.matchLookupDao = matchLookupDao;
        this.matchRequestDao = matchRequestDao;
        this.reentrantLock = reentrantLock;
        this.matchLookupId = matchLookupId;
        this.userId = userId;
    }

    @Override
    public void run() {
        LOG.info("{}-task, start of like process for match lookup id {} requested by user id {}", Thread.currentThread().getName(), matchLookupId, userId);
        try {
            reentrantLock.lock();
            LOG.info("{}-task, acquired lock for match lookup id {} requested by user id {}", Thread.currentThread().getName(), matchLookupId, userId);
            MatchLookup requesteeLookup = matchLookupDao.getById(matchLookupId).orElse(null);
            MatchLookup requesterLookup = matchLookupDao.getRequestMatch(requesteeLookup.getGymId(), requesteeLookup.getBranchId(), userId).orElse(null);
            if (Objects.isNull(requesteeLookup) || Objects.isNull(requesterLookup)) {
                LOG.info("{}-task, no record in MATCH_LOOKUP for matchLookUpId {} and userId {}", Thread.currentThread().getName(), matchLookupId, userId);
                throw new CustomException(CommonConstants.CANNOT_LIKE.getMessage());
            }
            if (requesteeLookup.getRequesterId() == userId) {
                LOG.info("{}-task, user id({}) and requester id({}) is same", Thread.currentThread().getName(), userId, requesteeLookup.getRequesterId());
                throw new CustomException(CommonConstants.CANNOT_LIKE.getMessage());
            }
            MatchRequest requesteeContext = matchRequestDao.getMatchRequest(requesterLookup.getId(), requesteeLookup.getId(), requesterLookup.getRequesterId(), requesteeLookup.getRequesterId()).orElse(null);
            MatchRequest requesterContext = matchRequestDao.getMatchRequest(requesteeLookup.getId(), requesterLookup.getId(), requesteeLookup.getRequesterId(), requesterLookup.getRequesterId()).orElse(null);
            if (!Objects.isNull(requesteeContext) || !Objects.isNull(requesterContext)) {
                int id = requesteeContext == null?requesterContext.getId():requesteeContext.getId();
                LOG.info("{}-task, request for match already exists with id {}", Thread.currentThread().getName(), id);
                throw new CustomException(String.format(MatchRequestConstants.REQUEST_ALREADY_EXISTS.getStatus(), id));
            }
            LOG.info("{}-task, record found in MATCH_LOOKUP for id {}, with status {}. CREATING MATCH", Thread.currentThread().getName(), matchLookupId, requesteeLookup.getStatus());
            requesteeLookup.setStatus(MatchLookupProto.Status.REQUESTED.name());
            requesterLookup.setStatus(MatchLookupProto.Status.REQUESTED.name());
            matchLookupDao.save(requesteeLookup);
            matchLookupDao.save(requesterLookup);
            LOG.info("{}-task, updated status to requested for match lookup record {}, {}", Thread.currentThread().getName(), requesteeLookup.getId(), requesterLookup.getId());
            matchRequestDao.save(buildmatchRequest(requesteeLookup, requesterLookup));
            LOG.info("{}-task, match request entry made for lookup id {}", Thread.currentThread().getName(), matchLookupId);
            Thread.sleep(30000);
        } catch (Exception e) {
            LOG.info("{}-task, exception occurred", Thread.currentThread().getName());
            e.printStackTrace();
        } finally {
            LOG.info("{}-task, completed processing request for like for match lookup id {} requested by user id {}. Releasing lock", Thread.currentThread().getName(), matchLookupId, userId);
            reentrantLock.unlock();
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
