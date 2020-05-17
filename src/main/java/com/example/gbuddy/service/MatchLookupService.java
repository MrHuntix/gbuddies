package com.example.gbuddy.service;

import com.example.gbuddy.dao.MatchDao;
import com.example.gbuddy.dao.MatchLookupDao;
import com.example.gbuddy.models.Match;
import com.example.gbuddy.models.MatchLookup;
import com.example.gbuddy.util.MatcherConst;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Component
public class MatchLookupService {
    private static final Logger LOG = LoggerFactory.getLogger(MatchLookupService.class);
    @Autowired
    private MatchLookupDao matchLookupDao;

    @Autowired
    private MatchDao matchDao;

    public boolean addForLookup(int requesterId, int gymId, int branchId) {
        MatchLookup lookup = matchLookupDao.getRequestMatch(gymId, branchId, requesterId);
        if (Objects.isNull(lookup)) {
            MatchLookup match = new MatchLookup();
            match.setBranchId(branchId);
            match.setGymId(gymId);
            match.setRequesterId(requesterId);
            match.setStatus(MatcherConst.UNMATCHED.getName());
            matchLookupDao.save(match);
            LOG.info("created match(id: {}) for user {}, for gym {}", match.getId(), match.getRequesterId(), match.getGymId());
            return true;
        }
        if (MatcherConst.MATCHED.getName().equals(lookup.getStatus()) || MatcherConst.UNMATCHED.getName().equals(lookup.getStatus())) {
            LOG.info("request already exists(id: {}, status: {})", lookup.getId(), lookup.getStatus());
        }
        return false;
    }

    public boolean like(int matchLookupId, int userId) {
        MatchLookup lookup = matchLookupDao.getById(matchLookupId);
        if (lookup.getRequesterId() == userId) {
            LOG.info("user id({}) and requester id({}) is same", userId, lookup.getRequesterId());
            return false;
        }
        if (Objects.isNull(lookup)) {
            LOG.info("no record in MATCH_LOOKUP for id {}", matchLookupId);
            return false;
        }
        if (MatcherConst.MATCHED.getName().equals(lookup.getStatus())) {
            LOG.info("record found in MATCH_LOOKUP for id {}, with status {}. SHOULD NOT HAPPEN", matchLookupId, lookup.getStatus());
            return false;
        }
        LOG.info("record found in MATCH_LOOKUP for id {}, with status {}. CREATING MATCH", matchLookupId, lookup.getStatus());
        lookup.setStatus(MatcherConst.MATCHED.getName());
        matchLookupDao.save(lookup);
        Match match = new Match();
        match.setRequester(lookup.getRequesterId());
        match.setLookupId(lookup.getId());
        match.setGymId(lookup.getGymId());
        match.setBranchId(lookup.getBranchId());
        match.setRequestee(userId);
        matchDao.save(match);
        lookup = matchLookupDao.getRequestMatch(lookup.getGymId(), lookup.getBranchId(), userId);
        lookup.setStatus(MatcherConst.MATCHED.getName());
        matchLookupDao.save(lookup);
        return true;
    }

    public boolean requestForLike(int matchLookupId, int userId) {
        MatchLookup lookup = matchLookupDao.getById(matchLookupId);
        if (lookup.getRequesterId() == userId) {
            LOG.info("user id({}) and requester id({}) is same", userId, lookup.getRequesterId());
            return false;
        }
        if (Objects.isNull(lookup)) {
            LOG.info("no record in MATCH_LOOKUP for id {}", matchLookupId);
            return false;
        }
        if (MatcherConst.REQUESTED.getName().equals(lookup.getStatus())) {
            LOG.info("record found in MATCH_LOOKUP for id {}, with status {}. SHOULD NOT HAPPEN", matchLookupId, lookup.getStatus());
            return false;
        }
        LOG.info("record found in MATCH_LOOKUP for id {}, with status {}. CREATING MATCH", matchLookupId, lookup.getStatus());
        lookup.setStatus(MatcherConst.REQUESTED.getName());
        LOG.info("setting status to REQUESTED for lookupId: {}", lookup.getId());
        matchLookupDao.save(lookup);

        lookup = matchLookupDao.getRequestMatch(lookup.getGymId(), lookup.getBranchId(), userId);
        lookup.setStatus(MatcherConst.REQUESTED.getName());
        LOG.info("setting status to REQUESTED for lookupId: {}", lookup.getId());
        matchLookupDao.save(lookup);
        return true;
    }

    public boolean disike(int matchId) {
        Match match = matchDao.findById(matchId).orElse(null);
        if (Objects.isNull(match)) {
            LOG.info("no record in MATCHES found for id: {}", matchId);
            return false;
        }
        MatchLookup requester = matchLookupDao.getRequestMatch(match.getGymId(), match.getBranchId(), match.getRequester());
        MatchLookup requestee = matchLookupDao.getRequestMatch(match.getGymId(), match.getBranchId(), match.getRequestee());
        if (Objects.isNull(requester) || Objects.isNull(requestee)) {
            LOG.info("no record in MATCH_LOOKUP for (gymId: {}, branchId: {}, requesterId: {})", match.getGymId(), match.getBranchId(), match.getRequester());
            return false;
        }

        if (MatcherConst.UNMATCHED.getName().equals(requester.getStatus()) || MatcherConst.UNMATCHED.getName().equals(requestee.getStatus())) {
            LOG.info("record found in MATCH_LOOKUP for (gymId: {}, branchId: {}, requesterId: {}) but status({}) invalid", match.getGymId(), match.getBranchId(), match.getRequester(), requester.getStatus());
            return false;
        }

        requester.setStatus(MatcherConst.UNMATCHED.getName());
        requestee.setStatus(MatcherConst.UNMATCHED.getName());
        LOG.info("requester: {}, requestee: {}", requester, requestee);
        matchLookupDao.save(requester);
        matchLookupDao.save(requestee);
        matchDao.delete(match);
        return true;
    }

    public List<MatchLookup> getSuitableMatches(int requesterId, int gymId, int branchId) {
        List<MatchLookup> matches = matchLookupDao.possibleMatches(gymId, branchId, requesterId, MatcherConst.UNMATCHED.getName());

        if(CollectionUtils.isEmpty(matches)){
            LOG.info("no records found in MATCH_LOOKUP for (gymId: {}, branchId: {}, requesterId: {})", gymId, branchId, requesterId);
        }
        return matches;
    }

    public List<MatchLookup> deriveMatches(int requesterId) {
        return deriveMatches(requesterId, MatcherConst.UNMATCHED);
    }

    public List<MatchLookup> deriveMatches(int requesterId, MatcherConst matcherConst) {
        List<MatchLookup> derivedMatches = new ArrayList<>();
        List<MatchLookup> matchLookupsForRequester = matchLookupDao.getAllByRequesterId(requesterId);
        if(matchLookupsForRequester==null || matchLookupsForRequester.isEmpty()) {
            LOG.info("there is no match_lookup entries for requester id :{}", requesterId);
            return null;
        }
        matchLookupsForRequester.forEach(matchLookup -> {
            List<MatchLookup> matches = matchLookupDao.possibleMatches(matchLookup.getGymId(), matchLookup.getBranchId(), requesterId, matcherConst.getName());
            if(matches!=null && !matches.isEmpty())
                derivedMatches.addAll(matches);
        });
        LOG.info("derived {} matches", derivedMatches.size());
        return derivedMatches;
    }
}
