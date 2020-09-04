package com.example.gbuddy.controllers;

import com.example.gbuddy.util.MiscUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/")
public class HomeController {

    @Autowired
    private MiscUtil miscUtil;

    @Value("${gbuddy.clean.secret}")
    private  String secret;

    @CrossOrigin
    @GetMapping("/")
    public ResponseEntity test() {
        Map<String, String> resp = new HashMap<>();
        resp.put("responseMessage", "gbuddies started properly");
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

