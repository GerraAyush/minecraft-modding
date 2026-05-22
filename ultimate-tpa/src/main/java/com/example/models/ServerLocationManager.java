package com.example.models;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.UUID;

public class ServerLocationManager {
    private static final Map<UUID, Stack<ServerLocation>> undo = new HashMap<>();
    private static final Map<UUID, Stack<ServerLocation>> redo = new HashMap<>();

    public static void putPreviousLocation(UUID uuid, ServerLocation serverLocation) {
        Stack<ServerLocation> previousLocations = undo.get(uuid);
        if (previousLocations == null) {
            previousLocations = new Stack<>();
            undo.put(uuid, previousLocations);
        }
        previousLocations.push(serverLocation);

        redo.put(uuid, new Stack<>());
    }

    public static ServerLocation getPreviousLocation(UUID uuid, ServerLocation serverLocation) {
        Stack<ServerLocation> undoLocations = undo.get(uuid);
        if (undoLocations == null) {
            undoLocations = new Stack<>();
            undo.put(uuid, undoLocations);
            return null;
        }

        if (undoLocations.size() == 0) {
            return null;
        }

        ServerLocation targetLocation = undoLocations.pop();

        Stack<ServerLocation> redoLocations = redo.get(uuid);
        if (redoLocations == null) {
            redoLocations = new Stack<>();
            redo.put(uuid, redoLocations);
        }
        redoLocations.push(serverLocation);

        return targetLocation;
    }

    public static ServerLocation getFrontLocation(UUID uuid, ServerLocation serverLocation) {
        Stack<ServerLocation> redoLocations = redo.get(uuid);
        if (redoLocations == null) {
            redoLocations = new Stack<>();
            redo.put(uuid, redoLocations);
            return null;
        }

        if (redoLocations.size() == 0) {
            return null;
        }

        ServerLocation targetLocation = redoLocations.pop();

        Stack<ServerLocation> undoLocations = undo.get(uuid);
        if (undoLocations == null) {
            undoLocations = new Stack<>();
            undo.put(uuid, undoLocations);
        }
        undoLocations.push(serverLocation);

        return targetLocation;
    }
}
