package com.example.gbuddy.service;

import com.example.gbuddy.dao.BuddyGraphDao;
import com.example.gbuddy.dao.MatchLookupDao;
import com.example.gbuddy.dao.MatchRequestDao;
import com.example.gbuddy.models.mbeans.LikeProcessorMBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.concurrent.locks.ReentrantLock;

@Component
@ManagedResource(objectName = "GBUDDIES-LIKE_PROCESSOR:name=likeProcessor")
public class LikeProcessor implements LikeProcessorMBean{
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

    void submitLikeRequest(SseEmitter emitter, int matchLookupId, int userId) {
        try {
            likeExecutor.execute(new LikeTask(emitter, matchLookupDao, matchRequestDao, likeLock, matchLookupId, userId));
        } catch (Exception e) {
            LOG.info("exception occurred while submitting like request {}", e.getMessage());
            e.printStackTrace();
        }
    }

    void submitFriendRequest(int matchRequestId) {
        try {
            friendRequestExecutor.execute(new FriendRequestTask(matchRequestId, friendRequestLock, matchLookupDao, matchRequestDao, buddyGraphDao));
        } catch (Exception e) {
            LOG.info("exception occurred while submitting friend request {}", e.getMessage());
            e.printStackTrace();
        }
    }

    @PreDestroy
    private void destroy() {
        try {
            if (likeExecutor != null && friendRequestExecutor != null) {
                LOG.info("shutting down executor");
                likeExecutor.shutdown();
                friendRequestExecutor.shutdown();
            }
        }catch (Exception e) {
            LOG.error("exception while shutting down executors {}", e.getMessage());
        }

    }

    @Override
    @ManagedAttribute(description = "get core pool size for like executor")
    public int getCorePoolSizeForLikeExecutor() {
        return this.corePoolSize;
    }

    @ManagedAttribute(description = "get core pool size for friend request executor")
    @Override
    public int getCorePoolSizeForFriendRequestExecutor() {
        return this.corePoolSize;
    }

    @Override
    @ManagedAttribute(description = "set core pool size for like executor")
    public void setCorePoolSizeForLikeExecutor(int corePoolSize) {
        this.corePoolSize = corePoolSize;
        this.likeExecutor.setCorePoolSize(this.corePoolSize);
    }

    @ManagedAttribute(description = "set core pool size for friend request executor")
    @Override
    public void setCorePoolSizeForFriendRequestExecutor(int corePoolSize) {
        this.corePoolSize = corePoolSize;
        this.friendRequestExecutor.setCorePoolSize(this.corePoolSize);
    }

    @ManagedAttribute(description = "thread prefix name for like executor")
    @Override
    public String getLikeExecutorPrefix() {
        return this.likePrefix;
    }

    @Override
    @ManagedAttribute(description = "thread prefix name for like executor")
    public String getFriendRequestExecutorPrefix() {
        return this.friendRequestPrefix;
    }
}
