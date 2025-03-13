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

public class ToxicCloud extends AbstractAbilities implements Abilities {

    private final HashMap<UUID, Long> cooldowns = new HashMap<>();
    private static final int COOLDOWN_TIME = 40; // Cooldown in seconds
    private static final int DURATION = 5; // Duration of the effect in seconds
    private static final int DAMAGE_PER_SECOND = 2; // Damage dealt per second
    private static final int RADIUS = 5; // Effect radius

    public ToxicCloud(Main main) {
        super("Toxic Cloud", "Venus", main, AbilitiesSlot.PRIMARY);
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
                caster.sendMessage(ChatColor.RED + "Toxic Cloud is on cooldown for " + (timeLeft / 1000) + " seconds.");
                return;
            }
        }

        // Execute the ability
        caster.sendMessage(ChatColor.DARK_GREEN + "You release a Toxic Cloud!");
        Location center = caster.getLocation();

        // Play initial sound
        caster.getWorld().playSound(center, Sound.ENTITY_WITCH_AMBIENT, 1.5f, 1.0f);

        // Create the toxic cloud and apply effects
        new BukkitRunnable() {
            int secondsPassed = 0;

            @Override
            public void run() {
                if (secondsPassed >= DURATION) {
                    cancel();
                    return;
                }

                // Generate multiple layers of the particle cloud (5 layers here)
                for (int j = 0; j < 5; j++) {  // 5 layers of particles, you can change this number
                    double heightOffset = 0.5 * j;  // Spacing between the layers (you can adjust the height offset)

                    // Create a circle at each height layer
                    for (int i = 0; i < 360; i += 10) {  // Creating a circle every 10 degrees
                        double angle = Math.toRadians(i);
                        double x = RADIUS * Math.cos(angle);
                        double z = RADIUS * Math.sin(angle);
                        Location particleLocation = center.clone().add(x, 1.0 + heightOffset, z);  // Adjust height with offset
                        caster.getWorld().spawnParticle(Particle.SLIME, particleLocation, 2, 0, 0, 0, 0.05);  // Increased particle count for density
                    }
                }

                // Apply damage and poison effect to entities
                for (Entity entity : center.getWorld().getNearbyEntities(center, RADIUS, RADIUS, RADIUS)) {
                    if (entity instanceof LivingEntity target && !entity.equals(caster)) {
                        target.addPotionEffect(new PotionEffect(PotionEffectType.POISON, DURATION * 20, 0, false, false));
                        target.damage(DAMAGE_PER_SECOND, caster);
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
                caster.sendMessage(ChatColor.YELLOW + "Toxic Cloud is now ready to use again!");
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
