package io.indices.hideandseek.hideandseek;

import lombok.Getter;
import lombok.Setter;
import me.minidigger.voxelgameslib.user.User;
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

    public void setStationaryLocation(Location location) {
        stationaryLocation = location;

        if(location == null) {
            isStationary = false;
        } else {
            isStationary = true;
        }
    }
}
