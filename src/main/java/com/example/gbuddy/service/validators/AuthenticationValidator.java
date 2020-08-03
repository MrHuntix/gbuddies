package com.example.gbuddy.service.validators;

import com.example.gbuddy.dao.UserDao;
import com.example.gbuddy.models.entities.User;
import com.example.gbuddy.models.constants.ValidationInfoEnum;
import com.example.gbuddy.models.protos.LoginSignupProto;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class AuthenticationValidator {
    private static final Logger LOG = LoggerFactory.getLogger(AuthenticationValidator.class);

    @Autowired
    private UserDao userDao;

    public List<String> validateSignupRequest(LoginSignupProto.SignupRequest userSignupRequest) {
        LOG.info("starting validation for signup request");
        List<String> validationMessage = new ArrayList<>();
        if (StringUtils.isEmpty(userSignupRequest.getUserName())) {
            validationMessage.add(ValidationInfoEnum.EMPTY_INVALID_USERNAME.getValidationInfoValue());
        }
        if (StringUtils.isEmpty(userSignupRequest.getEmailId())) {
            validationMessage.add(ValidationInfoEnum.EMPTY_INVALID_EMAIL.getValidationInfoValue());
        }
        if (StringUtils.isEmpty(userSignupRequest.getMobileNo())) {
            validationMessage.add(ValidationInfoEnum.EMPTY_INVALID_MOBILE.getValidationInfoValue());
        }
        if (StringUtils.isEmpty(userSignupRequest.getPassword())) {
            validationMessage.add(ValidationInfoEnum.EMPTY_INVALID_PASSWORD.getValidationInfoValue());
        }
        if (StringUtils.isEmpty(userSignupRequest.getRoles().name())) {
            validationMessage.add(ValidationInfoEnum.EMPTY_INVALID_ROLES.getValidationInfoValue());
        }
        if (StringUtils.isEmpty(userSignupRequest.getAbout())) {
            validationMessage.add(ValidationInfoEnum.EMPTY_INVALID_ABOUT.getValidationInfoValue());
        }
        if (userSignupRequest.getUserImage().isEmpty()) {
            validationMessage.add(ValidationInfoEnum.EMPTY_INVALID_IMAGE.getValidationInfoValue());
        }
        usernameAlreadyExists(userSignupRequest.getUserName(), validationMessage);
        return validationMessage;
    }

    private void usernameAlreadyExists(String userName, List<String> validationMessage) {
        if (StringUtils.isNotEmpty(userName) && userDao.getByUserName(userName).isPresent()) {
            validationMessage.add(String.format(ValidationInfoEnum.USERNAME_ALREADY_EXISTS.getValidationInfoValue(), userName));
        }
    }

    public List<String> validateLoginRequest(LoginSignupProto.LoginRequest userLoginRequest) {
        LOG.info("starting validation for login request");
        List<String> validationMessage = new ArrayList<>();
        if (StringUtils.isNotEmpty(userLoginRequest.getUsername())) {
            validationMessage.add(ValidationInfoEnum.EMPTY_INVALID_USERNAME.getValidationInfoValue());
        }
        if (StringUtils.isNotEmpty(userLoginRequest.getPassword())) {
            validationMessage.add(ValidationInfoEnum.EMPTY_INVALID_PASSWORD.getValidationInfoValue());
        }
        isCredentialsCorrect(userLoginRequest.getUsername(), userLoginRequest.getPassword(), validationMessage);
        return validationMessage;
    }

    private void isCredentialsCorrect(String username, String password, List<String> validationMessage) {
        Optional<User> user = userDao.getByUserName(username);
        if (!user.isPresent()) {
            validationMessage.add(String.format(ValidationInfoEnum.USER_NOT_FOUND_IN_DB.getValidationInfoValue(), username));
        }
        if (!password.equals(user.get().getPassword())) {
            validationMessage.add(ValidationInfoEnum.INVALID_LOGIN_CREDENTALS.getValidationInfoValue());
        }
    }
}
