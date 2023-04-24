package dev.emortal.lobby;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.metadata.other.AreaEffectCloudMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SeatEntity extends Entity {

    private final @Nullable Runnable onRemoval;
    public SeatEntity(@Nullable Runnable onRemoval) {
        super(EntityType.AREA_EFFECT_CLOUD);

        this.onRemoval = onRemoval;

        AreaEffectCloudMeta meta = (AreaEffectCloudMeta)entityMeta;
        meta.setRadius(0f);
        setNoGravity(true);
        hasPhysics = false;
    }

    @Override
    public void removePassenger(@NotNull Entity entity) {
        super.removePassenger(entity);

        entity.setVelocity(new Vec(0.0, MinecraftServer.TICK_PER_SECOND * 0.5, 0.0));

        if (getPassengers().isEmpty()) {
            if (onRemoval != null) {
                onRemoval.run();
            }
            remove();
        }
    }
}
