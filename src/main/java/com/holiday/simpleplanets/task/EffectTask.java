package com.holiday.simpleplanets.task;

import com.holiday.simpleplanets.Main;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class EffectTask extends BukkitRunnable {
    @Override
    public void run() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            String element = Main.getInstance().getPlayerDataManager().getElement(player.getUniqueId());
            if (element != null) {

                switch (element) {
                    case "Sun":
                        player.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, 200, 1, false, false));
                        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 200, 1, false, false));
                        continue;
                    case "Mercury":
                        player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 200, 0, false, false));
                        continue;
                    case "Earth":
                        player.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, 200, 0, false, false));
                        continue;
                    case "Mars":
                        player.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 200, 0, false, false));
                        continue;
                    case "Venus":
                        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 200, 0, false, false));
                        continue;
                }
            }
        }
    }

    public void clearEffects(Player player) {
        for (PotionEffect effect : player.getActivePotionEffects()) {
            player.removePotionEffect(effect.getType());
        }
    }
}
