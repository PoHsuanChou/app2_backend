package org.example.exception;


import lombok.Getter;

public class CustomExceptions {

    @Getter
    public static class BaseException extends RuntimeException {
        private final int errorCode;

        public BaseException(String message, int errorCode) {
            super(message);
            this.errorCode = errorCode;
        }
    }

    public static class UserNotFoundException extends BaseException {
        public UserNotFoundException(String message) {
            super(message, 404);
        }
    }

    public static class InvalidRequestException extends BaseException {
        public InvalidRequestException(String message) {
            super(message, 400);
        }
    }

    public static class AuthenticationException extends BaseException {
        public AuthenticationException(String message) {
            super(message, 401);
        }
    }
}
