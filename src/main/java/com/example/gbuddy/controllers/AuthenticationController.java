package com.example.gbuddy.controllers;

import com.example.gbuddy.models.protos.CommonsProto;
import com.example.gbuddy.models.protos.LoginSignupProto;
import com.example.gbuddy.service.AuthenticationService;
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
        CommonsProto.AuthResponse response = authenticationService.signup(userSignupRequest);
        return ResponseEntity.ok(JsonFormat.printer().print(response));
    }

    @POST
    @Path("/login")
    @CrossOrigin
    @PostMapping(value = "/login", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> login(@Valid @RequestBody LoginSignupProto.LoginRequest userLoginRequest) throws InvalidProtocolBufferException {
        CommonsProto.AuthResponse response = authenticationService.login(userLoginRequest);
        return ResponseEntity.ok(JsonFormat.printer().print(response));
    }

    @CrossOrigin
    @GetMapping(value = "/id/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getUserById(@PathVariable("id") int userId) throws InvalidProtocolBufferException {
        logger.info("fetching deatails of user having id {}", userId);
        CommonsProto.AuthResponse response = authenticationService.getUserById(userId);
        return ResponseEntity.ok(JsonFormat.printer().print(response));

    }

    @CrossOrigin
    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("authentication server is up and running");
    }
}
