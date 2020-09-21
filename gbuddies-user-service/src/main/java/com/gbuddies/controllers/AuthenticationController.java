package com.gbuddies.controllers;

import com.gbuddies.constants.ResponseMessageConstants;
import com.gbuddies.dao.UserDao;
import com.gbuddies.exception.CustomException;
import com.gbuddies.models.User;
import com.gbuddies.protos.LoginSignupProto;
import com.gbuddies.utils.AuthenticationMapperUtil;
import com.gbuddies.validators.AuthenticationValidator;
import com.google.protobuf.ByteString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import java.sql.Blob;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/auth")
public class AuthenticationController {

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationController.class);

    @Autowired
    private UserDao userDao;

    @Autowired
    private AuthenticationMapperUtil mapperUtil;

    @Autowired
    private AuthenticationValidator authenticationValidator;

    @PostMapping(value = "/signup")
    public LoginSignupProto.SignupResponse signup(@Valid @RequestBody LoginSignupProto.SignupRequest userSignupRequest) {
        logger.info("starting signup process");
        User user = null;
        LoginSignupProto.LoginResponse.Builder responseBuilder = LoginSignupProto.LoginResponse.newBuilder();
        LoginSignupProto.SignupResponse response = null;
        try {
            List<String> validationMessage = authenticationValidator.validateSignupRequest(userSignupRequest);
            if (!validationMessage.isEmpty()) {
                logger.info("validation failed with {} issues", validationMessage.size());
                throw new CustomException(String.join("|", validationMessage));
            }
            logger.info("validation completed in {} sec");
            user = mapperUtil.getUserFromUserSignupRequest(userSignupRequest);
            logger.info("creating new user, {}", user);
            user = userDao.save(user);
            logger.info("user is saved in db");
            Blob image = user.getProfilePic().getUserImage();
            responseBuilder.setUserImage(ByteString.copyFrom(image.getBytes(1, Math.toIntExact(image.length()))));
            responseBuilder.setResponseMessage(ResponseMessageConstants.SIGNUP_SUCCESSFULL.getMessage())
                    .setResponseCode(HttpStatus.OK.value());
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

    @POST
    @Path("/login")
    @CrossOrigin
    @PostMapping(value = "/login")
    public LoginSignupProto.LoginResponse login(@Valid @RequestBody LoginSignupProto.LoginRequest userLoginRequest) {
        LoginSignupProto.LoginResponse.Builder builder = LoginSignupProto.LoginResponse.newBuilder();
        LoginSignupProto.LoginResponse response = null;
        User user = null;
        logger.info("start or login process");
        try {
            List<String> validationMessage = authenticationValidator.validateLoginRequest(userLoginRequest);
            if (!validationMessage.isEmpty()) {
                logger.info("validation failed with {} issues", validationMessage.size());
                throw new CustomException(String.join("|", validationMessage));
            }
            user = userDao.getByUserName(userLoginRequest.getUsername()).get();
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

    @CrossOrigin
    @GetMapping("/id/{id}")
    public LoginSignupProto.LoginResponse getUserById(@PathVariable("id") int userId) {
        logger.info("fetching deatails of user having id {}", userId);
        LoginSignupProto.LoginResponse.Builder responseBuilder = LoginSignupProto.LoginResponse.newBuilder();
        LoginSignupProto.LoginResponse response;
        User user = null;
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
        } catch (Exception e) {
            logger.info("exception occurred while getting user for id {}");
            e.printStackTrace();
            responseBuilder.setResponseMessage(ResponseMessageConstants.USER_LOOKUP_FAILED.getMessage())
                    .setResponseCode(HttpStatus.BAD_REQUEST.value());
        } finally {
            response = mapperUtil.buildLoginResponse(responseBuilder, user);
        }
        return response;
    }

    @CrossOrigin
    @GetMapping("/test")
    public ResponseEntity test() {
        return ResponseEntity.ok("authentication server is up and running");
    }
}
