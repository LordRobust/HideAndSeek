package io.indices.hideandseek.features;

import com.comphenix.packetwrapper.WrapperPlayServerEntityDestroy;
import com.comphenix.packetwrapper.WrapperPlayServerMount;
import com.comphenix.packetwrapper.WrapperPlayServerRespawn;
import com.comphenix.packetwrapper.WrapperPlayServerSpawnEntity;
import com.comphenix.packetwrapper.WrapperPlayServerSpawnEntityLiving;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;

import net.kyori.text.TextComponent;
import net.kyori.text.format.TextColor;

import org.apache.commons.lang.time.DurationFormatUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.stream.Collectors;
import javax.inject.Inject;

import com.voxelgameslib.voxelgameslib.VoxelGamesLib;
import com.voxelgameslib.voxelgameslib.components.scoreboard.Scoreboard;
import com.voxelgameslib.voxelgameslib.feature.AbstractFeature;
import com.voxelgameslib.voxelgameslib.feature.features.MapFeature;
import com.voxelgameslib.voxelgameslib.feature.features.ScoreboardFeature;
import com.voxelgameslib.voxelgameslib.user.User;
import com.voxelgameslib.voxelgameslib.user.UserHandler;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import io.indices.hideandseek.HideAndSeekGameData;
import io.indices.hideandseek.hideandseek.HideAndSeekPlayer;
import io.indices.hideandseek.util.NmsUtil;
import lombok.Setter;

public class GameFeature extends AbstractFeature {

    @Inject
    private VoxelGamesLib plugin;
    @Inject
    private UserHandler userHandler;
    private ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();

    @Setter
    private Scoreboard scoreboard;

    private List<User> hiders = new ArrayList<>();
    private List<User> seekers = new ArrayList<>();

    private Material[] transformableBlocks = new Material[]{Material.BEACON, Material.SAND, Material.LAPIS_BLOCK};
    private Map<UUID, HideAndSeekPlayer> playerMap = new HashMap<>();
    private Map<Player, Long> movements = new WeakHashMap<>();

    @Setter
    private int ticks;
    private int nextBlock = 0;

    @Override
    public void init() {

    }

    @Override
    public Class[] getDependencies() {
        return new Class[]{MapFeature.class, ScoreboardFeature.class};
    }

    @Override
    public void start() {
        // load any existing data from previous phase(s) also using GameFeature

        HideAndSeekGameData gameData = getPhase().getGame().getGameData(HideAndSeekGameData.class).orElse(new HideAndSeekGameData());

        hiders = gameData.hiders;
        seekers = gameData.seekers;

        if (gameData.playerMap != null) {
            playerMap = gameData.playerMap;
        } else {
            // generate default values
            getPhase().getGame().getPlayers().forEach((user) -> {
                HideAndSeekPlayer hideAndSeekPlayer = new HideAndSeekPlayer();
                hideAndSeekPlayer.setUser(user);
                playerMap.put(user.getUuid(), hideAndSeekPlayer);
            });
        }

        if (!gameData.gameStarted) {
            // initialise game

            // choose seekers and hiders
            Random random = new Random();
            int playerCount = getPhase().getGame().getPlayers().size();
            int seekerAmount = (playerCount / 10) + 1; // one seeker per 10 players

            for (int i = 0; i < seekerAmount; i++) {
                int n = random.nextInt(playerCount - 1);

                seekers.add(getPhase().getGame().getPlayers().get(n));
            }

            getPhase().getGame().getPlayers().forEach((user) -> {
                if (!seekers.contains(user)) {
                    Player subject = user.getPlayer();
                    HideAndSeekPlayer subjectGamePlayer = playerMap.get(subject.getUniqueId());
                    hiders.add(user);

                    assignBlock(user);

                    //Bukkit.getScheduler().runTaskLater(plugin, () -> sendMorphPackets(subject, subjectGamePlayer.getBlock(), clients), 40); // run later to ensure entities spawn

                    // good luck
                    subject.sendTitle(ChatColor.GREEN + "You have 30 seconds to hide!", null);
                }
            });

            protocolManager.addPacketListener(new PacketAdapter(plugin, ListenerPriority.NORMAL,
                    PacketType.Play.Client.TELEPORT_ACCEPT) {
                @Override
                public void onPacketReceiving(PacketEvent event) {
                    hiders.forEach((hider) -> {
                        HideAndSeekPlayer subjectGamePlayer = playerMap.get(hider.getUuid());

                        if (!subjectGamePlayer.isStationary()) {
                            sendMorphPackets(hider.getPlayer(), subjectGamePlayer.getBlock(), event.getPlayer());
                        }
                    });
                }
            });

            seekers.forEach((seeker) -> {
                seeker.getPlayer().teleport(seeker.getPlayer().getLocation()); // todo teleport them to a locked location, dummy code for now
                seeker.sendMessage(TextComponent.of("Start counting down from 30...").color(TextColor.GREEN)); // todo create lang leys
            });
        }

        scoreboard.setTitle(ChatColor.AQUA + "Hide" + ChatColor.GREEN + "And" + ChatColor.YELLOW + "Seek");
        // todo improve VGL api to have string keys rather than having to edit int keys constantly
        scoreboard.createAndAddLine(7, ChatColor.RESET + "");
        scoreboard.createAndAddLine(6, ChatColor.RED + ChatColor.BOLD.toString() + "Grace Period Left");
        scoreboard.createAndAddLine(5, "" + DurationFormatUtils.formatDuration(ticks / 20 * 1000, "mm:ss"));
        scoreboard.createAndAddLine(4, ChatColor.RESET + ChatColor.RESET.toString() + "");
        scoreboard.createAndAddLine(3, ChatColor.AQUA + ChatColor.BOLD.toString() + "Players Alive");
        scoreboard.createAndAddLine(2, hiders.size() + " hiders");
        scoreboard.createAndAddLine(1, seekers.size() + " seekers");

        // save current data for other features to use
        gameData.hiders = hiders;
        gameData.seekers = seekers;
        gameData.playerMap = playerMap;
        gameData.gameStarted = true;
        getPhase().getGame().putGameData(gameData);
    }

    @Override
    public void stop() {
        HideAndSeekGameData gameData = getPhase().getGame().getGameData(HideAndSeekGameData.class).orElse(new HideAndSeekGameData());
        gameData.hiders = hiders;
        gameData.seekers = seekers;
        gameData.playerMap = playerMap;
        gameData.gameStarted = true;
        getPhase().getGame().putGameData(gameData);
    }

    @Override
    public void tick() {
        movements.forEach((player, time) -> {
            if (System.currentTimeMillis() - time > 3 * 1000) { // 3 seconds to become solid
                HideAndSeekPlayer hideAndSeekPlayer = playerMap.get(player.getUniqueId());
                if (!hideAndSeekPlayer.isStationary()) {
                    // they're ready to become a block

                    // send a falling entity to the player, to make them see the block but be able to move thru it
                    // to all other players, make it a solid block and destroy the falling block entity. recreate it when the player moves

                    solidifyPlayer(player);

                    player.sendTitle(ChatColor.GREEN + "You're now a solid block!", null);
                }
            }
        });
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        userHandler.getUser(event.getEntity().getUniqueId()).ifPresent((user -> {
            if (getPhase().getGame().getPlayers().contains(user)) {
                user.getPlayer().spigot().respawn();
                if (hiders.contains(user)) {
                    HideAndSeekPlayer hideAndSeekPlayer = playerMap.get(user.getUuid());
                    hideAndSeekPlayer.setStationaryLocation(null);
                    hiders.remove(user);
                    seekers.add(user); // you've joined the evil side now

                    // todo might need extra params for this to work
                    WrapperPlayServerRespawn respawnPacket = new WrapperPlayServerRespawn();
                    respawnPacket.sendPacket(user.getPlayer());
                }
            }
        }));
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        userHandler.getUser(event.getPlayer().getUniqueId()).ifPresent((user) -> {
            if ((event.getFrom().getBlockX() != event.getTo().getBlockX() ||
                    event.getFrom().getBlockY() != event.getTo().getBlockY() ||
                    event.getFrom().getBlockZ() != event.getTo().getBlockZ()) && hiders.contains(user)) {

                HideAndSeekPlayer hideAndSeekPlayer = playerMap.get(event.getPlayer().getUniqueId());

                movements.put(event.getPlayer(), System.currentTimeMillis());

                if (hideAndSeekPlayer.isStationary()) {
                    hideAndSeekPlayer.setStationaryLocation(null);

                    setHiddenPlayerVisible(hideAndSeekPlayer.getUser().getPlayer());
                }
            }
        });
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        // wont need that data anymore
        playerMap.remove(event.getPlayer().getUniqueId());
    }

    private void solidifyPlayer(Player subject) {
        HideAndSeekPlayer hideAndSeekPlayer = playerMap.get(subject.getUniqueId());

        // send packet to the subject to put a falling block entity on their position so they see the block
        // send packets to all other players to make it seem like a real block is there

        // send to subject:
        // create our falling block they are 'disguised' as
        WrapperPlayServerSpawnEntity blockSpawnPacket = new WrapperPlayServerSpawnEntity();
        int blockEntityId = NmsUtil.getNextEntityId();
        blockSpawnPacket.setEntityID(blockEntityId);
        blockSpawnPacket.setUniqueId(UUID.randomUUID());
        blockSpawnPacket.setType(70);
        blockSpawnPacket.setObjectData(hideAndSeekPlayer.getBlock().getId());

        blockSpawnPacket.setX(subject.getLocation().getBlockX());
        blockSpawnPacket.setY(subject.getLocation().getBlockY());
        blockSpawnPacket.setZ(subject.getLocation().getBlockZ());
        blockSpawnPacket.setPitch(0);
        blockSpawnPacket.setYaw(0);

        hideAndSeekPlayer.setStationaryLocation(subject.getLocation());
        hideAndSeekPlayer.setFakeBlockEntityId(blockEntityId);
        blockSpawnPacket.sendPacket(subject);

        // send a fake block to everyone else and destroy the entity
        getPhase().getGame().getPlayers().forEach((user) -> {
            // destroy the real player, client side
            WrapperPlayServerEntityDestroy destroyPacket = new WrapperPlayServerEntityDestroy();
            destroyPacket.setEntityIds(new int[]{hideAndSeekPlayer.getEntityId()});

            destroyPacket.sendPacket(user.getPlayer());
            user.getPlayer().sendBlockChange(subject.getLocation(), hideAndSeekPlayer.getBlock(), (byte) 0);
        });
    }

    private void setHiddenPlayerVisible(Player player) {
        HideAndSeekPlayer hideAndSeekPlayer = playerMap.get(player.getUniqueId());
        Location stationaryLocation = hideAndSeekPlayer.getStationaryLocation();

        // reverse solidifying the player
        List<Player> clients = getPhase().getGame().getPlayers().stream()
                .filter((u) -> !u.getUuid().equals(player.getUniqueId()))
                .map(User::getPlayer)
                .collect(Collectors.toList());

        sendMorphPackets(player, hideAndSeekPlayer.getBlock(), clients);

        hideAndSeekPlayer.setStationaryLocation(null);
    }

    /**
     * Assigns a player with a block
     *
     * @param user the user
     */
    private void assignBlock(User user) {
        if (nextBlock > (transformableBlocks.length - 1)) {
            nextBlock = 0;
        }

        HideAndSeekPlayer hideAndSeekPlayer = playerMap.get(user.getUuid());
        hideAndSeekPlayer.setBlock(transformableBlocks[nextBlock]);

        nextBlock++;
    }

    /**
     * Send packets to 'morph' a player into a block
     *
     * @param clients list of players to send packets to
     */
    public void sendMorphPackets(Player subject, Material block, List<Player> clients) {
        clients.forEach((client) -> sendMorphPackets(subject, block, client));
    }

    /**
     * Send packets to 'morph' a player into a block
     *
     * @param client player to send packets to
     */
    public void sendMorphPackets(Player subject, Material block, Player client) {
        int playerEntityId = subject.getEntityId();

        // i would rather use NMS, but i was convinced to use protocollib
        // welcome to the evil side, i suppose

        // destroy the real player, client side
        WrapperPlayServerEntityDestroy destroyPacket = new WrapperPlayServerEntityDestroy();
        destroyPacket.setEntityIds(new int[]{playerEntityId});

        // create our fake entity to become the player so block movement remains smooth
        // looks like server doesn't smoothly track non-living entities
        WrapperPlayServerSpawnEntityLiving morphedEntityPacket = new WrapperPlayServerSpawnEntityLiving();
        morphedEntityPacket.setEntityID(playerEntityId);
        morphedEntityPacket.setUniqueId(UUID.randomUUID());
        morphedEntityPacket.setType(EntityType.SLIME);

        Location location = subject.getLocation();
        morphedEntityPacket.setX(location.getX());
        morphedEntityPacket.setY(location.getY());
        morphedEntityPacket.setZ(location.getZ());
        morphedEntityPacket.setPitch(location.getPitch());
        morphedEntityPacket.setYaw(location.getYaw());

        WrappedDataWatcher morphedEntityDataWatcher = new WrappedDataWatcher();
        //morphedEntityDataWatcher.setObject(0, WrappedDataWatcher.Registry.get(Byte.class), (byte) 0x20, true); // set invisible
        morphedEntityDataWatcher.setObject(4, WrappedDataWatcher.Registry.get(Boolean.class), true, true);
        morphedEntityDataWatcher.setObject(5, WrappedDataWatcher.Registry.get(Boolean.class), true, true); // no gravity
        morphedEntityDataWatcher.setObject(12, WrappedDataWatcher.Registry.get(Integer.class), 0, true); // Set size
        morphedEntityPacket.setMetadata(morphedEntityDataWatcher);

        // create our falling block they are 'disguised' as
        WrapperPlayServerSpawnEntity blockSpawnPacket = new WrapperPlayServerSpawnEntity();
        int blockEntityId = NmsUtil.getNextEntityId();
        blockSpawnPacket.setEntityID(blockEntityId);
        blockSpawnPacket.setUniqueId(UUID.randomUUID());
        blockSpawnPacket.setType(70);
        blockSpawnPacket.setObjectData(block.getId());

        blockSpawnPacket.setX(location.getX());
        blockSpawnPacket.setY(location.getY());
        blockSpawnPacket.setZ(location.getZ());
        blockSpawnPacket.setPitch(location.getPitch());
        blockSpawnPacket.setYaw(location.getYaw());

        // set the falling block as a passenger of the fake entity
        WrapperPlayServerMount mount = new WrapperPlayServerMount();
        mount.setEntityID(subject.getEntityId());
        mount.setPassengerIds(new int[]{blockEntityId});

        // send packets to client
        destroyPacket.sendPacket(client);
        morphedEntityPacket.sendPacket(client);
        blockSpawnPacket.sendPacket(client);
        mount.sendPacket(client);
    }

    // for debug:
    /*@Override
    public AbstractFeatureCommand getCommandClass() {
        return new GameFeatureCommands();
    }

    @Singleton
    public class GameFeatureCommands extends AbstractFeatureCommand {

        @CommandAlias("disguise")
        @CommandPermission("%user")
        public void disguise(User user) {
            sendMorphPackets(user.getPlayer(), Material.BEACON, Bukkit.getOnlinePlayers().stream().filter((p) -> p.getUniqueId() != user.getUuid()).collect(Collectors.toList()));
        }

    }*/
}
