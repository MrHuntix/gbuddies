package com.example.gbuddy.models.response;

import lombok.Data;

import java.util.List;

@Data
public class ValidationResponse<MSG, OBJ> {
    List<MSG> validationMessage;
    OBJ validObject;

    public ValidationResponse(List<MSG> validationMessage) {
        this.validationMessage = validationMessage;
        this.validObject = null;
    }
}
