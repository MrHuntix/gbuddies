package com.example.gbuddy.controllers;

import com.example.gbuddy.dao.TokenDao;
import com.example.gbuddy.dao.UserDao;
import com.example.gbuddy.models.protos.LoginSignupProto;
import com.example.gbuddy.service.AuthenticationService;
import com.example.gbuddy.service.validators.AuthenticationValidator;
import com.example.gbuddy.util.JwtUtil;
import com.example.gbuddy.util.MapperUtil;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

@RestController
@RequestMapping("/auth")
public class AuthenticationController {

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationController.class);

    @Autowired
    private AuthenticationService authenticationService;

    @PostMapping(value = "/signup", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> signup(@Valid @RequestBody LoginSignupProto.SignupRequest userSignupRequest) throws InvalidProtocolBufferException {
        logger.info("starting signup process");
        LoginSignupProto.SignupResponse response = authenticationService.signup(userSignupRequest);
        return ResponseEntity.status(response.getResponse().getResponseCode()).body(JsonFormat.printer().print(response.getResponse()));
    }

    @POST
    @Path("/login")
    @CrossOrigin
    @PostMapping(value = "/login", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> login(@Valid @RequestBody LoginSignupProto.LoginRequest userLoginRequest) throws InvalidProtocolBufferException {
        LoginSignupProto.LoginResponse response = authenticationService.login(userLoginRequest);
        return ResponseEntity.status(response.getResponseCode()).body(JsonFormat.printer().print(response));
    }

    @CrossOrigin
    @GetMapping(value = "/id/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getUserById(@PathVariable("id") int userId) throws InvalidProtocolBufferException {
        logger.info("fetching deatails of user having id {}", userId);
        LoginSignupProto.LoginResponse response = authenticationService.getUserById(userId);
        return ResponseEntity.status(response.getResponseCode()).body(JsonFormat.printer().print(response));

    }

    @CrossOrigin
    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("authentication server is up and running");
    }
}
