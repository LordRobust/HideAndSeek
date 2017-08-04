package io.indices.hideandseek.phases;

import com.voxelgameslib.voxelgameslib.GameConstants;
import com.voxelgameslib.voxelgameslib.feature.features.ClearInventoryFeature;
import com.voxelgameslib.voxelgameslib.feature.features.GameModeFeature;
import com.voxelgameslib.voxelgameslib.feature.features.HealFeature;
import com.voxelgameslib.voxelgameslib.feature.features.MapFeature;
import com.voxelgameslib.voxelgameslib.feature.features.NoBlockBreakFeature;
import com.voxelgameslib.voxelgameslib.feature.features.NoBlockPlaceFeature;
import com.voxelgameslib.voxelgameslib.feature.features.NoDamageFeature;
import com.voxelgameslib.voxelgameslib.feature.features.ScoreboardFeature;
import com.voxelgameslib.voxelgameslib.feature.features.SpawnFeature;
import com.voxelgameslib.voxelgameslib.phase.TimedPhase;

import org.bukkit.GameMode;

import io.indices.hideandseek.features.GameFeature;
import io.indices.hideandseek.features.GraceFeature;

public class GracePhase extends TimedPhase {

    @Override
    public void init() {
        int ticks = 30 * GameConstants.TPS;

        setName("GracePhase");
        setTicks(ticks); // 30 seconds
        super.init();
        setAllowJoin(false);
        setAllowSpectate(true);

        MapFeature mapFeature = getGame().createFeature(MapFeature.class, this);
        mapFeature.setShouldUnload(true);
        addFeature(mapFeature);

        SpawnFeature spawnFeature = getGame().createFeature(SpawnFeature.class, this);
        spawnFeature.setInitialSpawn(true);
        addFeature(spawnFeature);

        NoBlockBreakFeature noBlockBreakFeature = getGame()
                .createFeature(NoBlockBreakFeature.class, this);
        addFeature(noBlockBreakFeature);

        NoBlockPlaceFeature noBlockPlaceFeature = getGame()
                .createFeature(NoBlockPlaceFeature.class, this);
        addFeature(noBlockPlaceFeature);

        ClearInventoryFeature clearInventoryFeature = getGame()
                .createFeature(ClearInventoryFeature.class, this);
        addFeature(clearInventoryFeature);

        NoDamageFeature noDamageFeature = getGame().createFeature(NoDamageFeature.class, this);
        addFeature(noDamageFeature);

        HealFeature healFeature = getGame().createFeature(HealFeature.class, this);
        addFeature(healFeature);

        GameModeFeature gameModeFeature = getGame().createFeature(GameModeFeature.class, this);
        gameModeFeature.setGameMode(GameMode.ADVENTURE);
        addFeature(gameModeFeature);

        ScoreboardFeature scoreboardFeature = getGame().createFeature(ScoreboardFeature.class, this);
        addFeature(scoreboardFeature);

        // main game feature
        GameFeature gameFeature = getGame().createFeature(GameFeature.class, this);
        gameFeature.setScoreboard(scoreboardFeature.getScoreboard());
        gameFeature.setTicks(ticks);
        addFeature(gameFeature);

        // game is in grace period
        GraceFeature graceFeature = getGame().createFeature(GraceFeature.class, this);
        graceFeature.setScoreboard(scoreboardFeature.getScoreboard());
        graceFeature.setTicks(ticks);
        addFeature(graceFeature);
    }
}
