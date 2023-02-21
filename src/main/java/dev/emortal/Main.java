package dev.emortal;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.event.player.PlayerLoginEvent;
import net.minestom.server.instance.AnvilLoader;

public class Main {

    private static final Pos SPAWN_POS = new Pos(0.0, 67.0, 0.0);

    public static void main(String[] args) {
        var server = MinecraftServer.init();

        var lobbyInstance = MinecraftServer.getInstanceManager().createInstanceContainer();
        lobbyInstance.enableAutoChunkLoad(false);
        lobbyInstance.setChunkLoader(new AnvilLoader("./lobby/"));

        var radius = 4;
        for (int x = -radius; x < radius; x++) {
            for (int z = -radius; z < radius; z++) {
                lobbyInstance.loadChunk(x, z);
            }
        }

        var global = MinecraftServer.getGlobalEventHandler();
        global.addListener(PlayerLoginEvent.class, e -> {
            e.setSpawningInstance(lobbyInstance);
            e.getPlayer().setRespawnPoint(SPAWN_POS);
        });

        server.start("0.0.0.0", 25565);
    }
}