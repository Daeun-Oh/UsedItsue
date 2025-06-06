package org.koreait.global.exceptions;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@Getter @Setter
public class CommonException extends RuntimeException {
    private final HttpStatus status;
    private boolean errorCode;

    public CommonException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }
}
