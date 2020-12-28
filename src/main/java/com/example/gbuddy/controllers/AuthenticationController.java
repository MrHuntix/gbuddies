package com.example.gbuddy.controllers;

import com.example.gbuddy.dao.UserDao;
import com.example.gbuddy.exception.CustomException;
import com.example.gbuddy.models.constants.ResponseMessageConstants;
import com.example.gbuddy.models.entities.User;
import com.example.gbuddy.models.protos.LoginSignupProto;
import com.example.gbuddy.service.validators.AuthenticationValidator;
import com.example.gbuddy.util.JwtUtil;
import com.example.gbuddy.util.MapperUtil;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import java.sql.Blob;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@RestController
@RequestMapping("/auth")
public class AuthenticationController {

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationController.class);

    @Autowired
    private UserDao userDao;

    @Autowired
    private MapperUtil mapperUtil;

    @Autowired
    private AuthenticationValidator authenticationValidator;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping(value = "/signup", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> signup(@Valid @RequestBody LoginSignupProto.SignupRequest userSignupRequest) throws InvalidProtocolBufferException {
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
            responseBuilder.setToken(jwtUtil.generateToken(userSignupRequest.getUserName()));
            response = mapperUtil.buildSignUpResponse(responseBuilder, user);
        } catch (Exception e) {
            logger.info("exception occurred during signup process {}", e.getMessage());
            e.printStackTrace();
            responseBuilder.setResponseMessage(ResponseMessageConstants.SIGNUP_UNSUCCESSFULL.getMessage())
                    .setResponseCode(HttpStatus.BAD_REQUEST.value());
            response = LoginSignupProto.SignupResponse.newBuilder().setResponse(responseBuilder.build()).build();
        }
        return ResponseEntity.ok(JsonFormat.printer().print(response));
    }

    @POST
    @Path("/login")
    @CrossOrigin
    @PostMapping(value = "/login", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> login(@Valid @RequestBody LoginSignupProto.LoginRequest userLoginRequest) throws InvalidProtocolBufferException {
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
        return ResponseEntity.ok(JsonFormat.printer().print(response));
    }

    @CrossOrigin
    @GetMapping(value = "/id/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getUserById(@PathVariable("id") int userId) throws InvalidProtocolBufferException {
        logger.info("fetching deatails of user having id {}", userId);
        LoginSignupProto.LoginResponse.Builder responseBuilder = LoginSignupProto.LoginResponse.newBuilder();
        LoginSignupProto.LoginResponse response = null;
        User user = null;
        try {
            Optional<User> userFromDb = userDao.getByUserId(userId);
            if (!userFromDb.isPresent()) {
                logger.info("no user present for id {}", userId);
                return ResponseEntity.ok(JsonFormat.printer().print(LoginSignupProto.LoginResponse.newBuilder()
                        .setResponseMessage(ResponseMessageConstants.USER_NOT_PRESENT.getMessage())
                        .setResponseCode(HttpStatus.NO_CONTENT.value())
                        .build()));
            }
            user = userFromDb.get();
            logger.info("found user having id {}", user.getUserId());
            Blob image = user.getProfilePic().getUserImage();
            responseBuilder.setUserImage(ByteString.copyFrom(image.getBytes(1, Math.toIntExact(image.length()))))
                    .setResponseMessage("user found in db")
                    .setResponseCode(HttpStatus.OK.value());
            response = mapperUtil.buildLoginResponse(responseBuilder, user);
        } catch (Exception e) {
            logger.info("exception occurred while getting user for id {}");
            e.printStackTrace();
            responseBuilder.setResponseMessage(ResponseMessageConstants.USER_LOOKUP_FAILED.getMessage())
                    .setResponseCode(HttpStatus.BAD_REQUEST.value());
        }
        return Objects.isNull(response)?ResponseEntity.ok(ResponseMessageConstants.USER_LOOKUP_FAILED.getMessage()):ResponseEntity.ok(JsonFormat.printer().print(response));

    }

    @CrossOrigin
    @GetMapping("/test")
    public ResponseEntity test() {
        return ResponseEntity.ok("authentication server is up and running");
    }
}
