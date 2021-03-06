package io.indices.hideandseek.hideandseek;

import javax.annotation.Nullable;

import com.voxelgameslib.voxelgameslib.user.User;

import org.bukkit.Location;
import org.bukkit.Material;

import lombok.Getter;
import lombok.Setter;

public class HideAndSeekPlayer {

    @Getter
    @Setter
    private User user;

    @Getter
    @Setter
    private Material block;

    @Getter
    @Setter
    private boolean isStationary;

    @Getter
    private Location stationaryLocation;

    @Getter
    @Setter
    private int fakeBlockEntityId;

    @Getter
    @Setter
    private int entityId;

    public void setStationaryLocation(@Nullable Location location) {
        stationaryLocation = location;

        isStationary = location != null;
    }
}
