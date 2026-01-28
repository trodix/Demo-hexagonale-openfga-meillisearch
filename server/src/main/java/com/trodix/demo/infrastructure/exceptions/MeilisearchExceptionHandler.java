package com.trodix.demo.infrastructure.exceptions;

import com.meilisearch.sdk.exceptions.MeilisearchApiException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class MeilisearchExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(MeilisearchApiException.class)
    public void handleMeilisearchApiException(MeilisearchApiException ex) {
        if ("document_not_found".equals(ex.getCode())) {
           throw new HttpClientErrorException(HttpStatus.NOT_FOUND, ex.getMessage());
        }
        throw new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
    }

}
