package com.example.gbuddy.service;

import com.example.gbuddy.dao.TokenDao;
import com.example.gbuddy.dao.UserDao;
import com.example.gbuddy.exception.CustomException;
import com.example.gbuddy.models.constants.ResponseMessageConstants;
import com.example.gbuddy.models.entities.Token;
import com.example.gbuddy.models.entities.User;
import com.example.gbuddy.models.protos.LoginSignupProto;
import com.example.gbuddy.models.response.ValidationResponse;
import com.example.gbuddy.service.validators.AuthenticationValidator;
import com.example.gbuddy.util.JwtUtil;
import com.example.gbuddy.util.MapperUtil;
import com.google.protobuf.ByteString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.sql.Blob;
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

    public LoginSignupProto.LoginResponse login(LoginSignupProto.LoginRequest userLoginRequest) {
        LoginSignupProto.LoginResponse.Builder builder = LoginSignupProto.LoginResponse.newBuilder();
        LoginSignupProto.LoginResponse response;

        logger.info("start or login process");
        try {
            ValidationResponse<String, User> validationResponse = authenticationValidator.validateLoginRequest(userLoginRequest);
            if (!validationResponse.getValidationMessage().isEmpty()) {
                logger.info("validation failed with {} issues", validationResponse.getValidationMessage().size());
                throw new CustomException(String.join("|", validationResponse.getValidationMessage()));
            }
            User user = validationResponse.getValidObject();
            Blob image = user.getProfilePic().getUserImage();
            builder.setUserImage(ByteString.copyFrom(image.getBytes(1, Math.toIntExact(image.length()))));
            builder.setResponseMessage(ResponseMessageConstants.LOGIN_SUCCESSFULL.getMessage())
                    .setResponseCode(HttpStatus.OK.value());
            response = mapperUtil.buildLoginResponse(builder, user);
        } catch (Exception e) {
            logger.info("exception occurred during login process {}", e.getMessage());
            e.printStackTrace();
            builder.setResponseMessage(ResponseMessageConstants.INVALID_LOGIN_CREDENTIALS.getMessage())
                    .setResponseCode(HttpStatus.BAD_REQUEST.value());
            response = builder.build();
        }
        return response;
    }

    public LoginSignupProto.SignupResponse signup(LoginSignupProto.SignupRequest userSignupRequest) {
        LoginSignupProto.LoginResponse.Builder responseBuilder = LoginSignupProto.LoginResponse.newBuilder();
        LoginSignupProto.SignupResponse response;
        try {
            ValidationResponse<String, User> validationResponse = authenticationValidator.validateSignupRequest(userSignupRequest);
            if (!validationResponse.getValidationMessage().isEmpty()) {
                logger.info("validation failed with {} issues", validationResponse.getValidationMessage().size());
                throw new CustomException(String.join("|", validationResponse.getValidationMessage()));
            }
            String token = jwtUtil.generateToken(userSignupRequest.getUserName());
            Token createdToken = new Token();
            createdToken.setActive(1);
            createdToken.setToken(token);
            User user = mapperUtil.getUserFromUserSignupRequest(userSignupRequest);
            logger.info("creating new user, {}", user);
            user = userDao.save(user);
            logger.info("user {} created", user.getUserId());
            createdToken.setUserId(user.getUserId());
            createdToken = tokenDao.save(createdToken);
            logger.info("token created for {}", createdToken.getId());
            Blob image = user.getProfilePic().getUserImage();
            responseBuilder.setUserImage(ByteString.copyFrom(image.getBytes(1, Math.toIntExact(image.length()))));
            responseBuilder.setResponseMessage(ResponseMessageConstants.SIGNUP_SUCCESSFULL.getMessage())
                    .setResponseCode(HttpStatus.OK.value());
            responseBuilder.setToken(token);
            response = mapperUtil.buildSignUpResponse(responseBuilder, user);
        } catch (Exception e) {
            logger.info("exception occurred during signup process {}", e.getMessage());
            e.printStackTrace();
            responseBuilder.setResponseMessage(ResponseMessageConstants.SIGNUP_UNSUCCESSFULL.getMessage())
                    .setResponseCode(HttpStatus.BAD_REQUEST.value());
            response = LoginSignupProto.SignupResponse.newBuilder().setResponse(responseBuilder.build()).build();
        }
        return response;
    }

    public LoginSignupProto.LoginResponse getUserById(int userId) {
        LoginSignupProto.LoginResponse.Builder responseBuilder = LoginSignupProto.LoginResponse.newBuilder();
        LoginSignupProto.LoginResponse response;
        User user;
        try {
            Optional<User> userFromDb = userDao.getByUserId(userId);
            if (!userFromDb.isPresent()) {
                logger.info("no user present for id {}", userId);
                return LoginSignupProto.LoginResponse.newBuilder()
                        .setResponseMessage(ResponseMessageConstants.USER_NOT_PRESENT.getMessage())
                        .setResponseCode(HttpStatus.NO_CONTENT.value())
                        .build();
            }
            user = userFromDb.get();
            logger.info("found user having id {}", user.getUserId());
            Blob image = user.getProfilePic().getUserImage();
            responseBuilder.setUserImage(ByteString.copyFrom(image.getBytes(1, Math.toIntExact(image.length()))))
                    .setResponseMessage("user found in db")
                    .setResponseCode(HttpStatus.OK.value());
            response = mapperUtil.buildLoginResponse(responseBuilder, user);
        } catch (Exception e) {
            logger.info("exception occurred while getting user for id {}", userId);
            e.printStackTrace();
            response = responseBuilder.setResponseMessage(ResponseMessageConstants.USER_LOOKUP_FAILED.getMessage())
                    .setResponseCode(HttpStatus.BAD_REQUEST.value()).build();
        }
        return response;
    }
}
