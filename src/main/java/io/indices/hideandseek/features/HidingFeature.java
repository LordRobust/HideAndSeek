package io.indices.hideandseek.features;

import co.aikar.commands.annotation.*;
import com.comphenix.packetwrapper.WrapperPlayServerEntityDestroy;
import com.comphenix.packetwrapper.WrapperPlayServerMount;
import com.comphenix.packetwrapper.WrapperPlayServerSpawnEntity;
import com.comphenix.packetwrapper.WrapperPlayServerSpawnEntityLiving;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import io.indices.hideandseek.util.NmsUtil;
import me.minidigger.voxelgameslib.feature.AbstractFeature;
import me.minidigger.voxelgameslib.feature.AbstractFeatureCommand;
import me.minidigger.voxelgameslib.feature.FeatureCommandImplementor;
import me.minidigger.voxelgameslib.feature.features.MapFeature;
import me.minidigger.voxelgameslib.feature.features.SpawnFeature;
import me.minidigger.voxelgameslib.lang.Lang;
import me.minidigger.voxelgameslib.lang.LangKey;
import me.minidigger.voxelgameslib.scoreboard.Scoreboard;
import me.minidigger.voxelgameslib.user.User;
import me.minidigger.voxelgameslib.user.UserHandler;
import net.kyori.text.TextComponent;
import net.kyori.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;

public class HidingFeature extends AbstractFeature implements FeatureCommandImplementor {

    private Scoreboard scoreboard;

    private List<User> seekers = new ArrayList<>();
    private List<User> hiders = new ArrayList<>();

    private Material[] transformableBlocks = new Material[]{Material.BEACON, Material.SAND, Material.LAPIS_BLOCK};
    private Map<User, Material> playerBlockMap = new HashMap<>();
    private Map<User, Location> playerBlockLocations = new HashMap<>();

    private int nextBlock;

    @Inject
    private UserHandler userHandler;

    @Override
    public void init() {

    }

    @Override
    public void start() {
        // choose seekers and hiders

        Random random = new Random();
        int playerCount = getPhase().getGame().getPlayers().size();
        int seekerAmount = (playerCount / 10) + 1; // one seeker per 10 players

        for(int i = 0; i < seekerAmount; i++) {
            int n = random.nextInt(playerCount - 1);

            seekers.add(getPhase().getGame().getPlayers().get(n));
        }

        getPhase().getGame().getPlayers().forEach((user -> {
            if(!seekers.contains(user)) {
                Player player = user.getPlayer();
                hiders.add(user);

                assignBlock(user);

                int playerEntityId = player.getEntityId();

                // i would rather use NMS, but i was convinced to use protocollib
                // welcome to the evil side, i suppose
                WrapperPlayServerEntityDestroy destroyPacket = new WrapperPlayServerEntityDestroy();
                destroyPacket.setEntityIds(new int[]{playerEntityId});

                WrapperPlayServerSpawnEntityLiving silverfishSpawnPacket = new WrapperPlayServerSpawnEntityLiving();
                silverfishSpawnPacket.setEntityID(playerEntityId);
                silverfishSpawnPacket.setUniqueId(UUID.randomUUID());
                silverfishSpawnPacket.setType(EntityType.SILVERFISH);

                Location location = player.getLocation();
                silverfishSpawnPacket.setX(location.getX());
                silverfishSpawnPacket.setY(location.getY());
                silverfishSpawnPacket.setZ(location.getZ());
                silverfishSpawnPacket.setPitch(location.getPitch());
                silverfishSpawnPacket.setYaw(location.getYaw());

                WrappedDataWatcher silverfishDataWatcher = new WrappedDataWatcher();
                silverfishDataWatcher.setObject(0, WrappedDataWatcher.Registry.get(Byte.class), (byte) 0x20, true);
                silverfishDataWatcher.setObject(4, WrappedDataWatcher.Registry.get(Boolean.class), true, true);
                silverfishDataWatcher.setObject(5, WrappedDataWatcher.Registry.get(Boolean.class), true, true);
                //silverfishDataWatcher.setObject(12, WrappedDataWatcher.Registry.get(Integer.class), 2, true); // Set size
                silverfishSpawnPacket.setMetadata(silverfishDataWatcher);

                WrapperPlayServerSpawnEntity blockSpawnPacket = new WrapperPlayServerSpawnEntity();
                int blockEntityId = NmsUtil.getNextEntityId();
                blockSpawnPacket.setEntityID(blockEntityId);
                blockSpawnPacket.setUniqueId(UUID.randomUUID());
                blockSpawnPacket.setType(70);
                blockSpawnPacket.setObjectData(playerBlockMap.get(user).getId());

                blockSpawnPacket.setX(location.getX());
                blockSpawnPacket.setY(location.getY());
                blockSpawnPacket.setZ(location.getZ());
                blockSpawnPacket.setPitch(location.getPitch());
                blockSpawnPacket.setYaw(location.getYaw());

                WrapperPlayServerMount mount = new WrapperPlayServerMount();
                mount.setEntityID(player.getEntityId());
                mount.setPassengerIds(new int[]{blockEntityId});

                getPhase().getGame().getPlayers().stream().filter((otherU) -> !otherU.getUuid().equals(user.getUuid())).forEach((otherU) -> {
                    Player otherPlayer = otherU.getPlayer();

                    Bukkit.getLogger().info(otherU.getRawDisplayName());

                    destroyPacket.sendPacket(otherPlayer);
                    silverfishSpawnPacket.sendPacket(otherPlayer);
                    blockSpawnPacket.sendPacket(otherPlayer);
                    mount.sendPacket(otherPlayer);
                });

                //user.setPrefix(TextComponent.of("[H] ").color(TextColor.GREEN).append(user.getPrefix()));
                player.sendTitle("You have 30 seconds to hide!", null, 10, 70, 20);
            }
        }));

        seekers.forEach((seeker) -> {
            seeker.getPlayer().teleport(seeker.getPlayer().getLocation()); // todo teleport them to a locked location, dummy code for now
            seeker.sendMessage(TextComponent.of("Start counting down from 30...").color(TextColor.GREEN)); // todo create lang leys
        });
    }

    public void setScoreboard(Scoreboard scoreboard) {
        this.scoreboard = scoreboard;
    }

    /**
     * Assigns a player with a block
     *
     * @param user the user
     */
    private void assignBlock(User user) {
        if(nextBlock > (transformableBlocks.length - 1)) {
            nextBlock = 0;
        }

        playerBlockMap.put(user, transformableBlocks[nextBlock]);

        nextBlock++;
    }

    @Override
    public void stop() {

    }

    @Override
    public void tick() {

    }

    @Override
    public Class[] getDependencies() {
        return new Class[]{MapFeature.class, SpawnFeature.class};
    }

    @Override
    public AbstractFeatureCommand getCommandClass() {
        return new HidingFeatureCommands();
    }

    @Singleton
    public class HidingFeatureCommands extends AbstractFeatureCommand {

        @CommandAlias("disguise")
        @CommandPermission("%user")
        public void disguise(User user) {
            Player player = user.getPlayer();

            int playerEntityId = player.getEntityId();

            // i would rather use NMS, but i was convinced to use protocollib
            // welcome to the evil side, i suppose
            WrapperPlayServerEntityDestroy destroyPacket = new WrapperPlayServerEntityDestroy();
            destroyPacket.setEntityIds(new int[]{playerEntityId});

            WrapperPlayServerSpawnEntityLiving silverfishSpawnPacket = new WrapperPlayServerSpawnEntityLiving();
            silverfishSpawnPacket.setEntityID(playerEntityId);
            silverfishSpawnPacket.setUniqueId(UUID.randomUUID());
            silverfishSpawnPacket.setType(EntityType.SILVERFISH);

            Location location = player.getLocation();
            silverfishSpawnPacket.setX(location.getX());
            silverfishSpawnPacket.setY(location.getY());
            silverfishSpawnPacket.setZ(location.getZ());
            silverfishSpawnPacket.setPitch(location.getPitch());
            silverfishSpawnPacket.setYaw(location.getYaw());

            WrappedDataWatcher silverfishDataWatcher = new WrappedDataWatcher();
            silverfishDataWatcher.setObject(0, WrappedDataWatcher.Registry.get(Byte.class), (byte) 0x20, true);
            silverfishDataWatcher.setObject(4, WrappedDataWatcher.Registry.get(Boolean.class), true, true);
            silverfishDataWatcher.setObject(5, WrappedDataWatcher.Registry.get(Boolean.class), true, true);
            //silverfishDataWatcher.setObject(12, WrappedDataWatcher.Registry.get(Integer.class), 2, true); // Set size
            silverfishSpawnPacket.setMetadata(silverfishDataWatcher);

            WrapperPlayServerSpawnEntity blockSpawnPacket = new WrapperPlayServerSpawnEntity();
            int blockEntityId = NmsUtil.getNextEntityId();
            blockSpawnPacket.setEntityID(blockEntityId);
            blockSpawnPacket.setUniqueId(UUID.randomUUID());
            blockSpawnPacket.setType(70);
            blockSpawnPacket.setObjectData(playerBlockMap.get(user).getId());

            blockSpawnPacket.setX(location.getX());
            blockSpawnPacket.setY(location.getY());
            blockSpawnPacket.setZ(location.getZ());
            blockSpawnPacket.setPitch(location.getPitch());
            blockSpawnPacket.setYaw(location.getYaw());

            WrapperPlayServerMount mount = new WrapperPlayServerMount();
            mount.setEntityID(player.getEntityId());
            mount.setPassengerIds(new int[]{blockEntityId});

            Bukkit.getOnlinePlayers().stream().filter(p -> p != player).forEach((otherU) -> {
                Player otherPlayer = otherU.getPlayer();

                destroyPacket.sendPacket(otherPlayer);
                silverfishSpawnPacket.sendPacket(otherPlayer);
                blockSpawnPacket.sendPacket(otherPlayer);
                mount.sendPacket(otherPlayer);
            });
        }

    }
}
