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

public class ExtremeHeat extends AbstractAbilities implements Abilities {

    private final HashMap<UUID, Long> cooldowns = new HashMap<>();
    private static final int COOLDOWN_TIME = 30; // Cooldown in seconds
    private static final int DURATION = 5; // Duration of the burn effect in seconds
    private static final int DAMAGE_PER_SECOND = 1; // Damage dealt per second
    private static final int RADIUS = 5; // Effect radius

    public ExtremeHeat(Main main) {
        super("Extreme Heat", "Venus", main, AbilitiesSlot.SECONDARY);
    }

    @Override
    public void startAbilities(PlayerInteractEvent event, Player caster) {
        if (!isElement(caster)) {
            return;
        }

        if (caster.isSneaking()) {
            return;
        }

        if (isLeft(event.getAction())) {
            return;
        }

        UUID playerId = caster.getUniqueId();
        long currentTime = System.currentTimeMillis();

        if (cooldowns.containsKey(playerId)) {
            long lastUsed = cooldowns.get(playerId);
            long timeLeft = (lastUsed + COOLDOWN_TIME * 1000) - currentTime;

            if (timeLeft > 0) {
                caster.sendMessage(ChatColor.RED + "Extreme Heat is on cooldown for " + (timeLeft / 1000) + " seconds.");
                return;
            }
        }

        // Activate ability
        caster.sendMessage(ChatColor.GOLD + "You unleash Extreme Heat!");
        Location center = caster.getLocation();

        // Play initial sound
        caster.getWorld().playSound(center, Sound.ENTITY_CREEPER_HURT, 1.5f, 1.0f);

        new BukkitRunnable() {
            int secondsPassed = 0;

            @Override
            public void run() {
                if (secondsPassed >= DURATION) {
                    cancel();
                    return;
                }

                // Generate 5 circles stacked vertically
                for (int j = 0; j < 5; j++) {  // 5 layers of particles
                    double heightOffset = 0.5 * j;  // Spacing between the layers

                    // Create a circle at each height layer
                    for (int i = 0; i < 360; i += 15) {  // Creating a circle every 15 degrees
                        double angle = Math.toRadians(i);
                        double x = RADIUS * Math.cos(angle);
                        double z = RADIUS * Math.sin(angle);
                        Location particleLocation = center.clone().add(x, 1.0 + heightOffset, z);  // Adjust height with offset
                        caster.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, particleLocation, 2, 0, 0, 0, 0.05);  // Increased particle count
                    }
                }

                // Damage entities in the radius
                for (Entity entity : center.getWorld().getNearbyEntities(center, RADIUS, RADIUS, RADIUS)) {
                    if (entity instanceof LivingEntity target && !entity.equals(caster)) {
                        if (target instanceof Player playerTarget) {
                            playerTarget.setHealth(playerTarget.getHealth() - ((double) DAMAGE_PER_SECOND / 2));
                            playerTarget.damage(1);
                        } else {
                            target.damage(DAMAGE_PER_SECOND, caster);
                        }
                        target.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 20, 1));  // Apply poison effect
                    }
                }
                secondsPassed++;
            }
        }.runTaskTimer(Main.getInstance(), 0, 20L); // Run every second
        // Set cooldown
        cooldowns.put(playerId, currentTime);

        // Notify cooldown expiration
        Bukkit.getScheduler().runTaskLaterAsynchronously(Main.getInstance(), () -> {
            if (caster.isOnline()) {
                caster.sendMessage(ChatColor.YELLOW + "Extreme Heat is now ready to use again!");
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
