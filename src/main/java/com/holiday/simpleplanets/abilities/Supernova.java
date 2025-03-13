package com.holiday.simpleplanets.abilities;

import com.holiday.simpleplanets.Main;
import com.holiday.simpleplanets.abstracts.AbstractAbilities;
import com.holiday.simpleplanets.enums.AbilitiesSlot;
import com.holiday.simpleplanets.interfaces.Abilities;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.HashMap;
import java.util.UUID;

public class Supernova extends AbstractAbilities implements Abilities {

    private final HashMap<UUID, Long> cooldowns = new HashMap<>();
    private static final int COOLDOWN_TIME = 60; // Cooldown in seconds
    private static final double DAMAGE = 8.0; // Damage dealt (7 hearts)
    private static final int RADIUS = 15; // Effect radius (15 blocks)

    public Supernova(Main main) {
        super("Super Nova", "Sun", main, AbilitiesSlot.THIRD);
    }

    @Override
    public void startAbilities(PlayerInteractEvent event, Player caster) {
        if (!isElement(caster)) {
            return;
        }

        if (!isRightShift(event.getAction(), event.getPlayer())) {
            return;
        }

        UUID playerId = caster.getUniqueId();
        long currentTime = System.currentTimeMillis();

        if (cooldowns.containsKey(playerId)) {
            long lastUsed = cooldowns.get(playerId);
            long timeLeft = (lastUsed + COOLDOWN_TIME * 1000) - currentTime;

            if (timeLeft > 0) {
                caster.sendMessage(ChatColor.RED + "Supernova is on cooldown for " + (timeLeft / 1000) + " seconds.");
                return;
            }
        }

        // Execute the ability
        caster.sendMessage(ChatColor.GOLD + "You unleash a devastating Supernova!");
        Location casterLocation = caster.getLocation();

        // Play sound and create explosion effect
        caster.getWorld().playSound(casterLocation, Sound.ENTITY_GENERIC_EXPLODE, 2.0f, 0.8f);
        createExplosionEffect(casterLocation);

        // Apply damage and ignite entities
        for (Entity entity : caster.getWorld().getNearbyEntities(casterLocation, RADIUS, RADIUS, RADIUS)) {
            if (entity instanceof LivingEntity target && !entity.equals(caster)) {
                // If the target is a player, reduce health directly
                if (target instanceof Player targetPlayer) {
                    double newHealth = Math.max(targetPlayer.getHealth() - DAMAGE, 0); // Ensure health doesn't go below 0
                    targetPlayer.setHealth(newHealth);

                } else {
                    // For non-player entities, apply damage normally
                    target.damage(DAMAGE, caster);
                }

                target.setFireTicks(100); // Ignite for 5 seconds
                target.getWorld().playSound(target.getLocation(), Sound.ENTITY_BLAZE_HURT, 1.0f, 1.0f);
            }
        }

        // Set cooldown
        cooldowns.put(playerId, currentTime);

        // Notify cooldown expiration
        Bukkit.getScheduler().runTaskLaterAsynchronously(Main.getInstance(), () -> {
            if (caster.isOnline()) {
                caster.sendMessage(ChatColor.YELLOW + "Supernova is now ready to use again!");
            }
        }, COOLDOWN_TIME * 20L);
    }

    private void createExplosionEffect(Location center) {
        Bukkit.getScheduler().runTaskAsynchronously(Main.getInstance(), () -> {
            for (int radius = 0; radius <= RADIUS; radius++) {
                int currentRadius = radius;
                Bukkit.getScheduler().runTaskLater(Main.getInstance(), () -> {
                    for (double angle = 0; angle < 360; angle += 10) {
                        double radians = Math.toRadians(angle);
                        double x = currentRadius * Math.cos(radians);
                        double z = currentRadius * Math.sin(radians);
                        Location particleLocation = center.clone().add(x, 0, z);

                        // Add height variation for the dome-like effect
                        for (double y = -0.5; y <= 0.5; y += 0.25) {
                            Location adjustedLocation = particleLocation.clone().add(0, y, 0);
                            center.getWorld().spawnParticle(
                                    Particle.FLAME,
                                    adjustedLocation,
                                    2, // Particle count
                                    0, 0, 0, // Offset
                                    0.05 // Speed
                            );
                        }
                    }
                }, radius * 3L); // Delay between each radius step
            }
        });
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
