package com.holiday.simpleplanets.abilities;

import com.holiday.simpleplanets.Main;
import com.holiday.simpleplanets.abstracts.AbstractAbilities;
import com.holiday.simpleplanets.enums.AbilitiesSlot;
import com.holiday.simpleplanets.interfaces.Abilities;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.HashMap;
import java.util.UUID;

public class SelfHeal extends AbstractAbilities implements Abilities {

    private final HashMap<UUID, Long> cooldowns = new HashMap<>();
    private static final int COOLDOWN_TIME = 30; // Cooldown in seconds
    private static final int HEAL_AMOUNT = 10; // Heal amount in health points (5 hearts)

    public SelfHeal(Main main) {
        super("Self-Heal", "Earth", main, AbilitiesSlot.PRIMARY);
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
                caster.sendMessage(ChatColor.RED + "Self-Heal is on cooldown for " + (timeLeft / 1000) + " seconds.");
                return;
            }
        }

        // Activate ability
        caster.sendMessage(ChatColor.GREEN + "You heal yourself!");
        caster.setHealth(Math.min(caster.getHealth() + HEAL_AMOUNT, caster.getMaxHealth())); // Heal the player without exceeding max health

        // Play sound effect
        caster.getWorld().playSound(caster.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.5f, 1.0f);

        // Spawn particle effect
        caster.getWorld().spawnParticle(Particle.HEART, caster.getLocation().add(0, 1, 0), 10, 0.5, 0.5, 0.5, 0.05);

        // Set cooldown
        cooldowns.put(playerId, currentTime);

        // Notify cooldown expiration
        Bukkit.getScheduler().runTaskLaterAsynchronously(Main.getInstance(), () -> {
            if (caster.isOnline()) {
                caster.sendMessage(ChatColor.YELLOW + "Self-Heal is now ready to use again!");
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
