package com.gbuddies.filters;

import com.gbuddies.constants.EZuulConstants;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;

public class PreFilters extends ZuulFilter {
    private static final Logger LOGGER = LoggerFactory.getLogger(PreFilters.class);

    @Autowired
    private Environment environment;

    @Value("${gbuddies.enable.zuul.filters}")
    private boolean enableFilters;

    @Override
    public String filterType() {
        return EZuulConstants.PRE.getFilterType();
    }

    @Override
    public int filterOrder() {
        return 0;
    }

    @Override
    public boolean shouldFilter() {
        return enableFilters;
    }

    @Override
    public Object run() throws ZuulException {
        RequestContext ctx = RequestContext.getCurrentContext();
        return null;
    }
}
