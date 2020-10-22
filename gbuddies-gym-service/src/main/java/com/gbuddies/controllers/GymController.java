package com.gbuddies.controllers;

import com.gbuddies.constants.ResponseMessageConstants;
import com.gbuddies.dao.BranchDao;
import com.gbuddies.dao.GymDao;
import com.gbuddies.exception.CustomException;
import com.gbuddies.models.Branch;
import com.gbuddies.models.Gym;
import com.gbuddies.protos.GymProto;
import com.gbuddies.util.MapperUtil;
import com.gbuddies.validators.GymValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("gym")
public class GymController {
    private static final Logger logger = LoggerFactory.getLogger(GymController.class);

    @Autowired
    private GymDao gymDao;

    @Autowired
    private BranchDao branchDao;

    @Autowired
    private MapperUtil mapperUtil;

    @Autowired
    private GymValidator gymValidator;

    @CrossOrigin
    @PostMapping(path = "/register/gym")
    public GymProto.RegisterResponse registerGym(@RequestBody GymProto.Gym request) {
        logger.info("start of gym registration process");
        GymProto.RegisterResponse.Builder builder = GymProto.RegisterResponse.newBuilder();
        try {
            List<String> validationMessage = gymValidator.validateGym(request);
            if (!validationMessage.isEmpty()) {
                logger.info("validation failed with {} issues", validationMessage.size());
                throw new CustomException(String.join("|", validationMessage));
            }
            logger.info("gym details validated. Processing request to add new gym");
            Gym g = mapperUtil.getGymFromRequest(request);
            Gym gym = gymDao.save(g);
            logger.info("gym created with id: {}", gym.getId());
            builder.setMessage(String.format(ResponseMessageConstants.GYM_CREATED.getMessage(), gym.getId()))
                    .setResponseCode(HttpStatus.OK.value());
            logger.info("completed building response for gym registration");
        } catch (Exception e) {
            logger.info("exception occurred during gym registration");
            e.printStackTrace();
            builder.setMessage(ResponseMessageConstants.FAILED_TO_CREATE_GYM.getMessage())
                    .setResponseCode(HttpStatus.BAD_REQUEST.value());
        }
        logger.info("sending register response");
        return builder.build();
    }

    @PostMapping(path = "/fetch")
    public GymProto.FetchResponse fetch() {
        logger.info("start of fetch process");
        GymProto.FetchResponse.Builder builder = GymProto.FetchResponse.newBuilder();
        GymProto.FetchResponse response = null;
        try {
            List<Gym> gyms = gymDao.findAll();
            if (CollectionUtils.isEmpty(gyms)) {
                logger.info("fetch process did not find any gyms");
                return GymProto.FetchResponse.newBuilder()
                        .setGym(0, GymProto.Gym.getDefaultInstance())
                        .setMessage("there are no gyms in the system")
                        .setResponseCode(HttpStatus.NO_CONTENT.value())
                        .build();
            }
            logger.info("fetch process completed. Found {} gyms", gyms.size());
            builder.setMessage(String.format(ResponseMessageConstants.FETCH_COMPLETED.getMessage(), gyms.size()))
                    .setResponseCode(HttpStatus.OK.value());
            response = mapperUtil.getResponseFromEntity(gyms, builder);
            logger.info("completed building fetch response");
        } catch (Exception e) {
            logger.info("exception occurred during fetch process");
            e.printStackTrace();
            builder.setMessage(ResponseMessageConstants.FETCH_FAILED.getMessage())
                    .setResponseCode(HttpStatus.BAD_REQUEST.value());
            logger.info("exception during fetch");
            e.printStackTrace();
        }
        logger.info("sending fetch response");
        return response;
    }


    @GetMapping(path = "/coordinates/{branchId}")
    public GymProto.CoordinateResponse coordinates(@PathVariable("branchId") int branchId) {
        GymProto.CoordinateResponse.Builder builder = GymProto.CoordinateResponse.newBuilder();
        logger.info("getting coordinates for branch id: {}", branchId);
        try {
            Branch branch = branchDao.findById(branchId).orElse(null);
            if (branch == null) {
                logger.info("no branch found for having branch id: {}", branchId);
                throw new CustomException(ResponseMessageConstants.COORDINATES_ERROR.getMessage());
            }
            logger.info("found branch: {}", branch);
            builder.setResponseCode(HttpStatus.OK.value())
                    .setMessage(ResponseMessageConstants.COORDINATES_FOUND.getMessage())
                    .setLatitude(branch.getLatitude())
                    .setLongitude(branch.getLongitude());
            logger.info("completed building coordinate response");
        } catch (Exception e) {
            logger.info("exception occurred while fetching coordinates for branch {}", branchId);
            e.printStackTrace();
            builder.setMessage(ResponseMessageConstants.COORDINATES_ERROR.getMessage())
                    .setResponseCode(HttpStatus.BAD_REQUEST.value());
            logger.info("exception during coordinate request");
            e.printStackTrace();
        }
        logger.info("sending coordinate response");
        return builder.build();
    }

    @GetMapping(path = "/up")
    public ResponseEntity test() {
        return ResponseEntity.ok("gym service is up and running");
    }
}

