package com.example.gbuddy.controllers;

import com.example.gbuddy.dao.UserDao;
import com.example.gbuddy.models.*;
import com.example.gbuddy.util.MapperUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
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

    @CrossOrigin
    @PostMapping("/signup")
    public ResponseEntity<UserSignupResponse> signup(@Valid @ModelAttribute UserSignupRequest userSignupRequest) {
        logger.info("starting signup process");
        UserSignupResponse response;
        if (userDao.getByUserName(userSignupRequest.getUserName()).isPresent()) {
            logger.info("username {} already taken", userSignupRequest.getUserName());
            return new ResponseEntity<>(getUserResponse(userSignupRequest.getUserName() + " already taken",
                    HttpStatus.OK.getReasonPhrase(), HttpStatus.OK.value()), HttpStatus.OK);
        }
        User user = null;
        try {
            user = mapperUtil.getUserFromUserSignupRequest(userSignupRequest);
            logger.info("creating new user, {}", user);
            user = userDao.save(user);
        } catch (Exception e) {
            logger.info("failed to create user object");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        response = getUserResponse(user.getUserName() + " created successfully",
                HttpStatus.OK.getReasonPhrase(), HttpStatus.OK.value());
        logger.info("completed signup process");
        return ResponseEntity.ok(response);
    }

    @CrossOrigin
    @PostMapping("/login")
    public ResponseEntity login(@Valid @RequestBody UserLoginRequest userLoginRequest) {
        logger.info("start or login process");
        Optional<User> user = userDao.getByUserName(userLoginRequest.getUserName());
        if(!user.isPresent()) {
            logger.info("no user present for username {}", userLoginRequest.getUserName());
            return ResponseEntity.noContent().build();
        }
        if(!userLoginRequest.getPassword().equalsIgnoreCase(user.get().getPassword())) {
            logger.info("wrong password entered");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return ResponseEntity.ok(new UserLoginResponse(user.get().getUserId(), "authorized", "success", 200,
                "jwt_token_undefined", user.get().getUserName()));
    }

    @CrossOrigin
    @GetMapping("/id/{id}")
    public ResponseEntity<User> getUserById(@PathVariable("id") List<Integer> userId) {
        logger.info("fetching deatails of user having id {}", userId);
        Optional<List<User>> users = userDao.getByUserIdIn(userId);
        if(!users.isPresent()) {
            logger.info("no user present for id {}", userId.get(0));
            ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }
        User user = users.get().get(0);
        logger.info("found user having id {}", user.getUserId());
        return ResponseEntity.ok(user);
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
