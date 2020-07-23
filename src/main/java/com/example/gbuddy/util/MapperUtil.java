package com.example.gbuddy.util;

import com.example.gbuddy.dao.ProfilePicDao;
import com.example.gbuddy.models.*;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.sql.rowset.serial.SerialBlob;
import java.io.IOException;
import java.sql.Blob;
import java.sql.SQLException;

@Component
public class MapperUtil {
    private static final Logger logger = LoggerFactory.getLogger(MapperUtil.class);

    @Autowired
    private ProfilePicDao profilePicDao;

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

    public User getUserFromUserSignupRequest(UserSignupRequest userSignupRequest) throws SQLException, IOException {
        ProfilePic profilePic = new ProfilePic();
        profilePic.setUserImage(new SerialBlob(userSignupRequest.getImage().getBytes()));
        User user = new User();
        user.setUserName(userSignupRequest.getUserName());
        user.setEmailId(userSignupRequest.getEmailId());
        user.setMobileNo(userSignupRequest.getMobileNo());
        user.setPassword(userSignupRequest.getPassword());
        user.setAbout(userSignupRequest.getAbout());
        user.setRoles("ROLE_ADMIN,ROLE_CLIENT");
        user.setProfilePic(profilePic);
        profilePic.setUser(user);
        //profilePicDao.save(profilePic);
        return user;
    }
}
