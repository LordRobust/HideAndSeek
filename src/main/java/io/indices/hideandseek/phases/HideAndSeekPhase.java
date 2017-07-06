package io.indices.hideandseek.phases;

import io.indices.hideandseek.features.HideAndSeekFeature;
import me.minidigger.voxelgameslib.GameConstants;
import me.minidigger.voxelgameslib.feature.features.*;
import me.minidigger.voxelgameslib.phase.TimedPhase;
import org.bukkit.GameMode;

public class HideAndSeekPhase extends TimedPhase {

    @Override
    public void init() {
        setName("HideAndSeekPhase");
        setTicks(1 * 60 * GameConstants.TPS); // todo set to 10 minutes
        super.init();
        setAllowJoin(false);
        setAllowSpectate(true);

        MapFeature mapFeature = getGame().createFeature(MapFeature.class, this);
        mapFeature.setShouldUnload(true);
        addFeature(mapFeature);

        SpawnFeature spawnFeature = getGame().createFeature(SpawnFeature.class, this);
        spawnFeature.setRespawn(true);
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
        HideAndSeekFeature hideAndSeekFeature = getGame().createFeature(HideAndSeekFeature.class, this);
        hideAndSeekFeature.setScoreboard(scoreboardFeature.getScoreboard());
        addFeature(hideAndSeekFeature);
    }
}
