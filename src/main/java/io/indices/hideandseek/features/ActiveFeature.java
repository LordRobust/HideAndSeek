package io.indices.hideandseek.features;

import com.voxelgameslib.voxelgameslib.components.scoreboard.Scoreboard;
import com.voxelgameslib.voxelgameslib.feature.AbstractFeature;
import com.voxelgameslib.voxelgameslib.feature.features.ScoreboardFeature;

import net.kyori.text.TextComponent;

import org.bukkit.ChatColor;

import lombok.Setter;

public class ActiveFeature extends AbstractFeature {

    @Setter
    private Scoreboard scoreboard;

    @Override
    public void init() {

    }

    @Override
    public void start() {
        getPhase().getGame().getPlayers().forEach(user -> user.sendMessage(TextComponent.of(ChatColor.RED + "Ready or not, here they come!")));
    }

    @Override
    public void stop() {

    }

    @Override
    public void tick() {

    }

    @Override
    public Class[] getDependencies() {
        return new Class[]{GameFeature.class, ScoreboardFeature.class};
    }
}
