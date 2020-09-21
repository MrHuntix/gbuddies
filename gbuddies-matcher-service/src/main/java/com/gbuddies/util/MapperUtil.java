package com.gbuddies.util;

import com.gbuddies.constants.MatchRequestConstants;
import com.gbuddies.dao.BranchDao;
import com.gbuddies.dao.MatchLookupDao;
import com.gbuddies.dao.MatchRequestDao;
import com.gbuddies.dao.UserDao;
import com.gbuddies.exception.CustomException;
import com.gbuddies.models.*;
import com.gbuddies.protos.MatchLookupProto;
import com.google.protobuf.ByteString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.Blob;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

@Component
public class MapperUtil {
    private static final Logger logger = LoggerFactory.getLogger(MapperUtil.class);

    @Autowired
    private UserDao userDao;

    @Autowired
    private BranchDao branchDao;

    @Autowired
    private MatchLookupDao matchLookupDao;

    @Autowired
    private MatchRequestDao matchRequestDao;

    public void getResponseFromMatchLookup(List<MatchLookup> requestees, MatchLookupProto.LookupResponse.Builder builder) {
        logger.info("in mapper adding {} requestee to lookup response", requestees.size());
        requestees.forEach(requestee -> {
            try {
                User user = userDao.getByUserId(requestee.getRequesterId()).orElse(null);
                if (Objects.isNull(user)) {
                    throw new CustomException(String.format(MatchRequestConstants.NO_USER_PRESENT.getStatus(), requestee.getRequesterId()));
                }
                Branch branch = branchDao.selectGymBranchRecordById(requestee.getBranchId(), requestee.getGymId()).orElse(null);
                if (Objects.isNull(branch)) {
                    throw new CustomException(String.format(MatchRequestConstants.NO_BRANCH_PRESENT.getStatus(), requestee.getGymId(), requestee.getBranchId()));
                }
                MatchLookupProto.MatchLookup.Builder lookupBuilder = MatchLookupProto.MatchLookup.newBuilder();

                lookupBuilder.setId(requestee.getId())
                        .setStatus(MatchLookupProto.Status.valueOf(requestee.getStatus()))
                        .setGym(MatchLookupProto.Gym.newBuilder()
                                .setGymId(branch.getGymId().getId())
                                .setGymName(branch.getGymId().getName())
                                .setWebsite(branch.getGymId().getWebsite())
                                .setBranch(MatchLookupProto.Branch.newBuilder()
                                        .setBranchId(branch.getId())
                                        .setLocality(branch.getLocality())
                                        .setCity(branch.getCity())
                                        .setLatitude(branch.getLatitude())
                                        .setLongitude(branch.getLongitude())
                                        .setContact(branch.getContact())
                                ))
                        .setUser(MatchLookupProto.User.newBuilder()
                                .setUserId(user.getUserId())
                                .setUserName(user.getEmailId())
                                .setMobileNo(user.getMobileNo())
                                .setUserImage(generateImageByteString(user.getProfilePic().getUserImage()))
                                .setAbout(user.getAbout()));
                builder.addLookups(lookupBuilder.build());
                logger.info("built and added requestee having match lookup id {}", requestee.getId());
            } catch (Exception e) {
                logger.info("got exception while buiding requestee having match lookup id {}", requestee.getId());
                e.printStackTrace();
            }

        });
        logger.info("completed building process");
    }

    public void getResponseFromMatches(MatchLookupProto.FriendResponse.Builder builder, List<BuddyGraph> friends) {
        logger.info("building response for {} friends", friends.size());
        friends.forEach(friend -> {
            try {
                MatchLookupProto.Friend.Builder friendBuilder = MatchLookupProto.Friend.newBuilder();
                MatchRequest matchRequest = matchRequestDao.findById(friend.getMatchRequestId()).orElse(null);
                if (Objects.isNull(matchRequest)) {
                    throw new CustomException(String.format(MatchRequestConstants.NO_MATCH_REQUEST_PRESENT.getStatus(), friend.getMatchRequestId()));
                }
                if (Objects.nonNull(matchRequest) && !MatchRequestConstants.ACCEPTED.getStatus().equalsIgnoreCase(matchRequest.getStatus())) {
                    throw new CustomException(String.format(MatchRequestConstants.MATCH_REQUEST_STATUS_NOT_ACCEPTED.getStatus(), matchRequest.getId(), matchRequest.getStatus()));
                }
                MatchLookup matchLookup = matchLookupDao.getById(matchRequest.getLookupRequesteeId()).orElse(null);
                if (Objects.isNull(matchLookup)) {
                    throw new CustomException(String.format(MatchRequestConstants.NO_MATCH_LOOKUP_RECORD.getStatus(), matchRequest.getLookupRequesteeId()));
                }
                Branch branch = branchDao.selectGymBranchRecordById(matchLookup.getBranchId(), matchLookup.getGymId()).orElse(null);
                if (Objects.isNull(branch)) {
                    throw new CustomException(String.format(MatchRequestConstants.NO_BRANCH_PRESENT.getStatus(), matchLookup.getGymId(), matchLookup.getBranchId()));
                }
                User user = userDao.getByUserId(friend.getUserBuddy()).orElse(null);
                if (Objects.isNull(user)) {
                    throw new CustomException(String.format(MatchRequestConstants.NO_USER_PRESENT.getStatus(), friend.getUserBuddy()));
                }
                friendBuilder.setMatchRequestId(matchRequest.getId())
                        .setUser(MatchLookupProto.User.newBuilder()
                                .setUserId(user.getUserId())
                                .setUserName(user.getUserName())
                                .setMobileNo(user.getMobileNo())
                                .setUserImage(generateImageByteString(user.getProfilePic().getUserImage()))
                                .setAbout(user.getAbout())
                                .build())
                        .setGym(MatchLookupProto.Gym.newBuilder()
                                .setGymId(branch.getGymId().getId())
                                .setGymName(branch.getGymId().getName())
                                .setWebsite(branch.getGymId().getWebsite())
                                .setBranch(MatchLookupProto.Branch.newBuilder()
                                        .setBranchId(branch.getId())
                                        .setLocality(branch.getLocality())
                                        .setCity(branch.getCity())
                                        .setLatitude(branch.getLatitude())
                                        .setLongitude(branch.getLongitude())
                                        .setContact(branch.getContact())
                                ));
                builder.addFriends(friendBuilder.build());
            } catch (Exception e) {
                logger.info("failed building friend for {}", friend.getId());
                e.printStackTrace();
            }
        });
    }

    public void buildFriendRequests(MatchLookupProto.FriendRequestsResponse.Builder builder, List<MatchRequest> requests) {
        requests.forEach(request -> {
            try {
                MatchLookupProto.FriendRequest.Builder friendRequest = MatchLookupProto.FriendRequest.newBuilder();
                MatchLookup matchLookup = matchLookupDao.getById(request.getLookupRequesterId()).orElse(null);
                friendRequest.setMatchRequestId(request.getId())
                        .setUser(buildUserForMatchLookup(matchLookup))
                        .setGym(buildGymForMatchLookup(matchLookup));
                builder.addFriendRequests(friendRequest.build());
            } catch (Exception e) {
                logger.info(e.getMessage());
                e.printStackTrace();
                return;
            }
        });
    }

    private MatchLookupProto.User buildUserForMatchLookup(MatchLookup matchLookup) throws CustomException, SQLException {
        MatchLookupProto.User.Builder user = MatchLookupProto.User.newBuilder();
        if (Objects.isNull(matchLookup)) {
            throw new CustomException(String.format(MatchRequestConstants.NO_LOOKUP_PRESENT.getStatus(), matchLookup.getRequesterId()));
        }
        User userFromDb = userDao.getByUserId(matchLookup.getRequesterId()).orElse(null);
        if (Objects.isNull(userFromDb)) {
            throw new CustomException(String.format(MatchRequestConstants.NO_USER_PRESENT.getStatus(), matchLookup.getRequesterId()));
        }
        user.setUserId(userFromDb.getUserId())
                .setUserName(userFromDb.getUserName())
                .setMobileNo(userFromDb.getMobileNo())
                .setUserImage(generateImageByteString(userFromDb.getProfilePic().getUserImage()))
                .setAbout(userFromDb.getAbout());
        return user.build();
    }

    private ByteString generateImageByteString(Blob image) throws SQLException {
        return ByteString.copyFrom(image.getBytes(1, Math.toIntExact(image.length())));
    }

    private MatchLookupProto.Gym buildGymForMatchLookup(MatchLookup matchLookup) throws CustomException {
        MatchLookupProto.Gym.Builder gym = MatchLookupProto.Gym.newBuilder();
        if (Objects.isNull(matchLookup)) {
            throw new CustomException(String.format(MatchRequestConstants.NO_LOOKUP_PRESENT.getStatus(), matchLookup.getRequesterId()));
        }
        Branch branch = branchDao.selectGymBranchRecordById(matchLookup.getBranchId(), matchLookup.getGymId()).orElse(null);
        if (Objects.isNull(branch)) {
            throw new CustomException(String.format(MatchRequestConstants.NO_BRANCH_PRESENT.getStatus(), matchLookup.getGymId(), matchLookup.getBranchId()));
        }
        gym.setGymId(branch.getGymId().getId())
                .setGymName(branch.getGymId().getName())
                .setWebsite(branch.getGymId().getWebsite())
                .setBranch(MatchLookupProto.Branch.newBuilder()
                        .setBranchId(branch.getId())
                        .setLocality(branch.getLocality())
                        .setCity(branch.getCity())
                        .setLatitude(branch.getLatitude())
                        .setLongitude(branch.getLongitude())
                        .setContact(branch.getContact())
                        .build());
        return gym.build();
    }
}
