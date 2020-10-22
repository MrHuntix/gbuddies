package com.gbuddies.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;

//TODO enable jwt token validation and filter
//@Component
//@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Value("${login.url}")
    private String loginUrl;

    @Value("${signup.url}")
    private String signupUrl;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable();
        http.headers().frameOptions().disable();

        //login and signup requests do not need to be authenticated but other requests need to have jwt token
        http.authorizeRequests()
                .antMatchers(HttpMethod.POST, loginUrl, signupUrl).permitAll()
                .anyRequest().permitAll();
        //to prevent client details to be cached so that we do not authenticate requests without jwt token
        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
    }
}
