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
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class Sunbeam extends AbstractAbilities implements Abilities {

    private final HashMap<UUID, Long> cooldowns = new HashMap<>();
    private static final int COOLDOWN_TIME = 30; // Cooldown in seconds
    private static final double DAMAGE = 8.0; // Damage dealt (7 hearts)
    private static final int RADIUS = 15; // Effect radius (15 blocks)

    public Sunbeam(Main main) {
        super("Sun Beam", "Sun", main, AbilitiesSlot.SECONDARY);
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
                caster.sendMessage(ChatColor.RED + "Sun Beam is on cooldown for " + (timeLeft / 1000) + " seconds.");
                return;
            }
        }

        // Execute the ability
        caster.sendMessage(ChatColor.GOLD + "You call down a beam of intense sunlight!");
        Location casterLocation = caster.getLocation();

        // Play sound
        caster.getWorld().playSound(casterLocation, Sound.ENTITY_BLAZE_SHOOT, 1.5f, 1.0f);

        // Select a random enemy within radius
        Entity randomEnemy = getRandomEnemy(caster, RADIUS);

        if (randomEnemy == null) {
            caster.sendMessage(ChatColor.RED + "No enemies found in range!");
            return;
        }

        // Create the beam of sunlight
        Location targetLocation = randomEnemy.getLocation();
        createSunlightBeam(casterLocation, targetLocation);

        // Apply damage and blindness
        if (randomEnemy instanceof LivingEntity target) {
            // If the target is a player, reduce health manually instead of using damage()
            if (target instanceof Player targetPlayer) {
                targetPlayer.damage(1);
                targetPlayer.setHealth(Math.max(targetPlayer.getHealth() - DAMAGE, 0)); // Reduce health but don't go below 0
            } else {
                // For mobs, use damage()
                target.damage(DAMAGE, caster);
            }

            // Apply blindness effect
            target.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 100, 1)); // Blindness for 5 seconds
            target.getWorld().playSound(target.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 1.0f);
        }

        // Set cooldown
        cooldowns.put(playerId, currentTime);

        // Notify cooldown expiration
        Bukkit.getScheduler().runTaskLaterAsynchronously(Main.getInstance(), () -> {
            if (caster.isOnline()) {
                caster.sendMessage(ChatColor.YELLOW + "Sun Beam is now ready to use again!");
            }
        }, COOLDOWN_TIME * 20L);
    }

    private Entity getRandomEnemy(Player caster, int radius) {
        List<Entity> nearbyEntities = (List<Entity>) caster.getWorld().getNearbyEntities(caster.getLocation(), radius, radius, radius);
        Random random = new Random();

        // Filter out non-living entities and the caster itself
        nearbyEntities.removeIf(entity -> !(entity instanceof LivingEntity) || entity.equals(caster));

        if (nearbyEntities.isEmpty()) {
            return null; // No enemies found
        }

        // Return a random enemy
        return nearbyEntities.get(random.nextInt(nearbyEntities.size()));
    }

    private void createSunlightBeam(Location start, Location target) {
        // Create a line of particles from start to target
        double distance = start.distance(target);
        Vector direction = target.toVector().subtract(start.toVector()).normalize();

        // Use Particle.REDSTONE for a red laser effect
        for (double i = 0; i <= distance; i += 0.3) { // Adjust particle density
            Location particleLocation = start.clone().add(direction.multiply(i));

            // Creating a wider beam using larger particle spread
            start.getWorld().spawnParticle(Particle.REDSTONE, particleLocation, 1, 0, 0, 0, 0, new Particle.DustOptions(org.bukkit.Color.RED, 2.0f)); // Adjusted size and color
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
