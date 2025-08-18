package adris.altoclef.brain.server.global;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import adris.altoclef.brain.server.local.ServerLocalState;

public class ServerGlobalNPCManager {
    public static ConcurrentHashMap<UUID, ServerLocalState> getLocalState = new ConcurrentHashMap<>();
}
