package dev.emortal;

import net.kyori.adventure.text.Component;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.metadata.other.AreaEffectCloudMeta;

public class HologramEntity extends Entity {

    public HologramEntity(Component name) {
        super(EntityType.AREA_EFFECT_CLOUD);

        AreaEffectCloudMeta meta = (AreaEffectCloudMeta)entityMeta;
        meta.setRadius(0f);
        meta.setCustomNameVisible(true);
        meta.setCustomName(name);

        hasPhysics = false;
        setNoGravity(true);
    }

    public void setName(Component name) {
        AreaEffectCloudMeta meta = (AreaEffectCloudMeta)entityMeta;
        meta.setCustomName(name);
    }

}
