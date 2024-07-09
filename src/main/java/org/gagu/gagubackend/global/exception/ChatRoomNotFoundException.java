package org.gagu.gagubackend.global.exception;

public class ChatRoomNotFoundException extends NullPointerException{
    public ChatRoomNotFoundException(){
        super("chat room not found.");
    }
    public ChatRoomNotFoundException(String msg){
        super(msg);
    }
}
