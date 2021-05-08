package com.example.gbuddy.service;

import com.example.gbuddy.dao.TokenDao;
import com.example.gbuddy.dao.UserDao;
import com.example.gbuddy.exception.CustomException;
import com.example.gbuddy.models.entities.Token;
import com.example.gbuddy.models.entities.User;
import com.example.gbuddy.models.protos.CommonsProto;
import com.example.gbuddy.models.protos.LoginSignupProto;
import com.example.gbuddy.models.response.ValidationResponse;
import com.example.gbuddy.service.validators.AuthenticationValidator;
import com.example.gbuddy.util.JwtUtil;
import com.example.gbuddy.util.MapperUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthenticationService {
    private static final Logger logger = LoggerFactory.getLogger(AuthenticationService.class);

    @Autowired
    private UserDao userDao;

    @Autowired
    private TokenDao tokenDao;

    @Autowired
    private MapperUtil mapperUtil;

    @Autowired
    private AuthenticationValidator authenticationValidator;

    @Autowired
    private JwtUtil jwtUtil;

    public CommonsProto.AuthResponse login(LoginSignupProto.LoginRequest userLoginRequest) {
        CommonsProto.AuthResponse.Builder builder = CommonsProto.AuthResponse.newBuilder();
        CommonsProto.AuthResponse response;

        logger.info("start or login process");
        try {
            ValidationResponse<String, User> validationResponse = authenticationValidator.validateLoginRequest(userLoginRequest);
            if (!validationResponse.getValidationMessage().isEmpty()) {
                logger.info("validation failed with {} issues", validationResponse.getValidationMessage().size());
                throw new CustomException(String.join("|", validationResponse.getValidationMessage()));
            }
            User user = validationResponse.getValidObject();
            builder.setPicUrl(user.getPicUrl());
            response = mapperUtil.buildAuthResponse(builder, user);
        } catch (Exception e) {
            logger.info("exception occurred during login process {}", e.getMessage());
            e.printStackTrace();
            response = builder.build();
        }
        return response;
    }

    public CommonsProto.AuthResponse signup(LoginSignupProto.SignupRequest userSignupRequest) {
        CommonsProto.AuthResponse.Builder responseBuilder = CommonsProto.AuthResponse.newBuilder();
        CommonsProto.AuthResponse response;
        try {
            ValidationResponse<String, User> validationResponse = authenticationValidator.validateSignupRequest(userSignupRequest);
            if (!validationResponse.getValidationMessage().isEmpty()) {
                logger.info("validation failed with {} issues", validationResponse.getValidationMessage().size());
                throw new CustomException(String.join("|", validationResponse.getValidationMessage()));
            }
            String token = jwtUtil.generateToken(userSignupRequest.getMobileNo());
            Token createdToken = new Token();
            createdToken.setActive(1);
            createdToken.setToken(token);
            User user = mapperUtil.getUserFromUserSignupRequest(userSignupRequest);
            logger.info("creating new user, {}", user);
            user = userDao.save(user);
            logger.info("user {} created", user.getId());
            createdToken.setUserId(user.getId());
            createdToken = tokenDao.save(createdToken);
            logger.info("token created for {}", createdToken.getId());
            responseBuilder.setPicUrl(user.getPicUrl());
            responseBuilder.setToken(token);
            response = mapperUtil.buildAuthResponse(responseBuilder, user);
        } catch (Exception e) {
            logger.info("exception occurred during signup process {}", e.getMessage());
            e.printStackTrace();
            response = CommonsProto.AuthResponse.newBuilder().build();
        }
        return response;
    }

    public CommonsProto.AuthResponse getUserById(int userId) {
        CommonsProto.AuthResponse.Builder responseBuilder = CommonsProto.AuthResponse.newBuilder();
        CommonsProto.AuthResponse response;
        User user;
        try {
            Optional<User> userFromDb = userDao.getById(userId);
            if (!userFromDb.isPresent()) {
                logger.info("no user present for id {}", userId);
                return CommonsProto.AuthResponse.newBuilder().build();
            }
            user = userFromDb.get();
            logger.info("found user having id {}", user.getId());
            responseBuilder.setPicUrl(user.getPicUrl());
            response = mapperUtil.buildAuthResponse(responseBuilder, user);
        } catch (Exception e) {
            logger.info("exception occurred while getting user for id {}", userId);
            e.printStackTrace();
            response = CommonsProto.AuthResponse.newBuilder().build();
        }
        return response;
    }
}
