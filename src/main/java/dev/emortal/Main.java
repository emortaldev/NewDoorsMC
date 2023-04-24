package dev.emortal;

import ch.qos.logback.core.net.server.Client;
import dev.emortal.commands.CreditsCommand;
import dev.emortal.commands.PerformanceCommand;
import dev.emortal.lobby.Elevator;
import dev.emortal.lobby.SeatEntity;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.GameMode;
import net.minestom.server.event.player.*;
import net.minestom.server.extras.MojangAuth;
import net.minestom.server.instance.AnvilLoader;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.network.packet.client.play.ClientSteerVehiclePacket;
import net.minestom.server.timer.TaskSchedule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

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
        Arrays.setAll(elevators, e -> new Elevator(
                lobbyInstance,
                ELEVATOR_POSITIONS[e],
                Math.max(1, Math.min((e+1) % 6 + (e/6), 4))) // max players calculation - messy haha lol
        );


        var global = MinecraftServer.getGlobalEventHandler();
        global.addListener(PlayerLoginEvent.class, e -> {
            e.setSpawningInstance(lobbyInstance);
            e.getPlayer().setRespawnPoint(SPAWN_POS);

            e.getPlayer().setGameMode(GameMode.CREATIVE);
        });

        global.addListener(PlayerSpawnEvent.class, e -> {
            e.getPlayer().scheduler().buildTask(() -> {
                e.getPlayer().playSound(Sound.sound(Key.key("music.dawnofthedoors"), Sound.Source.MASTER, 0.4f, 1f), Sound.Emitter.self());

            }).delay(TaskSchedule.seconds(2)).repeat(TaskSchedule.millis(214000)).schedule();
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

        Set<Point> usedSeats = ConcurrentHashMap.newKeySet();
        global.addListener(PlayerBlockInteractEvent.class, e -> {
            // Seats!!!!
            if (e.getBlock().name().endsWith("stairs")) {
                if (!e.getBlock().getProperty("half").equals("bottom")) return;

                float yaw = switch (e.getBlock().getProperty("facing")) {
                    case "east" -> 90f;
                    case "south" -> 180f;
                    case "west" -> -90f;
                    default -> 0f;
                };

                SeatEntity entity = new SeatEntity(() -> {
                    usedSeats.remove(e.getBlockPosition());
                });
                entity.setInstance(lobbyInstance, new Pos(e.getBlockPosition().add(0.5, -0.15, 0.5), yaw, 0f)).thenRun(() -> {
                    entity.addPassenger(e.getPlayer());
                });
                usedSeats.add(e.getBlockPosition());
            }
        });
        // Seat dismounting
        global.addListener(PlayerPacketEvent.class, e -> {
            if (e.getPacket() instanceof ClientSteerVehiclePacket) {
                ClientSteerVehiclePacket packet = (ClientSteerVehiclePacket) e.getPacket();
                if (packet.flags() == 2) { // dismount flag
                    if (e.getPlayer().getVehicle() != null) {
                        e.getPlayer().getVehicle().removePassenger(e.getPlayer());
                    }
                }
            }
        });


        var commandManager = MinecraftServer.getCommandManager();
        commandManager.register(new CreditsCommand());
        commandManager.register(new PerformanceCommand(global));


        MojangAuth.init();

        server.start("0.0.0.0", 25565);
    }
}