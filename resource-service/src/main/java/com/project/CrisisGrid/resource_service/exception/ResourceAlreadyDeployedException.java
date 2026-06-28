package com.project.CrisisGrid.resource_service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class ResourceAlreadyDeployedException extends RuntimeException {

    public ResourceAlreadyDeployedException(String message) {
        super(message);
    }
}
