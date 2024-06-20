package org.gagu.gagubackend.chat.controller;

import org.gagu.gagubackend.chat.dto.request.RequestChatRoomDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/chat")
public class ChatController {
    @PostMapping("/new")
    public ResponseEntity<?> createChatRoom(@RequestBody RequestChatRoomDto requestChatRoomDto){
        return null;
    }
    @DeleteMapping("/out")
    public ResponseEntity<?> removeChatRoom(@RequestBody RequestChatRoomDto requestChatRoomDto){
        return null;
    }
    @GetMapping("/contents")
    public ResponseEntity<?> getChatRoom(@RequestBody RequestChatRoomDto requestChatRoomDto){
        return null;
    }
}
