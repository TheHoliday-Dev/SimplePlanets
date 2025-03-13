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

public class MagneticPull extends AbstractAbilities implements Abilities {

    private final HashMap<UUID, Long> cooldowns = new HashMap<>();
    private static final int COOLDOWN_TIME = 20; // Cooldown in seconds
    private static final int RADIUS = 7; // Effect radius

    public MagneticPull(Main main) {
        super("Magnetic Pull", "Venus", main, AbilitiesSlot.THIRD);
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
                caster.sendMessage(ChatColor.RED + "Magnetic Pull is on cooldown for " + (timeLeft / 1000) + " seconds.");
                return;
            }
        }

        // Activate ability
        caster.sendMessage(ChatColor.AQUA + "You unleash Magnetic Pull!");
        Location center = caster.getLocation();

        // Play sound effect
        caster.getWorld().playSound(center, Sound.ENTITY_ENDERMAN_TELEPORT, 1.5f, 1.0f);

        // Particle effect
        for (int i = 0; i < 360; i += 15) {
            double angle = Math.toRadians(i);
            double x = RADIUS * Math.cos(angle);
            double z = RADIUS * Math.sin(angle);
            Location particleLocation = center.clone().add(x, 1.0, z);
            caster.getWorld().spawnParticle(Particle.PORTAL, particleLocation, 5, 0, 0, 0, 0.1);
        }

        for (Entity entity : center.getWorld().getNearbyEntities(center, RADIUS, RADIUS, RADIUS)) {
            if (entity instanceof LivingEntity target && !entity.equals(caster)) {
                Location targetLocation = target.getLocation();
                double deltaX = center.getX() - targetLocation.getX();
                double deltaY = center.getY() - targetLocation.getY();
                double deltaZ = center.getZ() - targetLocation.getZ();
                double distance = Math.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ);

                if (distance > 0) { // Prevent division by zero
                    // Create a stronger velocity vector towards the caster
                    org.bukkit.util.Vector velocity = new org.bukkit.util.Vector(
                            deltaX / distance * 1.5, // Increased horizontal pull strength
                            deltaY / distance * 0.7, // Adjusted vertical pull strength
                            deltaZ / distance * 1.5  // Increased horizontal pull strength
                    );

                    // Apply the velocity to the target
                    target.setVelocity(velocity);
                }
            }
        }

        // Set cooldown
        cooldowns.put(playerId, currentTime);

        // Notify cooldown expiration
        Bukkit.getScheduler().runTaskLaterAsynchronously(Main.getInstance(), () -> {
            if (caster.isOnline()) {
                caster.sendMessage(ChatColor.YELLOW + "Magnetic Pull is now ready to use again!");
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
