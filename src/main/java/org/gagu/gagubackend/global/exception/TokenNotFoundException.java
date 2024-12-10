package org.gagu.gagubackend.global.exception;

public class TokenNotFoundException extends RuntimeException{
    public TokenNotFoundException(){
        super("Token is null");
    }
    public TokenNotFoundException(String msg){
        super(msg);
    }
}
