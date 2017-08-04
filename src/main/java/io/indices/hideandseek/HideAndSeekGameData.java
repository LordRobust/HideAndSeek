package io.indices.hideandseek;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.voxelgameslib.voxelgameslib.game.GameData;
import com.voxelgameslib.voxelgameslib.user.User;

import io.indices.hideandseek.hideandseek.HideAndSeekPlayer;

public class HideAndSeekGameData implements GameData{

    public boolean gameStarted = false;
    public List<User> hiders = new ArrayList<>();
    public List<User> seekers = new ArrayList<>();
    public Map<UUID, HideAndSeekPlayer> playerMap;
}
