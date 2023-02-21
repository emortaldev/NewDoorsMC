package dev.emortal;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.GameMode;
import net.minestom.server.event.player.PlayerLoginEvent;
import net.minestom.server.event.player.PlayerMoveEvent;
import net.minestom.server.instance.AnvilLoader;
import net.minestom.server.instance.InstanceContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class Main {

    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    private static final Vec[] ELEVATOR_POSITIONS = new Vec[] {
            // Left
            new Vec(8.0, 65.0, 14.0),
            new Vec(8.0, 65.0, 19.0),
            new Vec(8.0, 65.0, 24.0),
            new Vec(8.0, 65.0, 29.0),
            new Vec(8.0, 65.0, 34.0),

            // Right
            new Vec(-9.0, 65.0, 14.0),
            new Vec(-9.0, 65.0, 19.0),
            new Vec(-9.0, 65.0, 24.0),
            new Vec(-9.0, 65.0, 29.0),
            new Vec(-9.0, 65.0, 34.0),
    };

    private static final Pos SPAWN_POS = new Pos(0.0, 67.0, 0.0);

    public static void main(String[] args) {
        LOGGER.info("Starting Doors server");

        MinecraftServer server = MinecraftServer.init();

        // Create lobby instance and load from anvil
        InstanceContainer lobbyInstance = MinecraftServer.getInstanceManager().createInstanceContainer();
        lobbyInstance.enableAutoChunkLoad(false);
        lobbyInstance.setChunkLoader(new AnvilLoader("./lobby/"));

        // Load some chunks since we disabled auto chunk load
        int radius = 4;
        List<CompletableFuture> futures = new ArrayList<>();
        for (int x = -radius; x < radius; x++) {
            for (int z = -radius; z < radius; z++) {
                futures.add(lobbyInstance.loadChunk(x, z));
            }
        }
        // Wait for the chunks to load
        LOGGER.info("Waiting for chunks...");
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        LOGGER.info("Loaded chunks!");

        // Initialize elevators
        Elevator[] elevators = new Elevator[ELEVATOR_POSITIONS.length];
        Arrays.setAll(elevators, e -> new Elevator(lobbyInstance, ELEVATOR_POSITIONS[e], Math.min(e+1 % 5, 4)));


        var global = MinecraftServer.getGlobalEventHandler();
        global.addListener(PlayerLoginEvent.class, e -> {
            e.setSpawningInstance(lobbyInstance);
            e.getPlayer().setRespawnPoint(SPAWN_POS);

            e.getPlayer().setGameMode(GameMode.ADVENTURE);
        });

        Map<UUID, Elevator> elevatorMap = new ConcurrentHashMap<>();

        global.addListener(PlayerMoveEvent.class, e -> {
            // Check for players entering an elevator

            // If already in elevator
            if (elevatorMap.containsKey(e.getPlayer().getUuid())) {
                // Check for exiting elevator
                Elevator currentElevator = elevatorMap.get(e.getPlayer().getUuid());

                if (!currentElevator.insideElevator(e.getNewPosition())) {
                    currentElevator.removePlayer(e.getPlayer());
                    elevatorMap.remove(e.getPlayer().getUuid());
                }

                return;
            }

            Elevator collidingElevator = null;
            for (Elevator elevator : elevators) {
                if (elevator.insideElevator(e.getNewPosition())) {
                    collidingElevator = elevator;
                    break;
                }
            }

            if (collidingElevator != null) {
                collidingElevator.addPlayer(e.getPlayer());
                elevatorMap.put(e.getPlayer().getUuid(), collidingElevator);
            }
        });

        server.start("0.0.0.0", 25565);
    }
}