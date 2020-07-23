package com.example.gbuddy.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.sql.Blob;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserSignupRequest {
    private String userName;
    private String emailId;
    private String mobileNo;
    private String password;
    private String about;
    private MultipartFile image;
}
