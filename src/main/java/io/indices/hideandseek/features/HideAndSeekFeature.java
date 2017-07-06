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
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;

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

    private List<User> hiders = new ArrayList<>();
    private List<User> seekers = new ArrayList<>();

    private Material[] transformableBlocks = new Material[]{Material.BEACON, Material.SAND, Material.LAPIS_BLOCK};
    private Map<UUID, Material> playerBlockMap = new HashMap<>();
    private Map<UUID, Location> playerBlockLocations = new HashMap<>();

    @Override
    public void init() {

    }

    @Override
    public void start() {
        // todo load variables from previous phase
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

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        userHandler.getUser(event.getEntity().getUniqueId()).ifPresent((user -> {
            if(getPhase().getGame().getPlayers().contains(user)) {
                user.getPlayer().spigot().respawn();
                if(hiders.contains(user)) {
                    hiders.remove(user);
                    seekers.add(user); // you've joined the evil side now
                    user.getPlayer().removePotionEffect(PotionEffectType.INVISIBILITY);
                }
            }
        }));
    }
}
