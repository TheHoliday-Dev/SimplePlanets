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

public class SolarFlareUp extends AbstractAbilities implements Abilities {

    private final HashMap<UUID, Long> cooldowns = new HashMap<>();
    private static final int COOLDOWN_TIME = 30; // Cooldown in seconds
    private static final double DAMAGE = 4.0; // Damage dealt (3 hearts)
    private static final int RADIUS = 10; // Effect radius (10 blocks)

    public SolarFlareUp(Main main) {
        super("Solar Flare", "Sun", main, AbilitiesSlot.PRIMARY);
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
                caster.sendMessage(ChatColor.RED + "Solar Flare is on cooldown for " + (timeLeft / 1000) + " seconds.");
                return;
            }
        }

        // Execute the ability
        caster.sendMessage(ChatColor.GOLD + "You unleash a massive Solar Flare!");
        Location casterLocation = caster.getLocation();

        // Play initial sound
        caster.getWorld().playSound(casterLocation, Sound.ENTITY_BLAZE_SHOOT, 1.5f, 1.0f);

        // Particle and damage effect
        Bukkit.getScheduler().runTaskAsynchronously(Main.getInstance(), () -> {
            for (int step = 0; step <= RADIUS; step++) {
                int currentStep = step;
                Bukkit.getScheduler().runTaskLater(Main.getInstance(), () -> {
                    createExplosionEffect(casterLocation, currentStep);
                    if (currentStep == RADIUS) {
                        damageEntitiesInRadius(caster, casterLocation, RADIUS);
                    }
                }, step * 5L); // Increment animation delay
            }
        });

        // Set cooldown
        cooldowns.put(playerId, currentTime);

        // Notify cooldown expiration
        Bukkit.getScheduler().runTaskLaterAsynchronously(Main.getInstance(), () -> {
            if (caster.isOnline()) {
                caster.sendMessage(ChatColor.YELLOW + "Solar Flare is now ready to use again!");
            }
        }, COOLDOWN_TIME * 20L);
    }

    private void createExplosionEffect(Location center, int radius) {
        // Increased number of particles for a more explosive and dome-like effect
        for (double y = -0.5; y <= 0.5; y += 0.25) { // Añade variación en altura
            double adjustedRadius = radius - Math.abs(y);
            for (int i = 0; i < 360; i += 10) {
                double angle = Math.toRadians(i);
                double x = adjustedRadius * Math.cos(angle);
                double z = adjustedRadius * Math.sin(angle);
                Location particleLocation = center.clone().add(x, y, z);
                center.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, particleLocation, 0);
                center.getWorld().spawnParticle(Particle.REDSTONE, particleLocation, 10, new Particle.DustOptions(org.bukkit.Color.ORANGE, 2.0f));
            }
        }
    }

    private void damageEntitiesInRadius(Player caster, Location center, int radius) {
        for (Entity entity : center.getWorld().getNearbyEntities(center, radius, radius, radius)) {
            if (entity instanceof LivingEntity target && !entity.equals(caster)) {
                if (target instanceof Player targetPlayer) {
                    targetPlayer.setHealth(Math.max(targetPlayer.getHealth() - DAMAGE, 0)); // Reduce health but don't go below 0
                    targetPlayer.damage(1);
                    return;
                } else {
                    target.damage(DAMAGE, caster);
                }
                target.getWorld().playSound(target.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 1.0f);
            }
        }
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
