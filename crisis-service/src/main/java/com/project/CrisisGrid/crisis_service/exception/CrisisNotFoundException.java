package com.project.CrisisGrid.crisis_service.exception;


import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class CrisisNotFoundException extends RuntimeException{
    public CrisisNotFoundException(String message){
        super(message);
    }
}
