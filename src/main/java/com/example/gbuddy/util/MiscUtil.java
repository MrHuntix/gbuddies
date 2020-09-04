package com.example.gbuddy.util;

import com.example.gbuddy.dao.BuddyGraphDao;
import com.example.gbuddy.dao.MatchDao;
import com.example.gbuddy.dao.MatchLookupDao;
import com.example.gbuddy.dao.MatchRequestDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;

@Component
public class MiscUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(MiscUtil.class);

    @Autowired
    private BuddyGraphDao buddyGraphDao;

    @Autowired
    private MatchLookupDao matchLookupDao;

    @Autowired
    private MatchRequestDao matchRequestDao;

    @Autowired
    MatchDao matchDao;

    @Transactional
    public String clenup() {
        String message;
        try {
            buddyGraphDao.deleteAll();
            matchLookupDao.deleteAll();
            matchRequestDao.deleteAll();
            message = "all records deleted";
        } catch (Exception e) {
            LOGGER.info("exception while performing db cleanup");
            message = e.getMessage();
            e.printStackTrace();
        }
        return message;
    }
}
