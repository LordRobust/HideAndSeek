package io.indices.hideandseek.features;

import me.minidigger.voxelgameslib.feature.AbstractFeature;
import me.minidigger.voxelgameslib.scoreboard.Scoreboard;
import me.minidigger.voxelgameslib.user.User;
import me.minidigger.voxelgameslib.user.UserHandler;
import net.kyori.text.TextComponent;
import net.kyori.text.format.TextColor;
import net.minecraft.server.v1_12_R1.EntityFallingBlock;
import net.minecraft.server.v1_12_R1.EntityPlayer;
import net.minecraft.server.v1_12_R1.PacketPlayOutEntityDestroy;
import net.minecraft.server.v1_12_R1.PacketPlayOutSpawnEntity;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

import javax.inject.Inject;
import java.lang.reflect.Field;
import java.util.*;

public class HidingFeature extends AbstractFeature {

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
                EntityPlayer entityPlayer = ((CraftPlayer) player).getHandle();
                hiders.add(user);

                assignBlock(user);

                Location playerLoc = new Location(Bukkit.getWorld("highjacked"), -447, 40, 154);
                World world = Bukkit.getWorld("highjacked");
                Bukkit.getWorlds().forEach((w) -> Bukkit.getLogger().info(w.getName()));

                int playerEntityId = entityPlayer.getId();
                UUID playerEntityUuid = entityPlayer.getUniqueID();

                EntityFallingBlock entityFallingBlock = new EntityFallingBlock(((CraftWorld) world).getHandle());
                //entityFallingBlock.setLocation(playerLoc.getX(), playerLoc.getY(), playerLoc.getZ(), 0, 0);
                PacketPlayOutSpawnEntity packetPlayOutSpawnEntity = new PacketPlayOutSpawnEntity(entityFallingBlock, 70, playerBlockMap.get(user).getId());

                try {
                    Field blockEntityIdField = packetPlayOutSpawnEntity.getClass().getDeclaredField("a");
                    blockEntityIdField.setAccessible(true);
                    blockEntityIdField.setInt(null, playerEntityId);
                    blockEntityIdField.setAccessible(false);

                    Field blockEntityUuidField = packetPlayOutSpawnEntity.getClass().getDeclaredField("b");
                    blockEntityUuidField.setAccessible(true);
                    blockEntityUuidField.set(null, playerEntityUuid);
                    blockEntityUuidField.setAccessible(false);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                getPhase().getGame().getPlayers().forEach((otherU) -> {
                    if(otherU.equals(user)) {
                        return;
                    }

                    // despawn player
                    entityPlayer.playerConnection.sendPacket(new PacketPlayOutEntityDestroy(playerEntityId));
                    // create block with their id
                     entityPlayer.playerConnection.sendPacket(packetPlayOutSpawnEntity);
                });

                user.setPrefix(TextComponent.of("[H] ").color(TextColor.GREEN).append(user.getPrefix()));
                player.sendTitle("It's time to hide!", null, 1, 3, 0);
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
        return new Class[0];
    }
}
