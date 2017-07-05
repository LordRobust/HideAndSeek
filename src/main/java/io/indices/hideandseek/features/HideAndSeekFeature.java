package io.indices.hideandseek.features;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import io.indices.hideandseek.HideAndSeekPlugin;
import me.minidigger.voxelgameslib.feature.AbstractFeature;
import me.minidigger.voxelgameslib.feature.FeatureInfo;
import me.minidigger.voxelgameslib.scoreboard.Scoreboard;
import me.minidigger.voxelgameslib.user.User;
import me.minidigger.voxelgameslib.user.UserHandler;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Singleton
@FeatureInfo(name = "HideAndSeekFeature", author = "aphel", version = "1.0",
        description = "Main feature for Hide and Seek")
public class HideAndSeekFeature extends AbstractFeature {
    @Inject
    private HideAndSeekPlugin plugin;
    @Inject
    private UserHandler userHandler;

    private ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();
    private Scoreboard scoreboard;

    private Material[] transformableBlocks = new Material[]{Material.BEACON, Material.SAND, Material.LAPIS_BLOCK};
    private Map<UUID, Material> playerBlockMap = new HashMap<>();
    private Map<UUID, Location> playerBlockLocations = new HashMap<>();

    @Override
    public void init() {

    }

    @Override
    public void start() {
        // make them invisible
        for (User user : getPhase().getGame().getPlayers()) {
            Player p = user.getPlayer();
            for (User otherUser : getPhase().getGame().getPlayers()) {
                p.hidePlayer(otherUser.getPlayer());
            }
        }

        // assign players a random block
        int block = 0;
        for (User user : getPhase().getGame().getPlayers()) {
            Player p = user.getPlayer();

            playerBlockMap.put(user.getUuid(), transformableBlocks[block]);

            block++;

            if (block > transformableBlocks.length - 1) {
                block = 0;
            }
        }
    }

    @Override
    public void stop() {

    }

    @Override
    public void tick() {

    }

    public void setScoreboard(Scoreboard scoreboard) {
        this.scoreboard = scoreboard;
    }

    @Override
    public Class[] getDependencies() {
        return new Class[0];
    }
}
