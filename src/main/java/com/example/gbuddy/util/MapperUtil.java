package com.example.gbuddy.util;

import com.example.gbuddy.dao.BranchDao;
import com.example.gbuddy.dao.UserDao;
import com.example.gbuddy.exception.CustomException;
import com.example.gbuddy.models.entities.*;
import com.example.gbuddy.models.protos.GymProto;
import com.example.gbuddy.models.protos.LoginSignupProto;
import com.example.gbuddy.models.protos.MatchLookupProto;
import com.google.protobuf.ByteString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.sql.rowset.serial.SerialBlob;
import java.io.IOException;
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

    public Gym getGymFromRequest(GymProto.Gym request) {
        logger.info("building gym entity from proto for gym {}", request.getName());
        Gym gym = new Gym();
        gym.setName(request.getName());
        gym.setWebsite(request.getWebsite());
        for (GymProto.Branch branch : request.getBranchesList()) {
            Branch b = new Branch();
            b.setGymId(gym);
            b.setLocality(branch.getLocality());
            b.setCity(branch.getCity());
            b.setLatitude(branch.getLatitude());
            b.setLongitude(branch.getLongitude());
            b.setContact(branch.getContact());
            gym.getBranches().add(b);
        }
        logger.info("built gym entity {}", gym);
        return gym;
    }

    public User getUserFromUserSignupRequest(LoginSignupProto.SignupRequest userSignupRequest) throws SQLException, IOException {
        logger.info("building user entity from proto for use {}", userSignupRequest.getUserName());
        ProfilePic profilePic = new ProfilePic();
        profilePic.setUserImage(new SerialBlob(userSignupRequest.getUserImage().toByteArray()));
        User user = new User();
        user.setUserName(userSignupRequest.getUserName());
        user.setEmailId(userSignupRequest.getEmailId());
        user.setMobileNo(userSignupRequest.getMobileNo());
        user.setPassword(userSignupRequest.getPassword());
        user.setAbout(userSignupRequest.getAbout());
        user.setRoles(userSignupRequest.getRoles().name());
        user.setProfilePic(profilePic);
        profilePic.setUser(user);
        logger.info("built user entity");
        return user;
    }

    public LoginSignupProto.SignupResponse buildSignUpResponse(LoginSignupProto.LoginResponse.Builder responseBuilder, User user) {
        logger.info("building signup response proto");
        if (user != null) {
            responseBuilder
                .setUserName(user.getUserName())
                .setEmailId(user.getEmailId())
                .setMobileNo(user.getMobileNo())
                .setPicId(user.getProfilePic().getPicId())
                .setUserId(user.getUserId())
                .setAbout(user.getAbout())
                .build();
        }
        logger.info("built signup response with message: {} and code: {}", responseBuilder.getResponseMessage(), responseBuilder.getResponseCode());
        return LoginSignupProto.SignupResponse.newBuilder().setResponse(responseBuilder.build()).build();
    }

    public LoginSignupProto.LoginResponse buildLoginResponse(LoginSignupProto.LoginResponse.Builder builder, User user) {
        logger.info("building login response proto");
        return builder.setUserName(user.getUserName())
                .setEmailId(user.getEmailId())
                .setMobileNo(user.getMobileNo())
                .setPicId(user.getProfilePic().getPicId())
                .setUserId(user.getUserId())
                .setAbout(user.getAbout())
                .build();
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
                    .setLocality(branch.getLocality())
                    .setCity(branch.getCity())
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
            User user = userDao.getByUserId(requestee.getRequesterId()).orElse(null);
            Branch branch = branchDao.selectGymBranchRecordById(requestee.getBranchId(), requestee.getGymId()).orElse(null);
            if (Objects.isNull(user) || Objects.isNull(branch)) {
                logger.info("got empty result for user and gym for lookup id {}. Skipping record", requestee.getId());
            } else {
                MatchLookupProto.MatchLookup.Builder lookupBuilder = MatchLookupProto.MatchLookup.newBuilder();
                try {
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
                                    .setUserImage(ByteString.copyFrom(user.getProfilePic().getUserImage().getBytes(1, Math.toIntExact(user.getProfilePic().getUserImage().length()))))
                                    .setAbout(user.getAbout()));
                    builder.addLookups(lookupBuilder.build());
                    logger.info("built and added requestee having match lookup id {}", requestee.getId());
                } catch (Exception e) {
                    logger.info("got exception while buiding requestee having match lookup id {}", requestee.getId());
                    e.printStackTrace();
                }
            }
        });
        logger.info("completed building process");
    }

    public void getResponseFromMatches(MatchLookupProto.ChatResponse.Builder builder, List<Match> matches) {
        logger.info("building response for chat for {} matches", matches.size());
        matches.forEach(match -> {
            try {
                MatchLookupProto.Match.Builder matchBuilder = MatchLookupProto.Match.newBuilder();
                Branch branch = branchDao.selectGymBranchRecordById(match.getBranchId(), match.getGymId()).orElse(null);
                User user = userDao.getByUserId(match.getRequestee()).orElse(null);
                if (Objects.isNull(branch) || Objects.isNull(user)) {
                    logger.info("skipping building record for match id {} as branch or user is null", match.getId());
                    throw new CustomException("skipping building of match record");
                }
                matchBuilder.setMatchId(match.getId())
                        .setLookupId(match.getLookupId())
                        .setUser(MatchLookupProto.User.newBuilder()
                                .setUserId(user.getUserId())
                                .setUserName(user.getUserName())
                                .setMobileNo(user.getMobileNo())
                                .setUserImage(ByteString.copyFrom(user.getProfilePic().getUserImage().getBytes(1, Math.toIntExact(user.getProfilePic().getUserImage().length()))))
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
                builder.addMatches(matchBuilder.build());
            } catch (Exception e) {
                logger.info("failed building mactch object for match id {}", match.getId());
                e.printStackTrace();
                return;
            }
        });
    }
}
