package dev.emortal;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.sound.SoundEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public class Elevator {

    private final Set<Player> players = new CopyOnWriteArraySet<>();
    private final Audience audience = Audience.audience(players);

    private final Instance instance;
    private final Point position;
    private final int maxPlayers;
    private final Area area;

    private final HologramEntity hologram;

    public Elevator(Instance instance, Point position, int maxPlayers) {
        this.instance = instance;
        this.position = position;
        this.maxPlayers = maxPlayers;

        Point hologramPosition = this.position.add(0.0, 1.5, 0.5);

        // If elevator on left
        if (position.blockX() > 0) {
            hologramPosition = hologramPosition.add(-0.5, 0, 0);

            this.area = new Area(position.add(4, 0, 2), position.add(0, 0, -1));
        } else { // If elevator on right
            hologramPosition = hologramPosition.add(1.5, 0, 0);

            this.area = new Area(position.add(-3, 0, -1), position.add(1, 0, 2));
        }

        this.hologram = new HologramEntity(getHologramName(0));
        this.hologram.setInstance(instance, hologramPosition);
        
        // TODO: Timers
    }

    public boolean insideElevator(Point position) {
        return this.area.isInside(position, 0);
    }

    public Area getArea() {
        return area;
    }

    public void addPlayer(Player player) {
        player.sendActionBar(Component.text("You entered the elevator", NamedTextColor.GREEN));

        players.add(player);
        this.hologram.setName(getHologramName(players.size()));

        audience.playSound(Sound.sound(SoundEvent.ENTITY_ITEM_FRAME_ADD_ITEM, Sound.Source.MASTER, 1f, 1f));
    }
    public void removePlayer(Player player) {
        player.sendActionBar(Component.text("You exited the elevator", NamedTextColor.GREEN));

        audience.playSound(Sound.sound(SoundEvent.ENTITY_ITEM_FRAME_REMOVE_ITEM, Sound.Source.MASTER, 1f, 1f));

        players.remove(player);
        this.hologram.setName(getHologramName(players.size()));
    }

    private Component getHologramName(int players) {
        return Component.text()
                .append(Component.text(players))
                .append(Component.text("/", NamedTextColor.GRAY))
                .append(Component.text(this.maxPlayers))
                .build();
    }
}
