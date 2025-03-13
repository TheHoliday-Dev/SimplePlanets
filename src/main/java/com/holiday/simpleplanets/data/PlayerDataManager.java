package com.holiday.simpleplanets.data;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.UUID;

public class PlayerDataManager {

    private final File file;
    private final FileConfiguration playerData;

    public PlayerDataManager() {
        file = new File(Bukkit.getPluginManager().getPlugin("SimplePlanets").getDataFolder(), "playerdata.yml");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        playerData = YamlConfiguration.loadConfiguration(file);
    }

    public void saveData() {
        try {
            playerData.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getBeforeElement(UUID playerUUID) {
        return playerData.getString(playerUUID + ".element_before");
    }

    public void setBeforeElement(UUID playerUUID, String element) {
        playerData.set(playerUUID + ".element_before", element);
        saveData();
    }


    public void setElement(UUID playerUUID, String name) {
        playerData.set(playerUUID + ".element", name);
        saveData();
    }

    public String getElement(UUID playerUUID) {
        String name = playerData.getString(playerUUID + ".element");
        return name == null ? "" : name;
    }

    public boolean existPlayerData(UUID playerUUID) {
        Set<String> keys = playerData.getKeys(false);
        for (String key : keys) {
            if (key.equals(playerUUID.toString())) {
                return true;
            }
        }

        return false;
    }
}
