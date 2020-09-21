package com.gbuddies.utils;

import com.gbuddies.models.ProfilePic;
import com.gbuddies.models.User;
import com.gbuddies.protos.LoginSignupProto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.sql.rowset.serial.SerialBlob;
import java.io.IOException;
import java.sql.SQLException;

@Component
public class AuthenticationMapperUtil {
    private static final Logger logger = LoggerFactory.getLogger(AuthenticationMapperUtil.class);

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
}
