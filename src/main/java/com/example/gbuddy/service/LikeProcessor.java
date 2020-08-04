package com.example.gbuddy.service;

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
    private String prefix;

    @Autowired
    private MatchLookupDao matchLookupDao;

    @Autowired
    private MatchRequestDao matchRequestDao;

    private ReentrantLock reentrantLock = null;

    private ThreadPoolTaskExecutor executor = null;

    @PostConstruct
    private void init() {
        reentrantLock = new ReentrantLock(true);
        executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setThreadNamePrefix(prefix);
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setKeepAliveSeconds(Integer.MAX_VALUE);
        executor.initialize();
        LOG.info("init completed");

    }

    void submitLikeRequest(int matchLookupId, int userId) {
        try {
            executor.execute(new LikeTask(matchLookupDao, matchRequestDao, reentrantLock, matchLookupId, userId));
        } catch (Exception e) {
            LOG.info("exception {}", e.getMessage());
            e.printStackTrace();
        }
    }

    @PreDestroy
    private void destory() {
        if(executor!=null) {
            LOG.info("shutting down executor");
            executor.shutdown();
        }
    }
}
