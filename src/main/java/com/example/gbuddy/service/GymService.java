package com.example.gbuddy.service;

import com.example.gbuddy.dao.BranchDao;
import com.example.gbuddy.dao.GymDao;
import com.example.gbuddy.exception.CustomException;
import com.example.gbuddy.models.constants.ResponseMessageConstants;
import com.example.gbuddy.models.entities.Branch;
import com.example.gbuddy.models.entities.Gym;
import com.example.gbuddy.models.protos.GymProto;
import com.example.gbuddy.service.validators.GymValidator;
import com.example.gbuddy.util.MapperUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@Service
public class GymService {
    private static final Logger logger = LoggerFactory.getLogger(GymService.class);

    @Autowired
    private GymDao gymDao;

    @Autowired
    private BranchDao branchDao;

    @Autowired
    private MapperUtil mapperUtil;

    @Autowired
    private GymValidator gymValidator;

    public GymProto.RegisterResponse registerGym(@RequestBody GymProto.Gym request) {
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

    public GymProto.FetchResponse fetch() {
        logger.info("start of fetch process");
        GymProto.FetchResponse.Builder builder = GymProto.FetchResponse.newBuilder();
        GymProto.FetchResponse response;
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
            response = builder.setMessage(ResponseMessageConstants.FETCH_FAILED.getMessage())
                    .setResponseCode(HttpStatus.BAD_REQUEST.value()).build();
            logger.info("exception during fetch");
            e.printStackTrace();
        }
        logger.info("sending fetch response");
        return response;
    }

    public GymProto.CoordinateResponse coordinates(int branchId) {
        GymProto.CoordinateResponse.Builder builder = GymProto.CoordinateResponse.newBuilder();
        GymProto.CoordinateResponse response;
        logger.info("getting coordinates for branch id: {}", branchId);
        try {
            Branch branch = branchDao.findById(branchId).orElse(null);
            if (branch == null) {
                logger.info("no branch found for having branch id: {}", branchId);
                throw new CustomException(ResponseMessageConstants.COORDINATES_ERROR.getMessage());
            }
            logger.info("found branch: {}", branch);
            response = builder.setResponseCode(HttpStatus.OK.value())
                    .setMessage(ResponseMessageConstants.COORDINATES_FOUND.getMessage())
                    .setLatitude(branch.getLatitude())
                    .setLongitude(branch.getLongitude()).build();
            logger.info("completed building coordinate response");
        } catch (Exception e) {
            logger.info("exception occurred while fetching coordinates for branch {}", branchId);
            e.printStackTrace();
            response = builder.setMessage(e.getMessage())
                    .setResponseCode(HttpStatus.BAD_REQUEST.value()).build();
        }
        logger.info("sending coordinate response");
        return response;
    }
}
