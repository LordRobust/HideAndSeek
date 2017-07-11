package io.indices.hideandseek.phases;

import io.indices.hideandseek.features.GameFeature;
import io.indices.hideandseek.features.GraceFeature;
import me.minidigger.voxelgameslib.GameConstants;
import me.minidigger.voxelgameslib.feature.features.*;
import me.minidigger.voxelgameslib.phase.TimedPhase;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;

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
