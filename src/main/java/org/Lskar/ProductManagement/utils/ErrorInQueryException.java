package org.Lskar.ProductManagement.utils;

public class ErrorInQueryException extends RuntimeException {
    public ErrorInQueryException(String message) {
        super(message);
    }
}
