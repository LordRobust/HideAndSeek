package io.indices.hideandseek.features;

import me.minidigger.voxelgameslib.feature.AbstractFeature;
import me.minidigger.voxelgameslib.scoreboard.Scoreboard;
import me.minidigger.voxelgameslib.user.User;
import me.minidigger.voxelgameslib.user.UserHandler;
import net.kyori.text.TextComponent;
import net.kyori.text.format.TextColor;
import net.minecraft.server.v1_12_R1.EntityFallingBlock;
import net.minecraft.server.v1_12_R1.PacketPlayOutSpawnEntity;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.material.MaterialData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import javax.inject.Inject;
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
        int seekerAmount = playerCount / 10; // one seeker per 10 players

        for(int i = 0; i < seekerAmount; i++) {
            int n = random.nextInt(playerCount - 1);

            seekers.add(getPhase().getGame().getPlayers().get(n));
        }

        getPhase().getGame().getPlayers().forEach((user -> {
            if(!seekers.contains(user)) {
                Player player = user.getPlayer();
                hiders.add(user);

                assignBlock(user);

                player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 0, false, false));

                Location playerLoc = new Location(Bukkit.getWorld("highjacked"), -447, 40, 154);
                World world = Bukkit.getWorld("highjacked");
                Bukkit.getWorlds().forEach((w) -> Bukkit.getLogger().info(w.getName()));

                EntityFallingBlock entityFallingBlock = new EntityFallingBlock(((CraftWorld) world).getHandle());
                entityFallingBlock.setLocation(playerLoc.getX(), playerLoc.getY(), playerLoc.getZ(), 0, 0);
                PacketPlayOutSpawnEntity packetPlayOutSpawnEntity = new PacketPlayOutSpawnEntity(entityFallingBlock, 70, (playerBlockMap.get(user).getId()));

                getPhase().getGame().getPlayers().forEach((otherU) -> {
                    ((CraftPlayer) otherU.getPlayer()).getHandle().playerConnection.sendPacket(packetPlayOutSpawnEntity);
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
