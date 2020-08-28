package com.example.gbuddy.models.mbeans;

public interface LikeProcessorMBean {
    int getCorePoolSizeForLikeExecutor();
    int getCorePoolSizeForFriendRequestExecutor();
    String getLikeExecutorPrefix();
    String getFriendRequestExecutorPrefix();


}
