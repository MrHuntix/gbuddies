package com.example.gbuddy.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/")
public class HomeController {
    @CrossOrigin
    @GetMapping("/")
    public ResponseEntity test() {
        Map<String, String> resp = new HashMap<>();
        resp.put("responseMessage", "gbuddies started properly");
        return ResponseEntity.ok(resp);
    }

}

