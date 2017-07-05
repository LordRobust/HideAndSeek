package io.indices.hideandseek;

import io.indices.hideandseek.phases.HideAndSeekPhase;
import me.minidigger.voxelgameslib.game.AbstractGame;
import me.minidigger.voxelgameslib.game.GameDefinition;
import me.minidigger.voxelgameslib.game.GameInfo;
import me.minidigger.voxelgameslib.phase.phases.GracePhase;
import me.minidigger.voxelgameslib.phase.phases.LobbyPhase;
import me.minidigger.voxelgameslib.phase.phases.VotePhase;

import javax.annotation.Nonnull;
import javax.inject.Singleton;

@Singleton
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
        GracePhase gracePhase = createPhase(GracePhase.class);
        HideAndSeekPhase mainPhase = createPhase(HideAndSeekPhase.class);

        lobbyPhase.setNextPhase(votePhase);
        lobbyPhase.setNextPhase(gracePhase);
        lobbyPhase.setNextPhase(mainPhase);

        activePhase = lobbyPhase;

        loadMap();
    }

    @Override
    public void initGameFromDefinition(@Nonnull GameDefinition gameDefinition) {
        super.initGameFromDefinition(gameDefinition);

        loadMap();
    }
}
