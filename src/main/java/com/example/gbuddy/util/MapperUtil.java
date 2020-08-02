package com.example.gbuddy.util;

import com.example.gbuddy.dao.BranchDao;
import com.example.gbuddy.dao.ProfilePicDao;
import com.example.gbuddy.dao.UserDao;
import com.example.gbuddy.exception.CustomException;
import com.example.gbuddy.models.*;
import com.example.gbuddy.protos.GymProto;
import com.example.gbuddy.protos.LoginSignupProto;
import com.example.gbuddy.protos.MatchLookupProto;
import com.google.protobuf.ByteString;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.sql.rowset.serial.SerialBlob;
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

    public Gym getGymFromRequest(GymProto.Gym request) {
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
        return user;
    }

    public LoginSignupProto.SignupResponse buildSignUpResponse(LoginSignupProto.LoginResponse.Builder responseBuilder, User user) {
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
        return builder.setUserName(user.getUserName())
                .setEmailId(user.getEmailId())
                .setMobileNo(user.getMobileNo())
                .setPicId(user.getProfilePic().getPicId())
                .setUserId(user.getUserId())
                .setAbout(user.getAbout())
                .build();
    }

    public GymProto.FetchResponse getResponseFromEntity(List<Gym> gyms, GymProto.FetchResponse.Builder builder) {
        logger.info("start of response builder");
        gyms.forEach(gym -> builder.addGym(getGymProtoFromGymEntity(gym)));
        return builder.build();
    }

    private GymProto.Gym getGymProtoFromGymEntity(Gym gym) {
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

    public void getResponseFromMatchLookup(List<MatchLookup> matchLookupList, MatchLookupProto.LookupResponse.Builder builder) {
        matchLookupList.forEach(matchLookup -> {
            User user = userDao.getByUserId(matchLookup.getRequesterId()).orElse(null);
            Branch branch = branchDao.selectGymBranchRecordById(matchLookup.getBranchId(), matchLookup.getGymId()).orElse(null);
            if (Objects.isNull(user) || Objects.isNull(branch)) {
                logger.info("got empty result for user and gym for lookup id {}. Skipping record", matchLookup.getId());
            } else {
                MatchLookupProto.MatchLookup.Builder lookupBuilder = MatchLookupProto.MatchLookup.newBuilder();
                try {
                    lookupBuilder.setId(matchLookup.getId())
                            .setStatus(MatchLookupProto.Status.valueOf(matchLookup.getStatus()))
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
                    logger.info("built and added lookup for match lookup id {}", matchLookup.getId());
                } catch (Exception e) {
                    logger.info("got exception while buiding match lookup for match lookup id {}", matchLookup.getId());
                    e.printStackTrace();
                }
            }
        });
        logger.info("completed buiding process");
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
