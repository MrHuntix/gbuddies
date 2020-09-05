package com.example.gbuddy.controllers;

import com.example.gbuddy.util.MiscUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/")
public class HomeController {

    private static final Logger LOG = LoggerFactory.getLogger(HomeController.class);
    @Autowired
    private MiscUtil miscUtil;

    @Value("${gbuddy.clean.secret}")
    private  String secret;

    @CrossOrigin
    @GetMapping("/")
    public ResponseEntity test() {
        Map<String, String> resp = new HashMap<>();
        resp.put("responseMessage", "gbuddies started properly");
        LOG.info("jmx port: {}, jmx enabled: {}, jmx host: {}", System.getProperty("com.sun.management.jmxremote.port"), System.getProperty("com.sun.management.jmxremote"), System.getProperty("java.rmi.server.hostname"));
        return ResponseEntity.ok(resp);
    }

    @CrossOrigin
    @GetMapping("/cleanup/{code}")
    public ResponseEntity cleanDb(@PathVariable("code") String code) {
        Map<String, String> resp = new HashMap<>();
        if(code.equals(secret)) resp.put("responseMessage", miscUtil.clenup());
        else resp.put("responseMessage", "aah! you are not worthy.");
        return ResponseEntity.ok(resp);
    }
}

