package com.texteditor.apt.Networking;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class StatusController {

    @Autowired
    private RoomManager roomManager;

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> status() {
        Map<String, Object> res = new HashMap<>();
        res.put("status", "UP");
        res.put("message", "Plain Text Editor WebSocket Server is running");
        res.put("websocketUrl", "ws://localhost:8080/ws");
        return ResponseEntity.ok(res);
    }

    @GetMapping("/rooms")
    public ResponseEntity<Map<String, Object>> getAllRooms() {
        Map<String, Object> res = new HashMap<>();
        res.put("rooms", roomManager.getAllRooms());
        res.put("totalRooms", roomManager.getAllRooms().size());
        return ResponseEntity.ok(res);
    }

    @GetMapping("/rooms/{docId}/count")
    public ResponseEntity<Map<String, Object>> getRoomCount(@PathVariable String docId) {
        Map<String, Object> res = new HashMap<>();
        res.put("docId", docId);
        res.put("count", roomManager.getUserCount(docId));
        res.put("maxUsers", RoomManager.MAX_USERS_PER_ROOM);
        res.put("isFull", roomManager.getUserCount(docId) >= RoomManager.MAX_USERS_PER_ROOM);
        return ResponseEntity.ok(res);
    }

    //scaffold for Person 4 to add access codes
    @PostMapping("/rooms/{docId}/join")
    public ResponseEntity<Map<String, Object>> joinWithCode(
            @PathVariable String docId,
            @RequestBody Map<String, String> body) {
        //Person 4: validate body.get("accessCode") using SecureRandom-generated codes
        Map<String, Object> res = new HashMap<>();
        res.put("docId", docId);
        res.put("role", "EDITOR"); //Person 4: replace this with real role logic
        res.put("note", "Access code validation not yet implemented");
        return ResponseEntity.ok(res);
    }
}
