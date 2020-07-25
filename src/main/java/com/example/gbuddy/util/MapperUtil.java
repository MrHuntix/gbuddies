package com.example.gbuddy.util;

import com.example.gbuddy.dao.ProfilePicDao;
import com.example.gbuddy.models.*;
import com.example.gbuddy.protos.LoginSignupProto;
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

@Component
public class MapperUtil {
    private static final Logger logger = LoggerFactory.getLogger(MapperUtil.class);

    @Autowired
    private ProfilePicDao profilePicDao;

    public Gym getGymFromRequest(GymRegisterRequest registerRequest) {
        Gym gym = new Gym();
        gym.setName(registerRequest.getName());
        gym.setWebsite(registerRequest.getWebsite());
        for (GymBranch branch : registerRequest.getBranches()) {
            Branch b = new Branch();
            b.setGymId(gym);
            b.setLocality(branch.getLocality());
            b.setCity(branch.getCity());
            b.setLatitude(branch.getLatitude());
            b.setLongitude(branch.getLongitude());
            b.setContact(branch.getContact());
            gym.getBranches().add(b);
        }
        logger.info("persisting gym {}", gym);
        return gym;
    }

    public boolean validateRequest(GymRegisterRequest registerRequest) {
        return StringUtils.isNotEmpty(registerRequest.getName()) && StringUtils.isNotEmpty(registerRequest.getWebsite()) && !CollectionUtils.isEmpty(registerRequest.getBranches());
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
        Blob image = user.getProfilePic().getUserImage();
        responseBuilder
            .setUserName(user.getUserName())
            .setEmailId(user.getEmailId())
            .setMobileNo(user.getMobileNo())
            .setPicId(user.getProfilePic().getPicId())
            .setUserId(user.getUserId())
            .setAbout(user.getAbout())
            .setResponseCode(HttpStatus.OK.value())
            .build();

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
}
