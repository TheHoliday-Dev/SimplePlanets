package com.holiday.simpleplanets.abilities;

import com.holiday.simpleplanets.Main;
import com.holiday.simpleplanets.abstracts.AbstractAbilities;
import com.holiday.simpleplanets.enums.AbilitiesSlot;
import com.holiday.simpleplanets.interfaces.Abilities;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Random;
import java.util.UUID;

public class Rockfall extends AbstractAbilities implements Abilities {

    private final HashMap<UUID, Long> cooldowns = new HashMap<>();
    private static final int COOLDOWN_TIME = 30; // Cooldown in seconds
    private static final int RADIUS = 7; // Effect radius
    private static final int DAMAGE = 3; // Damage in health points (3 hearts)
    private static final int STUN_DURATION = 2; // Stun duration in seconds
    private static final int ROCK_COUNT = 10; // Number of falling rocks

    public Rockfall(Main main) {
        super("Rockfall", "Earth", main, AbilitiesSlot.THIRD);
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
                caster.sendMessage(ChatColor.RED + "Rockfall is on cooldown for " + (timeLeft / 1000) + " seconds.");
                return;
            }
        }

        // Activate ability
        caster.sendMessage(ChatColor.DARK_GREEN + "You summon a Rockfall!");
        Location center = caster.getLocation();

        // Play sound effect
        caster.getWorld().playSound(center, Sound.ENTITY_ZOMBIE_BREAK_WOODEN_DOOR, 2.0f, 1.0f);

        // Spawn falling rocks
        Random random = new Random();
        for (int i = 0; i < ROCK_COUNT; i++) {
            double offsetX = random.nextDouble() * RADIUS * 2 - RADIUS;
            double offsetZ = random.nextDouble() * RADIUS * 2 - RADIUS;
            Location spawnLocation = center.clone().add(offsetX, 15, offsetZ); // Spawn rocks 15 blocks above the ground
            FallingBlock fallingBlock = caster.getWorld().spawnFallingBlock(spawnLocation, Material.COBBLESTONE.createBlockData());

            // Marcar el bloque como parte de Rockfall
            fallingBlock.setMetadata("rockfall", new FixedMetadataValue(Main.getInstance(), caster.getUniqueId()));
            fallingBlock.setDropItem(false);

            // Iniciar una tarea para verificar colisiones cercanas
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (fallingBlock.isDead() || !fallingBlock.isValid()) {
                        this.cancel(); // Termina la tarea si el bloque ya no existe
                        return;
                    }

                    // Buscar entidades cercanas dentro de un rango de 1 bloque
                    for (Entity entity : fallingBlock.getNearbyEntities(1.0, 1.0, 1.0)) {
                        if (entity instanceof LivingEntity target && !(entity.equals(caster))) {
                            // Aplicar daño y aturdimiento

                            if (target instanceof Player targetPlayer) {
                                targetPlayer.setHealth(Math.max(targetPlayer.getHealth() - DAMAGE, 0)); // Reduce health but don't go below 0
                                targetPlayer.damage(1);
                            } else {
                                target.damage(DAMAGE);
                            }
                            target.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, STUN_DURATION * 20, 2));

                            // Eliminar el bloque y cancelar la tarea
                            fallingBlock.remove();
                            this.cancel();
                            break;
                        }
                    }
                }
            }.runTaskTimer(Main.getInstance(), 0L, 1L); // Ejecutar cada tick (20 veces por segundo)
        }

        // Set cooldown
        cooldowns.put(playerId, currentTime);

        // Notify cooldown expiration
        Bukkit.getScheduler().runTaskLaterAsynchronously(Main.getInstance(), () -> {
            if (caster.isOnline()) {
                caster.sendMessage(ChatColor.YELLOW + "Rockfall is now ready to use again!");
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

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof FallingBlock fallingBlock && event.getEntity() instanceof LivingEntity target) {
            Rockfall.handleFallingBlockHit(fallingBlock, target);
            event.setCancelled(true); // Prevent default damage
        }
    }

    @EventHandler
    public void onEntityChangeBlock(EntityChangeBlockEvent event) {
        if (event.getEntity() instanceof FallingBlock fallingBlock) {
            Rockfall.handleFallingBlockHit(fallingBlock, null);
            event.setCancelled(true); // Prevent block from being placed
        }
    }

    public static void handleFallingBlockHit(FallingBlock block, Entity hitEntity) {
        if (block.hasMetadata("rockfall") && hitEntity instanceof LivingEntity target) {
            // Deal damage
            target.damage(DAMAGE);

            // Apply stun effect (slowness)
            target.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, STUN_DURATION * 20, 2));

            // Remove the falling block
            block.remove();
        } else if (block.hasMetadata("rockfall")) {
            // Remove the block if it hits the ground
            block.remove();
        }
    }
}
