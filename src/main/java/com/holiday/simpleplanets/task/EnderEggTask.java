package com.holiday.simpleplanets.task;

import com.holiday.simpleplanets.Main;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Arrays;

public class EnderEggTask extends BukkitRunnable {
    @Override
    public void run() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            Inventory inventory = player.getInventory();
            // Check if inventory contains an END_DRAGON_SPAWN_EGG
            boolean hasEnderEgg = Arrays.stream(inventory.getContents())
                    .anyMatch(item -> item != null && item.getType() == Material.DRAGON_EGG);

            if (hasEnderEgg) {
                // If the player is not already Sun, change their element to Sun
                if (!Main.getInstance().getPlayerDataManager().getElement(player.getUniqueId()).equalsIgnoreCase("Sun")) {
                    Main.getInstance().getPlayerDataManager().setBeforeElement(player.getUniqueId(), Main.getInstance().getPlayerDataManager().getElement(player.getUniqueId()));
                    Main.getInstance().getPlayerDataManager().setElement(player.getUniqueId(), "Sun");
                }
            } else {
                // If the player has Sun as their element, revert it back to before element
                if (Main.getInstance().getPlayerDataManager().getElement(player.getUniqueId()).equalsIgnoreCase("Sun")) {
                    String before = Main.getInstance().getPlayerDataManager().getBeforeElement(player.getUniqueId());
                    Main.getInstance().getPlayerDataManager().setElement(player.getUniqueId(), before);
                }
            }
        }
    }
}
