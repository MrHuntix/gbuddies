package com.example.gbuddy.util;

import com.example.gbuddy.models.*;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

@Component
public class MapperUtil {
    private static final Logger logger = LoggerFactory.getLogger(MapperUtil.class);

    public Gym getGymFromRequest(GymRegisterRequest registerRequest) {
        Gym gym = new Gym();
        gym.setName(registerRequest.getName());
        gym.setWebsite(registerRequest.getWebsite());
        for (GymBranch branch : registerRequest.getBranches()) {
            Branch b = new Branch();
            b.setGymId(gym);
            b.setLocality(branch.getLocality());
            b.setCity(branch.getCity());
            b.setLatitude(branch.getLatitude());
            b.setLongitude(branch.getLongitude());
            b.setContact(branch.getContact());
            gym.getBranches().add(b);
        }
        logger.info("persisting gym {}", gym);
        return gym;
    }

    public boolean validateRequest(GymRegisterRequest registerRequest) {
        return StringUtils.isNotEmpty(registerRequest.getName()) && StringUtils.isNotEmpty(registerRequest.getWebsite()) && !CollectionUtils.isEmpty(registerRequest.getBranches());
    }

    public User getUserFromUserSignupRequest(UserSignupRequest userSignupRequest) {
        User user = new User();
        user.setUserName(userSignupRequest.getUserName());
        user.setEmailId(userSignupRequest.getEmailId());
        user.setMobileNo(userSignupRequest.getMobileNo());
        user.setPassword(userSignupRequest.getPassword());
        return user;
    }
}
