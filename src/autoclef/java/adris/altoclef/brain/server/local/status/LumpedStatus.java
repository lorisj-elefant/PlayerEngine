package adris.altoclef.brain.server.local.status;

import adris.altoclef.AltoClefController;

public record LumpedStatus(AgentStatus agentStatus, WorldStatus worldStatus) {
    public static LumpedStatus fromMod(AltoClefController mod){
        return new LumpedStatus(AgentStatus.fromMod(mod), WorldStatus.fromMod(mod));
    }
}
