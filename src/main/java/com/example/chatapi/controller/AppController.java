package com.example.chatapi.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/status")
public class AppController {

    @GetMapping("/isReady")
    public ResponseEntity<Boolean> checkServerStatus() {
        boolean isServerReady = true;
        return ResponseEntity.ok(isServerReady);
    }

}