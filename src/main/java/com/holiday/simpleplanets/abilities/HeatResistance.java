package com.holiday.simpleplanets.abilities;

import com.holiday.simpleplanets.Main;
import com.holiday.simpleplanets.abstracts.AbstractAbilities;
import com.holiday.simpleplanets.enums.AbilitiesSlot;
import com.holiday.simpleplanets.interfaces.Abilities;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.UUID;

public class HeatResistance extends AbstractAbilities implements Abilities {

    private final HashMap<UUID, Long> activeAbilities = new HashMap<>();
    private final HashMap<UUID, Long> cooldowns = new HashMap<>();
    private static final int COOLDOWN_TIME = 60; // Cooldown in seconds
    private static final int DURATION = 5; // Immunity duration in seconds

    public HeatResistance(Main main) {
        super("Heat Resistance", "Mercury", main, AbilitiesSlot.PRIMARY);
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (activeAbilities.containsKey(player.getUniqueId())) {
                        player.setFireTicks(0);
                    }
                }
            }
        }.runTaskTimer(main, 0L, 20L);
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
                caster.sendMessage(ChatColor.RED + "Heat Resistance is on cooldown for " + (timeLeft / 1000) + " seconds.");
                return;
            }
        }

        // Activate fire immunity
        activeAbilities.put(playerId, currentTime + DURATION * 1000);
        caster.sendMessage(ChatColor.GREEN + "You are now immune to fire and lava damage!");

        // Runnable to create the particle dome
        new BukkitRunnable() {
            final int radius = 3; // Radius of the dome

            @Override
            public void run() {
                if (!activeAbilities.containsKey(playerId)) {
                    cancel();
                    return;
                }

                // Particle settings for orange color
                float red = 1.0f;
                float green = 0.5f;
                float blue = 0.0f;
                org.bukkit.Particle.DustOptions dustOptions = new org.bukkit.Particle.DustOptions(org.bukkit.Color.fromRGB((int) (red * 255), (int) (green * 255), (int) (blue * 255)), 1.0f);

                // Generate particles around the player
                Location center = caster.getLocation();
                for (int x = -radius; x <= radius; x++) {
                    for (int y = -radius; y <= radius; y++) {
                        for (int z = -radius; z <= radius; z++) {
                            if (Math.sqrt(x * x + y * y + z * z) <= radius) {
                                Location particleLocation = center.clone().add(x, y, z);
                                caster.getWorld().spawnParticle(Particle.REDSTONE, particleLocation, 1, dustOptions);
                            }
                        }
                    }
                }
            }
        }.runTaskTimer(Main.getInstance(), 0L, 10L);

        // Notify when immunity wears off
        Bukkit.getScheduler().runTaskLater(Main.getInstance(), () -> {
            if (caster.isOnline() && activeAbilities.containsKey(playerId)) {
                activeAbilities.remove(playerId);
                caster.sendMessage(ChatColor.YELLOW + "Your fire immunity has worn off!");
            }
        }, DURATION * 20L);

        // Set the cooldown
        cooldowns.put(playerId, currentTime);

        // Notify when cooldown ends
        Bukkit.getScheduler().runTaskLaterAsynchronously(Main.getInstance(), () -> {
            if (caster.isOnline()) {
                caster.sendMessage(ChatColor.YELLOW + "Heat Resistance is now ready to use again!");
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

    @EventHandler
    public void onDamageFire(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        UUID playerId = player.getUniqueId();
        if (activeAbilities.containsKey(playerId)) {
            EntityDamageEvent.DamageCause cause = event.getCause();
            if (cause == EntityDamageEvent.DamageCause.FIRE ||
                    cause == EntityDamageEvent.DamageCause.FIRE_TICK ||
                    cause == EntityDamageEvent.DamageCause.LAVA) {
                event.setCancelled(true);
            }
        }
    }

    @Override
    public String getName() {
        return super.getName();
    }
}
