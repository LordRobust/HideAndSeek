package io.indices.hideandseek.features;

import org.apache.commons.lang.time.DurationFormatUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.voxelgameslib.voxelgameslib.components.scoreboard.Scoreboard;
import com.voxelgameslib.voxelgameslib.feature.AbstractFeature;
import com.voxelgameslib.voxelgameslib.feature.features.ScoreboardFeature;
import com.voxelgameslib.voxelgameslib.user.User;

import io.indices.hideandseek.HideAndSeekGameData;
import io.indices.hideandseek.hideandseek.HideAndSeekPlayer;
import lombok.Setter;

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
        HideAndSeekGameData gameData = getPhase().getGame().getGameData(HideAndSeekGameData.class).orElse(new HideAndSeekGameData());

        hiders = gameData.hiders;
        seekers = gameData.seekers;
        playerMap = gameData.playerMap;

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
