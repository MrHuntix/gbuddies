package com.example.gbuddy.util;

import com.example.gbuddy.dao.BranchDao;
import com.example.gbuddy.dao.MatchLookupDao;
import com.example.gbuddy.dao.MatchRequestDao;
import com.example.gbuddy.dao.UserDao;
import com.example.gbuddy.exception.CustomException;
import com.example.gbuddy.models.constants.MatchRequestConstants;
import com.example.gbuddy.models.constants.Role;
import com.example.gbuddy.models.entities.*;
import com.example.gbuddy.models.protos.CommonsProto;
import com.example.gbuddy.models.protos.GymProto;
import com.example.gbuddy.models.protos.LoginSignupProto;
import com.example.gbuddy.models.protos.MatchLookupProto;
import com.google.protobuf.ByteString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
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

    public Gym getGymFromRequest(GymProto.Gym request) {
        logger.info("building gym entity from proto for gym {}", request.getName());
        Gym gym = CommonUtils.buildGym(request.getName(), request.getWebsite());
        for (GymProto.Branch branch : request.getBranchesList()) {
            Address address = CommonUtils.buildAddressEntity(branch.getGymAddress().getCity(), branch.getGymAddress().getState(), branch.getGymAddress().getPincode());
            Branch b = CommonUtils.buildBranch(branch.getContact(), branch.getLatitude(), branch.getLongitude(), gym, address);
            gym.getBranches().add(b);
        }
        logger.info("built gym entity {}", gym);
        return gym;
    }

    public User getUserFromUserSignupRequest(LoginSignupProto.SignupRequest userSignupRequest) throws SQLException, IOException {
        logger.info("building user entity from proto for use {}", userSignupRequest.getName());
        Address address = CommonUtils.buildAddressEntity(userSignupRequest.getHomeAddress().getCity(), userSignupRequest.getHomeAddress().getState(), userSignupRequest.getHomeAddress().getPincode());
        User user = CommonUtils.buildUser(userSignupRequest.getName(), userSignupRequest.getMobileNo(), userSignupRequest.getPassword(), userSignupRequest.getPicUrl(), address, Role.valueOf(userSignupRequest.getRole()), userSignupRequest.getBio());
        logger.info("built user entity");
        return user;
    }

    public CommonsProto.AuthResponse buildAuthResponse(CommonsProto.AuthResponse.Builder responseBuilder, User user) {
        logger.info("building signup response proto");
        if (user != null) {
            responseBuilder
                    .setName(user.getName())
                    .setMobileNo(user.getMobile())
                    .setPicUrl(user.getPicUrl())
                    .setUserId(user.getId())
                    .setBio(user.getBio());
        }
        logger.info("built signup response with for user {}", user.getId());
        return responseBuilder.build();
    }

    public GymProto.FetchResponse getResponseFromEntity(List<Gym> gyms, GymProto.FetchResponse.Builder builder) {
        logger.info("building fetch response for gyms");
        gyms.forEach(gym -> builder.addGym(getGymProtoFromGymEntity(gym)));
        logger.info("built fetch response");
        return builder.build();
    }

    private GymProto.Gym getGymProtoFromGymEntity(Gym gym) {
        logger.info("building gym proto from entity for gym {}", gym.getName());
        GymProto.Gym.Builder builder = GymProto.Gym.newBuilder();
        int gymId = gym.getId();
        builder.setId(gym.getId())
                .setName(gym.getName())
                .setWebsite(gym.getWebsite());
        gym.getBranches().forEach(branch -> {
            GymProto.Branch.Builder branchBuilder = GymProto.Branch.newBuilder();
            branchBuilder.setId(branch.getId())
                    .setGymId(gymId)
                    .setGymAddress(CommonUtils.buildAddressProto(branch))
                    .setLatitude(branch.getLatitude())
                    .setLongitude(branch.getLongitude())
                    .setContact(branch.getContact());
            builder.addBranches(branchBuilder.build());
        });
        return builder.build();
    }

    public void getResponseFromMatchLookup(List<MatchLookup> requestees, MatchLookupProto.LookupResponse.Builder builder) {
        logger.info("in mapper adding {} requestee to lookup response", requestees.size());
        requestees.forEach(requestee -> {
            try {
                User user = userDao.getById(requestee.getRequesterId()).orElse(null);
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
                        .setGym(GymProto.Gym.newBuilder()
                                .setId(branch.getGymId().getId())
                                .setName(branch.getGymId().getName())
                                .setWebsite(branch.getGymId().getWebsite())
                                .addBranches(GymProto.Branch.newBuilder()
                                        .setId(branch.getId())
                                        .setGymAddress(CommonUtils.buildAddressProto(branch))
                                        .setLatitude(branch.getLatitude())
                                        .setLongitude(branch.getLongitude())
                                        .setContact(branch.getContact())
                                ))
                        .setUser(CommonUtils.buildAuthResponse(user));
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
                User user = userDao.getById(friend.getUserBuddy()).orElse(null);
                if (Objects.isNull(user)) {
                    throw new CustomException(String.format(MatchRequestConstants.NO_USER_PRESENT.getStatus(), friend.getUserBuddy()));
                }
                friendBuilder.setMatchRequestId(matchRequest.getId())
                        .setUser(CommonUtils.buildAuthResponse(user))
                        .setGym(GymProto.Gym.newBuilder()
                                .setId(branch.getGymId().getId())
                                .setName(branch.getGymId().getName())
                                .setWebsite(branch.getGymId().getWebsite())
                                .addBranches(GymProto.Branch.newBuilder()
                                        .setId(branch.getId())
                                        .setGymAddress(CommonUtils.buildAddressProto(branch))
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
                builder.setMessage(MatchRequestConstants.FRIEND_REQUESTS_PRESENT.getStatus());
                builder.setResponseCode(200);
            } catch (Exception e) {
                builder.setMessage(MatchRequestConstants.FAILED_TO_FIND_FRIEND_REQUEST.getStatus());
                builder.setResponseCode(204);
                logger.info(e.getMessage());
                e.printStackTrace();
                return;
            }
        });
    }

    private CommonsProto.AuthResponse buildUserForMatchLookup(MatchLookup matchLookup) throws CustomException, SQLException {
        CommonsProto.AuthResponse.Builder user = CommonsProto.AuthResponse.newBuilder();
        if (Objects.isNull(matchLookup)) {
            throw new CustomException(String.format(MatchRequestConstants.NO_LOOKUP_PRESENT.getStatus(), matchLookup.getRequesterId()));
        }
        User userFromDb = userDao.getById(matchLookup.getRequesterId()).orElse(null);
        if (Objects.isNull(userFromDb)) {
            throw new CustomException(String.format(MatchRequestConstants.NO_USER_PRESENT.getStatus(), matchLookup.getRequesterId()));
        }
        user.setUserId(userFromDb.getId())
                .setName(userFromDb.getName())
                .setMobileNo(userFromDb.getMobile())
                .setPicUrl(userFromDb.getPicUrl())
                .setBio(userFromDb.getBio());
        return user.build();
    }

    private ByteString generateImageByteString(Blob image) throws SQLException {
        return ByteString.copyFrom(image.getBytes(1, Math.toIntExact(image.length())));
    }

    private GymProto.Gym buildGymForMatchLookup(MatchLookup matchLookup) throws CustomException {
        GymProto.Gym.Builder gym = GymProto.Gym.newBuilder();
        if (Objects.isNull(matchLookup)) {
            throw new CustomException(String.format(MatchRequestConstants.NO_LOOKUP_PRESENT.getStatus(), matchLookup.getRequesterId()));
        }
        Branch branch = branchDao.selectGymBranchRecordById(matchLookup.getBranchId(), matchLookup.getGymId()).orElse(null);
        if (Objects.isNull(branch)) {
            throw new CustomException(String.format(MatchRequestConstants.NO_BRANCH_PRESENT.getStatus(), matchLookup.getGymId(), matchLookup.getBranchId()));
        }
        gym.setId(branch.getGymId().getId())
                .setName(branch.getGymId().getName())
                .setWebsite(branch.getGymId().getWebsite())
                .addBranches(GymProto.Branch.newBuilder()
                        .setId(branch.getId())
                        .setGymAddress(CommonUtils.buildAddressProto(branch))
                        .setLatitude(branch.getLatitude())
                        .setLongitude(branch.getLongitude())
                        .setContact(branch.getContact())
                        .build());
        return gym.build();
    }
}
