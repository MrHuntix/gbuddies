package com.example.gbuddy.service.validators;

import com.example.gbuddy.models.constants.ValidationInfoEnum;
import com.example.gbuddy.protos.GymProto;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

@Component
public class GymValidator {
    private static final Logger LOG = LoggerFactory.getLogger(GymValidator.class);

    public List<String> validateGym(GymProto.Gym gym) {
        LOG.info("start of gym validation process");
        List<String> validationMessage = new ArrayList<>();
        Validate.notNull(gym,"gym object should not be null");
        if(StringUtils.isNotEmpty(gym.getName())) {
            validationMessage.add(ValidationInfoEnum.EMPTY_INVALID_GYM_NAME.getValidationInfoValue());
        }
        if(StringUtils.isNotEmpty(gym.getWebsite())) {
            validationMessage.add(ValidationInfoEnum.EMPTY_INVALID_WEBSITE.getValidationInfoValue());
        }
        if(!CollectionUtils.isEmpty(gym.getBranchesList())) {
            validationMessage.add(ValidationInfoEnum.EMPTY_BRANCHES.getValidationInfoValue());
            return validationMessage;
        }
        gym.getBranchesList().forEach(branch -> {
            if(StringUtils.isNotEmpty(branch.getLocality())) {
                validationMessage.add(String.format(ValidationInfoEnum.EMPTY_INVALID_LOCALITY.getValidationInfoValue(), branch.getLocality()));
            }
            if(StringUtils.isNotEmpty(branch.getCity())) {
                validationMessage.add(String.format(ValidationInfoEnum.EMPTY_INVALID_CITY.getValidationInfoValue(), branch.getCity()));
            }
            if(Double.isNaN(branch.getLatitude())) {
                validationMessage.add(String.format(ValidationInfoEnum.EMPTY_INVALID_LATITUDE.getValidationInfoValue(), branch.getLatitude()));
            }
            if(Double.isNaN(branch.getLongitude())) {
                validationMessage.add(String.format(ValidationInfoEnum.EMPTY_INVALID_LONGIUDE.getValidationInfoValue(), branch.getLongitude()));
            }
            if(StringUtils.isNotEmpty(branch.getContact())) {
                validationMessage.add(String.format(ValidationInfoEnum.EMPTY_INVALID_CONTACT.getValidationInfoValue(), branch.getContact()));
            }
        });
        return validationMessage;
    }
}
