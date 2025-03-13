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
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.UUID;

public class TectonicShockwave extends AbstractAbilities implements Abilities {

    private final HashMap<UUID, Long> cooldowns = new HashMap<>();
    private static final int COOLDOWN_TIME = 25; // Cooldown in seconds
    private static final int RADIUS = 5; // Shockwave radius
    private static final int DAMAGE = 4; // Damage in health points (4.5 hearts)
    private static final double KNOCKBACK_STRENGTH = 1.5; // Knockback multiplier

    public TectonicShockwave(Main main) {
        super("Tectonic Shockwave", "Earth", main, AbilitiesSlot.SECONDARY);
    }

    @Override
    public void startAbilities(PlayerInteractEvent event, Player caster) {
        if (!isElement(caster)) {
            return;
        }

        if (caster.isSneaking()) {
            return;
        }

        if (!isRight(event.getAction())) {
            return;
        }

        UUID playerId = caster.getUniqueId();
        long currentTime = System.currentTimeMillis();

        if (cooldowns.containsKey(playerId)) {
            long lastUsed = cooldowns.get(playerId);
            long timeLeft = (lastUsed + COOLDOWN_TIME * 1000) - currentTime;

            if (timeLeft > 0) {
                caster.sendMessage(ChatColor.RED + "Tectonic Shockwave is on cooldown for " + (timeLeft / 1000) + " seconds.");
                return;
            }
        }

        // Activate ability
        caster.sendMessage(ChatColor.DARK_GREEN + "You unleash a Tectonic Shockwave!");
        Location center = caster.getLocation();

        // Play sound effect
        caster.getWorld().playSound(center, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 2.0f, 0.8f);

        // Particle effect: Blue redstone forming a dome
        for (int i = 0; i < 5; i++) {  // Loop 5 times to increase the density
            for (int angleY = 0; angleY <= 180; angleY += 15) {
                double radius = RADIUS * Math.sin(Math.toRadians(angleY));
                double y = RADIUS * Math.cos(Math.toRadians(angleY));
                for (int angleXZ = 0; angleXZ < 360; angleXZ += 30) {
                    double x = radius * Math.cos(Math.toRadians(angleXZ));
                    double z = radius * Math.sin(Math.toRadians(angleXZ));
                    Location particleLocation = center.clone().add(x, y, z);
                    caster.getWorld().spawnParticle(Particle.REDSTONE, particleLocation, 10, new Particle.DustOptions(org.bukkit.Color.BLUE, 1.0f));  // Increase to 100 particles per location
                }
            }
        }
        // Apply damage and knockback to nearby entities
        for (Entity entity : center.getWorld().getNearbyEntities(center, RADIUS, RADIUS, RADIUS)) {
            if (entity instanceof LivingEntity target && !entity.equals(caster)) {

                if (target instanceof Player targetPlayer) {
                    targetPlayer.setHealth(Math.max(targetPlayer.getHealth() - DAMAGE, 0)); // Reduce health but don't go below 0
                    targetPlayer.damage(1);
                    // Deal damage
                } else {
                    target.damage(DAMAGE, caster);
                }
                // Calculate knockback
                Vector knockback = target.getLocation().toVector().subtract(center.toVector()).normalize().multiply(KNOCKBACK_STRENGTH);
                knockback.setY(0.5); // Add a vertical knockback for a more dramatic effect
                target.setVelocity(knockback);
            }
        }

        // Set cooldown
        cooldowns.put(playerId, currentTime);

        // Notify cooldown expiration
        Bukkit.getScheduler().runTaskLaterAsynchronously(Main.getInstance(), () -> {
            if (caster.isOnline()) {
                caster.sendMessage(ChatColor.YELLOW + "Tectonic Shockwave is now ready to use again!");
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
