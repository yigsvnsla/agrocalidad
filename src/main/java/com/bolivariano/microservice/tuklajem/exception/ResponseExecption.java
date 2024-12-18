package com.bolivariano.microservice.tuklajem.exception;

import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class ResponseExecption extends RuntimeException {

    private HttpStatus status;

    public ResponseExecption(@NonNull HttpStatus _httpStatus, String _message) {
        super(_message);

        this.status = _httpStatus == null ? HttpStatus.BAD_REQUEST : _httpStatus;
    }

}