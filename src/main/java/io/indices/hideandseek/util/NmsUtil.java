package io.indices.hideandseek.util;

import net.minecraft.server.v1_12_R1.Entity;

import java.lang.reflect.Field;

public class NmsUtil {

    public static int getNextEntityId() {
        int next = 0;

        try {
            Field entityId = Entity.class.getDeclaredField("entityCount");
            entityId.setAccessible(true);
            next = entityId.getInt(null) + 1;
            entityId.setInt(null, next);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return next;
    }
}
