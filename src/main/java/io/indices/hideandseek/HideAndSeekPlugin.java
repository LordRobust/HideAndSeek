package io.indices.hideandseek;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.voxelgameslib.voxelgameslib.game.GameHandler;
import com.voxelgameslib.voxelgameslib.game.GameMode;
import com.voxelgameslib.voxelgameslib.module.Module;
import com.voxelgameslib.voxelgameslib.module.ModuleHandler;
import com.voxelgameslib.voxelgameslib.module.ModuleInfo;

import org.bukkit.plugin.java.JavaPlugin;

@Singleton
@ModuleInfo(name = "HideAndSeek", authors = "aphel", version = "1.0")
public class HideAndSeekPlugin extends JavaPlugin implements Module {
    public static final GameMode GAMEMODE = new GameMode("HideAndSeek", HideAndSeekGame.class);

    @Inject
    private GameHandler gameHandler;

    @Override
    public void onLoad() {
        ModuleHandler.offerModule(this);
    }

    @Override
    public void enable() {
        gameHandler.registerGameMode(GAMEMODE);
    }

    @Override
    public void disable() {
        //
    }
}
