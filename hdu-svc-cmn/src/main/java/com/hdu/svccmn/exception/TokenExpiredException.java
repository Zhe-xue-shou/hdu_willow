package com.hdu.svccmn.exception;

public class TokenExpiredException extends Exception {
    public TokenExpiredException(String s) {
        super(s);
    }
}
