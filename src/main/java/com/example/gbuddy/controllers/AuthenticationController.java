package com.example.gbuddy.controllers;

import com.example.gbuddy.dao.UserDao;
import com.example.gbuddy.exception.CustomException;
import com.example.gbuddy.models.*;
import com.example.gbuddy.protos.LoginSignupProto;
import com.example.gbuddy.service.validators.AuthenticationValidators;
import com.example.gbuddy.util.MapperUtil;
import com.google.protobuf.ByteString;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/auth")
public class AuthenticationController {

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationController.class);

    @Autowired
    private UserDao userDao;

    @Autowired
    private MapperUtil mapperUtil;

    @Autowired
    private AuthenticationValidators authenticationValidators;

    @PostMapping(value = "/signup")//, consumes = "application/x-protobuf", produces = "application/x-protobuf")
    public LoginSignupProto.SignupResponse signup(@Valid @RequestBody LoginSignupProto.SignupRequest userSignupRequest) {
        logger.info("starting signup process");
        User user = null;
        LoginSignupProto.LoginResponse.Builder responseBuilder = LoginSignupProto.LoginResponse.newBuilder();
        LoginSignupProto.SignupResponse response;
        try {
            List<String> validationMessage = authenticationValidators.validateSignupRequest(userSignupRequest);
            if(!validationMessage.isEmpty()) {
                logger.info("validation failed with {} issues", validationMessage.size());
                responseBuilder.setResponseMessage(String.join("|", validationMessage));
                responseBuilder.setResponseCode(HttpStatus.BAD_REQUEST.value());
                throw new CustomException(String.join("|", validationMessage));
            }
            logger.info("validation completed in {} sec");
            user = mapperUtil.getUserFromUserSignupRequest(userSignupRequest);
            logger.info("creating new user, {}", user);
            user = userDao.save(user);
            Blob image = user.getProfilePic().getUserImage();
            responseBuilder.setUserImage(ByteString.copyFrom(image.getBytes(1, (int)image.length())));
            responseBuilder.setResponseMessage("successfully created user");
            responseBuilder.setResponseCode(HttpStatus.OK.value());
        } catch (Exception e) {
            logger.info("exception occurred during signup process");
            e.printStackTrace();
        } finally {
            response = mapperUtil.buildSignUpResponse(responseBuilder, user);
            logger.info("user persisted in db and response built");
        }
        logger.info("sending response");
        return response;
    }

    @POST
    @Path("/login")
    @CrossOrigin
    @PostMapping(value = "/login")//, consumes = "application/x-protobuf", produces = "application/x-protobuf")
    public LoginSignupProto.LoginResponse login(@Valid @RequestBody LoginSignupProto.LoginRequest userLoginRequest) {
        LoginSignupProto.LoginResponse.Builder builder = LoginSignupProto.LoginResponse.newBuilder();
        LoginSignupProto.LoginResponse response;
        User user = null;
        logger.info("start or login process");
        try {
            List<String> validationMessage = authenticationValidators.validateLoginRequest(userLoginRequest);
            if(validationMessage.isEmpty()) {
                logger.info("validation failed with {} issues", validationMessage.size());
                builder.setResponseMessage(String.join("|", validationMessage));
                builder.setResponseCode(HttpStatus.BAD_REQUEST.value());
                throw new CustomException(String.join("|", validationMessage));
            }
            user = userDao.getByUserName(userLoginRequest.getUsername()).get();
            Blob image = user.getProfilePic().getUserImage();
            builder.setUserImage(ByteString.copyFrom(image.getBytes(1, (int)image.length())));
        } catch (Exception e) {
            logger.info("exception occured during login process");
            e.printStackTrace();
        } finally {
            response = mapperUtil.buildLoginResponse(builder, user);
        }
        return response;
    }

    @CrossOrigin
    @GetMapping("/id/{id}")
    public LoginSignupProto.LoginResponse getUserById(@PathVariable("id") List<Integer> userId) {
        logger.info("fetching deatails of user having id {}", userId);
        LoginSignupProto.LoginResponse.Builder responseBuilder = LoginSignupProto.LoginResponse.newBuilder();
        LoginSignupProto.LoginResponse response;
        User user = null;
        try {
            Optional<List<User>> users = userDao.getByUserIdIn(userId);
            if(!users.isPresent()) {
                logger.info("no user present for id {}", userId.get(0));
                return LoginSignupProto.LoginResponse.newBuilder()
                        .setResponseMessage("no user present for id " + userId)
                        .setResponseCode(HttpStatus.NO_CONTENT.value())
                        .build();
            }
            user = users.get().get(0);
            logger.info("found user having id {}", user.getUserId());
            Blob image = user.getProfilePic().getUserImage();
            responseBuilder.setUserImage(ByteString.copyFrom(image.getBytes(1, (int)image.length())));
        } catch (Exception e) {
            responseBuilder.setResponseMessage(e.getMessage())
                .setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
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

    private UserSignupResponse getUserResponse(String responseMessage, String responseStatus, int responseCode) {
        return new UserSignupResponse(responseMessage, responseStatus, responseCode);
    }
}
