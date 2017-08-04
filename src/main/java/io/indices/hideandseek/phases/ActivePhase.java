package io.indices.hideandseek.phases;

import io.indices.hideandseek.features.ActiveFeature;
import io.indices.hideandseek.features.GameFeature;
import com.voxelgameslib.voxelgameslib.GameConstants;
import com.voxelgameslib.voxelgameslib.feature.features.*;
import com.voxelgameslib.voxelgameslib.phase.TimedPhase;
import org.bukkit.GameMode;

public class ActivePhase extends TimedPhase {

    @Override
    public void init() {
        setName("ActivePhase");
        setTicks(2 * 60 * GameConstants.TPS); // todo set to 10 minutes
        super.init();
        setAllowJoin(false);
        setAllowSpectate(true);

        MapFeature mapFeature = getGame().createFeature(MapFeature.class, this);
        mapFeature.setShouldUnload(true);
        addFeature(mapFeature);

        SpawnFeature spawnFeature = getGame().createFeature(SpawnFeature.class, this);
        spawnFeature.setRespawn(true);
        spawnFeature.setInitialSpawn(false);
        addFeature(spawnFeature);

        GameModeFeature gameModeFeature = getGame().createFeature(GameModeFeature.class, this);
        gameModeFeature.setGameMode(GameMode.ADVENTURE);
        addFeature(gameModeFeature);

        ClearInventoryFeature clearInventoryFeature = getGame().createFeature(ClearInventoryFeature.class, this);
        addFeature(clearInventoryFeature);

        HealFeature healFeature = getGame().createFeature(HealFeature.class, this);
        addFeature(healFeature);

        NoHungerLossFeature noHungerLossFeature = getGame().createFeature(NoHungerLossFeature.class, this);
        addFeature(noHungerLossFeature);

        ScoreboardFeature scoreboardFeature = getGame().createFeature(ScoreboardFeature.class, this);
        addFeature(scoreboardFeature);

        // main game feature
        GameFeature gameFeature = getGame().createFeature(GameFeature.class, this);
        gameFeature.setScoreboard(scoreboardFeature.getScoreboard());
        addFeature(gameFeature);

        // game is active
        ActiveFeature activeFeature = getGame().createFeature(ActiveFeature.class, this);
        addFeature(activeFeature);
    }
}
