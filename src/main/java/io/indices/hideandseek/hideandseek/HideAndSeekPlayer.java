package io.indices.hideandseek.hideandseek;

import lombok.Getter;
import lombok.Setter;
import com.voxelgameslib.voxelgameslib.user.User;
import org.bukkit.Location;
import org.bukkit.Material;

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

    public void setStationaryLocation(Location location) {
        stationaryLocation = location;

        if(location == null) {
            isStationary = false;
        } else {
            isStationary = true;
        }
    }
}
