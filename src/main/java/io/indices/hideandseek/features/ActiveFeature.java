package io.indices.hideandseek.features;

import net.kyori.text.TextComponent;

import javax.annotation.Nonnull;

import com.voxelgameslib.voxelgameslib.components.scoreboard.Scoreboard;
import com.voxelgameslib.voxelgameslib.feature.AbstractFeature;
import com.voxelgameslib.voxelgameslib.feature.features.ScoreboardFeature;

import org.bukkit.ChatColor;

import lombok.Setter;

public class ActiveFeature extends AbstractFeature {

    @Setter
    private Scoreboard scoreboard;

    @Override
    public void start() {
        getPhase().getGame().getPlayers().forEach(user -> user.sendMessage(TextComponent.of(ChatColor.RED + "Ready or not, here they come!")));
    }

    @Override
    @Nonnull
    public Class[] getDependencies() {
        return new Class[]{GameFeature.class, ScoreboardFeature.class};
    }
}
