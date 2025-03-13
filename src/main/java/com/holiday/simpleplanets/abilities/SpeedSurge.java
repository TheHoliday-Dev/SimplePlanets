package com.holiday.simpleplanets.abilities;

import com.holiday.simpleplanets.Main;
import com.holiday.simpleplanets.abstracts.AbstractAbilities;
import com.holiday.simpleplanets.enums.AbilitiesSlot;
import com.holiday.simpleplanets.interfaces.Abilities;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.UUID;

public class SpeedSurge extends AbstractAbilities implements Abilities {

    private final HashMap<UUID, Long> cooldowns = new HashMap<>();
    private static final int COOLDOWN_TIME = 50; // Cooldown in seconds
    private static final int DURATION = 10; // Duration of the effect in seconds

    public SpeedSurge(Main main) {
        super("SpeedSurge", "Mercury", main, AbilitiesSlot.THIRD);
    }

    @Override
    public void startAbilities(PlayerInteractEvent event, Player caster) {
        if (!isElement(caster)) {
            return;
        }

        if (!isLeft(event.getAction())) {
            return;
        }

        UUID playerId = caster.getUniqueId();
        long currentTime = System.currentTimeMillis();

        if (cooldowns.containsKey(playerId)) {
            long lastUsed = cooldowns.get(playerId);
            long timeLeft = (lastUsed + COOLDOWN_TIME * 1000) - currentTime;

            if (timeLeft > 0) {
                caster.sendMessage(ChatColor.RED + "SpeedSurge is on cooldown for " + (timeLeft / 1000) + " seconds.");
                return;
            }
        }

        caster.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, DURATION * 20, 1)); // 50% speed boost (level 1)
        caster.sendMessage(ChatColor.GREEN + "You feel a surge of speed!");

        cooldowns.put(playerId, currentTime);

        Bukkit.getScheduler().runTaskLaterAsynchronously(Main.getInstance(), () -> {
            if (caster.isOnline()) {
                caster.sendMessage(ChatColor.YELLOW + "SpeedSurge is now ready to use again!");
            }
        }, COOLDOWN_TIME * 20L);
    }

    @Override
    public int getCooldown(Player player) {
        if (cooldowns.containsKey(player.getUniqueId())) {
            long lastUsed = cooldowns.get(player.getUniqueId());
            long timeLeft = (lastUsed + COOLDOWN_TIME * 1000) - System.currentTimeMillis();
            if (timeLeft > 0) {
                return (int) (timeLeft / 1000);
            }
        }
        return 0;
    }


    @Override
    public String getName() {
        return super.getName();
    }
}
