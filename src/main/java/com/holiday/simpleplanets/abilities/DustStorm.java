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
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.UUID;

public class DustStorm extends AbstractAbilities implements Abilities {

    private final HashMap<UUID, Long> cooldowns = new HashMap<>();
    private static final int COOLDOWN_TIME = 20; // Cooldown in seconds
    private static final int RADIUS = 5; // Effect radius
    private static final int DAMAGE = 2; // Damage per second
    private static final int DURATION = 5; // Duration in seconds

    public DustStorm(Main main) {
        super("Dust Storm", "Mars", main, AbilitiesSlot.PRIMARY);
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
                caster.sendMessage(ChatColor.RED + "Dust Storm is on cooldown for " + (timeLeft / 1000) + " seconds.");
                return;
            }
        }

        // Activate ability
        caster.sendMessage(ChatColor.DARK_GREEN + "You summon a Dust Storm!");
        Location center = caster.getLocation();

        // Play sound effect
        caster.getWorld().playSound(center, Sound.WEATHER_RAIN, 2.0f, 1.0f);

        new BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run() {
                if (ticks >= DURATION) { // Stop after the duration
                    this.cancel();
                    return;
                }

                // Spawn particles (increased by a factor of 8)
                for (int i = 0; i < 80; i++) {  // Multiply 10 by 8 to get 80 particles per tick
                    double offsetX = (Math.random() - 0.5) * 2 * RADIUS;
                    double offsetY = Math.random() * 2; // Height of the storm
                    double offsetZ = (Math.random() - 0.5) * 2 * RADIUS;

                    Location particleLocation = center.clone().add(offsetX, offsetY, offsetZ);
                    caster.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, particleLocation, 1, 0.2, 0.2, 0.2, 0);
                }

                // Apply effects to entities within the radius
                for (Entity entity : caster.getNearbyEntities(RADIUS, RADIUS, RADIUS)) {
                    if (entity instanceof LivingEntity entity1) {

                        if (entity instanceof Player && entity.equals(caster)) {
                            continue; // Skip the caster
                        }

                        if (entity1 instanceof Player playerTarget) {
                            playerTarget.setHealth(Math.max(playerTarget.getHealth() - ((double) DAMAGE / 2), 0)); // Reduce health but don't go below 0

                            playerTarget.damage(1);
                        } else {
                            entity1.damage(DAMAGE);
                        }
                        entity1.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 40, 1)); // Reduce visibility
                    }
                }
                ticks++;
            }
        }.runTaskTimer(Main.getInstance(), 0L, 20L); // Run every second (20 ticks)

        // Set cooldown
        cooldowns.put(playerId, currentTime);

        // Notify cooldown expiration
        Bukkit.getScheduler().runTaskLaterAsynchronously(Main.getInstance(), () -> {
            if (caster.isOnline()) {
                caster.sendMessage(ChatColor.YELLOW + "Dust Storm is now ready to use again!");
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
