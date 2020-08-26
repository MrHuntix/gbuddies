package com.example.gbuddy.service;

import com.example.gbuddy.dao.BuddyGraphDao;
import com.example.gbuddy.dao.MatchLookupDao;
import com.example.gbuddy.dao.MatchRequestDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.concurrent.locks.ReentrantLock;

@Component
public class LikeProcessor {
    private static final Logger LOG = LoggerFactory.getLogger(LikeProcessor.class);

    @Value("${like.processor.core.pool.size}")
    private int corePoolSize;

    @Value("${like.processor.max.pool.size}")
    private int maxPoolSize;

    @Value("${like.processor.thread.prefix}")
    private String likePrefix;

    @Value("${friend.request.processor.thread.prefix}")
    private String friendRequestPrefix;

    @Autowired
    private MatchLookupDao matchLookupDao;

    @Autowired
    private MatchRequestDao matchRequestDao;

    @Autowired
    private BuddyGraphDao buddyGraphDao;

    private ReentrantLock likeLock = null;

    private ReentrantLock friendRequestLock = null;

    private ThreadPoolTaskExecutor likeExecutor = null;

    private ThreadPoolTaskExecutor friendRequestExecutor = null;

    @PostConstruct
    private void init() {
        likeLock = new ReentrantLock(true);
        likeExecutor = new ThreadPoolTaskExecutor();
        likeExecutor.setCorePoolSize(corePoolSize);
        likeExecutor.setMaxPoolSize(maxPoolSize);
        likeExecutor.setThreadNamePrefix(likePrefix);
        likeExecutor.setWaitForTasksToCompleteOnShutdown(true);
        likeExecutor.setKeepAliveSeconds(Integer.MAX_VALUE);
        likeExecutor.initialize();
        LOG.info("init completed for like executor");

        friendRequestLock = new ReentrantLock(true);
        friendRequestExecutor = new ThreadPoolTaskExecutor();
        friendRequestExecutor.setCorePoolSize(corePoolSize);
        friendRequestExecutor.setMaxPoolSize(maxPoolSize);
        friendRequestExecutor.setThreadNamePrefix(friendRequestPrefix);
        friendRequestExecutor.setWaitForTasksToCompleteOnShutdown(true);
        friendRequestExecutor.setKeepAliveSeconds(Integer.MAX_VALUE);
        friendRequestExecutor.initialize();
        LOG.info("init completed for friend request executor");
    }

    void submitLikeRequest(int matchLookupId, int userId) {
        try {
            likeExecutor.execute(new LikeTask(matchLookupDao, matchRequestDao, likeLock, matchLookupId, userId));
        } catch (Exception e) {
            LOG.info("exception {}", e.getMessage());
            e.printStackTrace();
        }
    }

    void submitFriendRequest(int matchRequestId) {
        try {
            friendRequestExecutor.execute(new FriendRequestTask(matchRequestId, friendRequestLock, matchLookupDao, matchRequestDao, buddyGraphDao));
        } catch (Exception e) {
            LOG.info("exception {}", e.getMessage());
            e.printStackTrace();
        }
    }

    @PreDestroy
    private void destory() {
        if (likeExecutor != null && friendRequestExecutor != null) {
            LOG.info("shutting down executor");
            likeExecutor.shutdown();
            friendRequestExecutor.shutdown();
        }
    }
}
