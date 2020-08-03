package com.example.gbuddy.util;

import com.example.gbuddy.models.constants.MatcherConst;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

@Component
public class MatcherUtil {
    public boolean isValidStatus(String status) {
        return StringUtils.isNotEmpty(status) && (MatcherConst.MATCHED.getName().equals(status) || MatcherConst.UNMATCHED.getName().equals(status));
    }
}
