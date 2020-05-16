package com.example.gbuddy.controllers;

import com.example.gbuddy.dao.BranchDao;
import com.example.gbuddy.dao.GymDao;
import com.example.gbuddy.models.Branch;
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
import java.util.Optional;

@RestController
@RequestMapping("/gym")
public class GymController {
    private static final Logger logger = LoggerFactory.getLogger(GymController.class);

    @Autowired
    private GymDao gymDao;

    @Autowired
    private BranchDao branchDao;

    @Autowired
    private MapperUtil mapperUtil;

    @CrossOrigin
    @PostMapping(name = "/register/gym")
    public ResponseEntity registerGym(@RequestBody GymRegisterRequest gymRegisterRequest) {
        logger.info("start of gym registration process");
        if(mapperUtil.validateRequest(gymRegisterRequest)) {
            logger.info("the request to add a new gym could not be processed");
            return new ResponseEntity<>(new GymResponse("The request to register gym cannot be processed as the entered details is invalid",
                    HttpStatus.UNPROCESSABLE_ENTITY.getReasonPhrase(), HttpStatus.UNPROCESSABLE_ENTITY.value(),
                    GymResponse.getDefaultGyms()), HttpStatus.UNPROCESSABLE_ENTITY);
        }
        logger.info("gym details validated. Processing request to add new gym");
        Gym g = mapperUtil.getGymFromRequest(gymRegisterRequest);
        Gym gym = gymDao.save(g);
        logger.info("gym created with id: {}", gym.getId());
        return ResponseEntity.ok(
                new GymResponse("Gym successfully registered into our systems",
                        HttpStatus.OK.getReasonPhrase(), HttpStatus.OK.value(),
                        Collections.singletonList(gym))
        );
    }

    @PostMapping("/fetch")
    public ResponseEntity fetch() {
        logger.info("start of fetch process");
        List<Gym> gyms = gymDao.findAll();
        if(gyms==null || gyms.isEmpty()) {
            logger.info("fetch process did not find any gyms");
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }
        logger.info("fetch process completed. Found {} gyms", gyms.size());
        return ResponseEntity.ok(new GymResponse("message", "status", 200, gyms));
    }

    @CrossOrigin
    @GetMapping("/test/gym")
    public ResponseEntity test() {
        return ResponseEntity.ok("gym service is up and running");
    }

    @GetMapping("/coordinates/{branchId}")
    public ResponseEntity coordinates(@PathVariable("branchId") int branchId) {
        logger.info("getting coordinates for branch id: {}", branchId);
        Optional<Branch> branch = branchDao.findById(branchId);
        if (!branch.isPresent()) {
            logger.info("no branch found for having branch id: {}", branchId);
        }
        logger.info("found branch: {}", branch.get());
        return ResponseEntity.ok(branch.get());
    }
}

