package org.gagu.gagubackend.global.exception;

public class NotMemberException extends NullPointerException{
    public NotMemberException(){
        super("this user is not part of the chat room");
    }
    public NotMemberException(String msg){
        super(msg);
    }
}
