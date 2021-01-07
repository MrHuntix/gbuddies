package com.example.gbuddy.controllers;

import com.example.gbuddy.dao.BranchDao;
import com.example.gbuddy.dao.GymDao;
import com.example.gbuddy.exception.CustomException;
import com.example.gbuddy.models.constants.ResponseMessageConstants;
import com.example.gbuddy.models.entities.Branch;
import com.example.gbuddy.models.entities.Gym;
import com.example.gbuddy.models.protos.GymProto;
import com.example.gbuddy.service.GymService;
import com.example.gbuddy.service.validators.GymValidator;
import com.example.gbuddy.util.MapperUtil;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/gym")
public class GymController {
    private static final Logger logger = LoggerFactory.getLogger(GymController.class);

    @Autowired
    private GymService gymService;

    @CrossOrigin
    @PostMapping(value = "/register/gym", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> registerGym(@RequestBody GymProto.Gym request) throws InvalidProtocolBufferException {
        GymProto.RegisterResponse response = gymService.registerGym(request);
        return ResponseEntity.status(response.getResponseCode()).body(JsonFormat.printer().print(response));
    }

    @GetMapping(value = "/fetch", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> fetch() throws InvalidProtocolBufferException {
        GymProto.FetchResponse response = gymService.fetch();
        return ResponseEntity.status(response.getResponseCode()).body(JsonFormat.printer().print(response));
    }

    @GetMapping(value = "/coordinates/{branchId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> coordinates(@PathVariable("branchId") int branchId) throws InvalidProtocolBufferException {
        GymProto.CoordinateResponse response = gymService.coordinates(branchId);
        return ResponseEntity.status(response.getResponseCode()).body(JsonFormat.printer().print(response));

    }

    @GetMapping(value = "/test/gym", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("gym service is up and running");
    }


}

