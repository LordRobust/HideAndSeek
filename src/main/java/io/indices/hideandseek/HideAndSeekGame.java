package io.indices.hideandseek;

import javax.annotation.Nonnull;

import me.minidigger.voxelgameslib.game.AbstractGame;
import me.minidigger.voxelgameslib.game.GameDefinition;
import me.minidigger.voxelgameslib.game.GameInfo;
import me.minidigger.voxelgameslib.phase.phases.LobbyPhase;
import me.minidigger.voxelgameslib.phase.phases.VotePhase;

import io.indices.hideandseek.phases.HideAndSeekPhase;
import io.indices.hideandseek.phases.HidingPhase;

@GameInfo(name = "HideAndSeek", author = "aphel", version = "1.0", description = "Everyone knows hide and seek!")
public class HideAndSeekGame extends AbstractGame {

    public HideAndSeekGame() {
        super(HideAndSeekPlugin.GAMEMODE);
    }

    @Override
    public void initGameFromModule() {
        setMinPlayers(2); // todo raise for production
        setMaxPlayers(2);

        LobbyPhase lobbyPhase = createPhase(LobbyPhase.class);
        VotePhase votePhase = createPhase(VotePhase.class);
        HidingPhase hidingPhase = createPhase(HidingPhase.class);
        HideAndSeekPhase mainPhase = createPhase(HideAndSeekPhase.class);

        lobbyPhase.setNextPhase(votePhase);
        votePhase.setNextPhase(hidingPhase);
        hidingPhase.setNextPhase(mainPhase);

        activePhase = lobbyPhase;

        loadMap();
    }

    @Override
    public void initGameFromDefinition(@Nonnull GameDefinition gameDefinition) {
        super.initGameFromDefinition(gameDefinition);

        loadMap();
    }
}
