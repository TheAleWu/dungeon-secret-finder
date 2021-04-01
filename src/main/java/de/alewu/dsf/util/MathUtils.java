package de.alewu.dsf.util;

import net.minecraft.entity.Entity;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;

public class MathUtils {

    public static Vec3 calculateLookVector(Entity player) {
        float rotationYaw = player.rotationYaw, rotationPitch = player.rotationPitch;
        float vx = -MathHelper.sin((float) Math.toRadians(rotationYaw)) * MathHelper.cos((float) Math.toRadians(rotationPitch));
        float vz = MathHelper.cos((float) Math.toRadians(rotationYaw)) * MathHelper.cos((float) Math.toRadians(rotationPitch));
        float vy = -MathHelper.sin((float) Math.toRadians(rotationPitch));
        return new Vec3(vx, vy, vz);
    }

}
