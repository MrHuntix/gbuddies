package com.example.gbuddy.controllers;

import com.example.gbuddy.dao.GymDao;
import com.example.gbuddy.models.Gym;
import com.example.gbuddy.models.GymRegisterRequest;
import com.example.gbuddy.models.GymResponse;
import com.example.gbuddy.util.MapperUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/gym")
public class GymController {
    private static final Logger logger = LoggerFactory.getLogger(GymController.class);

    @Autowired
    private GymDao gymDao;

    @Autowired
    private MapperUtil mapperUtil;

    @CrossOrigin
    @PostMapping(name = "/register/gym")
    public ResponseEntity registerGym(@RequestBody GymRegisterRequest gymRegisterRequest) {
        logger.info("registering gym: {}", gymRegisterRequest);
        if(mapperUtil.validateRequest(gymRegisterRequest)) {
            logger.info("the request to add a new gym could not be processed");
            return new ResponseEntity<>(new GymResponse("The request to register gym cannot be processed as the entered details is invalid",
                    HttpStatus.UNPROCESSABLE_ENTITY.getReasonPhrase(), HttpStatus.UNPROCESSABLE_ENTITY.value(),
                    GymResponse.getDefaultGyms()), HttpStatus.UNPROCESSABLE_ENTITY);
        }
        logger.info("processing request to add new gym");
        Gym g = mapperUtil.getGymFromRequest(gymRegisterRequest);

        Gym gym = gymDao.save(g);
        return ResponseEntity.ok(
                new GymResponse("Gym successfully registered into our systems",
                        HttpStatus.OK.getReasonPhrase(), HttpStatus.OK.value(),
                        Collections.singletonList(gym))
        );
    }

    @PostMapping("/fetch")
    public ResponseEntity fetch() {
        List<Gym> gyms = gymDao.findAll();
        if(gyms==null || gyms.isEmpty())
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        return ResponseEntity.ok(new GymResponse("message", "status", 200, gyms));
    }

    @CrossOrigin
    @GetMapping("/test/gym")
    public ResponseEntity test() {
        logger.info("*******");
        return ResponseEntity.ok("test string");
    }
}

