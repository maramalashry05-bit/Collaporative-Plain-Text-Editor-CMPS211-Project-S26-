package com.texteditor.apt.Networking;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

@Component
public class RoomManager {

    public static final int MAX_USERS_PER_ROOM = 4;

    private final ConcurrentHashMap<String, Set<String>> rooms = new ConcurrentHashMap<>();

    public Set<String> getUsers(String docId) {
        return rooms.getOrDefault(docId, ConcurrentHashMap.newKeySet());
    }

    public boolean joinRoom(String docId, String userId) {
        rooms.putIfAbsent(docId, ConcurrentHashMap.newKeySet());
        Set<String> users = rooms.get(docId);
        if (users.contains(userId)) return true; //allow reconnect
        if (users.size() >= MAX_USERS_PER_ROOM) return false; //law el room full
        users.add(userId);
        System.out.println("[RoomManager] " + userId + " joined room: " + docId
                + " (" + users.size() + "/" + MAX_USERS_PER_ROOM + ")");
        return true;
    }

    public void leaveRoom(String docId, String userId) {
        Set<String> users = rooms.get(docId);
        if (users != null) {
            users.remove(userId);
            System.out.println("[RoomManager] " + userId + " left room: " + docId
                    + " (" + users.size() + "/" + MAX_USERS_PER_ROOM + ")");
            if (users.isEmpty()) rooms.remove(docId);
        }
    }

    public boolean isInRoom(String docId, String userId) {
        Set<String> users = rooms.get(docId);
        return users != null && users.contains(userId);
    }

    public int getUserCount(String docId) {
        Set<String> users = rooms.get(docId);
        return users == null ? 0 : users.size();
    }

    public ConcurrentHashMap<String, Set<String>> getAllRooms() {
        return rooms;
    }
}
