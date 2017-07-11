package io.indices.hideandseek.features;

import io.indices.hideandseek.hideandseek.HideAndSeekPlayer;
import lombok.Setter;
import me.minidigger.voxelgameslib.feature.AbstractFeature;
import me.minidigger.voxelgameslib.feature.features.ScoreboardFeature;
import me.minidigger.voxelgameslib.scoreboard.Scoreboard;
import me.minidigger.voxelgameslib.user.User;
import org.apache.commons.lang.time.DurationFormatUtils;

import java.util.*;

public class GraceFeature extends AbstractFeature {

    @Setter
    private Scoreboard scoreboard;
    @Setter
    private int ticks;
    private int elapsedTicks;

    private List<User> hiders = new ArrayList<>();
    private List<User> seekers = new ArrayList<>();
    private Map<UUID, HideAndSeekPlayer> playerMap = new HashMap<>();

    @Override
    public void init() {

    }

    @Override
    public void start() {
        Object gameStarted = getPhase().getGame().getGameData("gameStarted");
        Object hidersData = getPhase().getGame().getGameData("hiders");
        Object seekersData = getPhase().getGame().getGameData("seekers");
        Object playerMapData = getPhase().getGame().getGameData("playerMap");

        if (hidersData != null && (hidersData instanceof List)) {
            hiders = (List<User>) hidersData;
        }

        if (seekersData != null && (seekersData instanceof List)) {
            seekers = (List<User>) seekersData;
        }

        if (playerMapData != null && (playerMapData instanceof Map)) {
            playerMap = (Map<UUID, HideAndSeekPlayer>) playerMapData;
        }

        // any logic specific to the grace feature that does not relate directly to the gameplay mechanics
    }

    @Override
    public void stop() {

    }

    @Override
    public void tick() {
        elapsedTicks++;
        // update time left
        scoreboard.getLine(5).ifPresent((line) -> {
            line.setValue("" + DurationFormatUtils.formatDuration((long) Math.ceil((ticks - elapsedTicks) / 20) * 1000, "mm:ss"));
            scoreboard.addLine(5, line);
        });
    }

    @Override
    public Class[] getDependencies() {
        return new Class[]{GameFeature.class, ScoreboardFeature.class};
    }
}
