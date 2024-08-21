package org.gagu.gagubackend.global.exception;

public class NotFoundUserException extends NullPointerException{
    public NotFoundUserException(){
        super("user is not found.");
    }
    public NotFoundUserException(String msg){
        super(msg);
    }
}
