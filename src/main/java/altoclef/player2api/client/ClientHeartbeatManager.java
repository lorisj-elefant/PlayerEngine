package altoclef.player2api.client;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import altoclef.player2api.Player2APIService;

// static is one per client
public class ClientHeartbeatManager {
    private static final ExecutorService worker = Executors.newSingleThreadExecutor();

    private static long lastBeginProcessTime = 0;
    private static List<String> processSet = new ArrayList<>(); // use list to ensure order
    private static volatile boolean workerBusy = false;
    private static Queue<String> toProcess = new ArrayDeque<>();

    public static void onClientTick(String player2GameId) {
        if (!processSet.contains(player2GameId)) {
            processSet.add(player2GameId);
        }
        if (workerBusy) {
            return;
        }
        if (!toProcess.isEmpty()) {
            handleQueuePresent();
            return;
        }
        if (System.nanoTime() > lastBeginProcessTime + 60_000_000_000L) {
            process();
        }
    }

    private static void handleQueuePresent() {
        workerBusy = true;
        String id = toProcess.poll();
        worker.submit(() -> {
            Player2APIService service = new Player2APIService(id);
            service.sendHeartbeat();
            workerBusy = false;
        });
    }

    private static void process() {
        lastBeginProcessTime = System.nanoTime(); // want 60 sec from start of first heartbeat
        for (String el : processSet) {
            toProcess.add(el);
        }
    }

}
