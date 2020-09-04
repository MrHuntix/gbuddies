package com.example.gbuddy.models.mbeans;

public interface LikeProcessorMBean {
    int getCorePoolSizeForLikeExecutor();
    int getCorePoolSizeForFriendRequestExecutor();
    void setCorePoolSizeForLikeExecutor(int corePoolSize);
    void setCorePoolSizeForFriendRequestExecutor(int corePoolSize);
    String getLikeExecutorPrefix();
    String getFriendRequestExecutorPrefix();


}
