package com.example.gbuddy.service.validators;

import com.example.gbuddy.dao.UserDao;
import com.example.gbuddy.models.constants.ValidationInfoEnum;
import com.example.gbuddy.models.entities.User;
import com.example.gbuddy.models.protos.LoginSignupProto;
import com.example.gbuddy.models.response.ValidationResponse;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Optional;

@Service
public class AuthenticationValidator {
    private static final Logger LOG = LoggerFactory.getLogger(AuthenticationValidator.class);

    @Autowired
    private UserDao userDao;

    public ValidationResponse<String, User> validateSignupRequest(LoginSignupProto.SignupRequest userSignupRequest) {
        LOG.info("starting validation for signup request");
        ValidationResponse<String, User> validationResponse = new ValidationResponse<>(new ArrayList<>());
        if (StringUtils.isEmpty(userSignupRequest.getName())) {
            validationResponse.getValidationMessage().add(ValidationInfoEnum.EMPTY_USERNAME.getValidationInfoValue());
        }
        if (StringUtils.isEmpty(userSignupRequest.getMobileNo())) {
            validationResponse.getValidationMessage().add(ValidationInfoEnum.EMPTY_MOBILE.getValidationInfoValue());
        }
        if (StringUtils.isEmpty(userSignupRequest.getPassword())) {
            validationResponse.getValidationMessage().add(ValidationInfoEnum.EMPTY_PASSWORD.getValidationInfoValue());
        }
        if (StringUtils.isEmpty(userSignupRequest.getRole())) {
            validationResponse.getValidationMessage().add(ValidationInfoEnum.EMPTY_ROLES.getValidationInfoValue());
        }
        if (StringUtils.isEmpty(userSignupRequest.getBio())) {
            validationResponse.getValidationMessage().add(ValidationInfoEnum.EMPTY_ABOUT.getValidationInfoValue());
        }
        if (StringUtils.isEmpty(userSignupRequest.getPicUrl())) {
            validationResponse.getValidationMessage().add(ValidationInfoEnum.EMPTY_IMAGE.getValidationInfoValue());
        }
        usernameAlreadyExists(userSignupRequest.getName(), validationResponse);
        return validationResponse;
    }

    private void usernameAlreadyExists(String userName, ValidationResponse<String, User> validationResponse) {
        Optional<User> user = userDao.getByMobile(userName);
        if (StringUtils.isNotEmpty(userName) && user.isPresent()) {
            validationResponse.getValidationMessage().add(String.format(ValidationInfoEnum.USERNAME_ALREADY_EXISTS.getValidationInfoValue(), userName));
        }
    }

    public ValidationResponse<String, User> validateLoginRequest(LoginSignupProto.LoginRequest userLoginRequest) {
        LOG.info("starting validation for login request");
        ValidationResponse<String, User> validationResponse = new ValidationResponse<>(new ArrayList<>());
        if (StringUtils.isEmpty(userLoginRequest.getMobileNo())) {
            validationResponse.getValidationMessage().add(ValidationInfoEnum.EMPTY_USERNAME.getValidationInfoValue());
        }
        if (StringUtils.isEmpty(userLoginRequest.getPassword())) {
            validationResponse.getValidationMessage().add(ValidationInfoEnum.EMPTY_PASSWORD.getValidationInfoValue());
        }
        isCredentialsCorrect(userLoginRequest.getMobileNo(), userLoginRequest.getPassword(), validationResponse);
        return validationResponse;
    }

    private void isCredentialsCorrect(String username, String password, ValidationResponse<String, User> validationResponse) {
        Optional<User> user = userDao.getByMobile(username);
        if (!user.isPresent()) {
            validationResponse.getValidationMessage().add(String.format(ValidationInfoEnum.USER_NOT_FOUND_IN_DB.getValidationInfoValue(), username));
        } else if (user.isPresent() && !(password.equals(user.get().getPassword()))) {
            validationResponse.getValidationMessage().add(ValidationInfoEnum.INVALID_LOGIN_CREDENTALS.getValidationInfoValue());
        } else {
            validationResponse.setValidObject(user.get());
        }
    }
}
