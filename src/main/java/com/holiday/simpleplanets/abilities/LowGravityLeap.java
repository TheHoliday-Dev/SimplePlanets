package com.holiday.simpleplanets.abilities;

import com.holiday.simpleplanets.Main;
import com.holiday.simpleplanets.abstracts.AbstractAbilities;
import com.holiday.simpleplanets.enums.AbilitiesSlot;
import com.holiday.simpleplanets.interfaces.Abilities;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.UUID;

public class LowGravityLeap extends AbstractAbilities implements Abilities {

    private final HashMap<UUID, Long> cooldowns = new HashMap<>();
    private final HashMap<UUID, Integer> remainingJumps = new HashMap<>();
    private final HashMap<UUID, Long> jumpCooldowns = new HashMap<>(); // Nuevo mapa para el cooldown de cada salto
    private static final int COOLDOWN_TIME = 60; // Cooldown en segundos
    private static final double JUMP_BOOST = 1.5; // Aumenta la altura del salto
    private static final int MAX_JUMPS = 3; // Número de saltos permitidos
    private static final int JUMP_COOLDOWN_TIME = 60; // Cooldown interno entre saltos (en ticks, 1 segundo)
    private static final long MAX_DURATION = 50000; // 50 segundos en milisegundos

    public LowGravityLeap(Main main) {
        super("Low Gravity Leap", "Mars", main, AbilitiesSlot.SECONDARY);
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

        // Verificar cooldown general
        if (cooldowns.containsKey(playerId)) {
            long lastUsed = cooldowns.get(playerId);
            long timeLeft = (lastUsed + COOLDOWN_TIME * 1000) - currentTime;

            if (timeLeft > 0) {
                caster.sendMessage(ChatColor.RED + "Low Gravity Leap is on cooldown for " + (timeLeft / 1000) + " seconds.");
                return;
            }
        }

        // Establecer el número de saltos restantes
        remainingJumps.put(playerId, MAX_JUMPS);

        // Activar la habilidad
        caster.sendMessage(ChatColor.AQUA + "You are now able to leap higher with low gravity!");
        // Sonido de efecto
        caster.getWorld().playSound(caster.getLocation(), Sound.ENTITY_PLAYER_SPLASH, 1.0f, 1.0f);

        // Establecer cooldown general
        cooldowns.put(playerId, currentTime);

        // Crear un runnable que verifica el estado del jugador
        new BukkitRunnable() {
            @Override
            public void run() {
                // Verifica si el jugador tiene saltos restantes
                if (remainingJumps.containsKey(playerId) && remainingJumps.get(playerId) > 0) {
                    Player player = Bukkit.getPlayer(playerId);
                    if (player == null || !player.isOnline()) {
                        cancel();
                        return;
                    }

                    // Si el jugador está en el aire (es decir, no está tocando el suelo)
                    if (!player.isOnGround()) {
                        // Si no está tocando el suelo, se puede usar un salto
                        if (player.getVelocity().getY() <= 0) {
                            return; // Si no hay impulso hacia arriba, no hacer nada
                        }

                        // Verificar cooldown entre saltos
                        if (jumpCooldowns.containsKey(playerId)) {
                            long lastJumpTime = jumpCooldowns.get(playerId);
                            long timeLeft = (lastJumpTime + JUMP_COOLDOWN_TIME * 50) - System.currentTimeMillis(); // Convertir a milisegundos

                            if (timeLeft > 0) {
                                return; // Si el cooldown interno no ha pasado, no hacer nada
                            }
                        }

                        // Impulso adicional al salto
                        Vector velocity = player.getVelocity();
                        velocity.setY(velocity.getY() + JUMP_BOOST); // Aumenta el impulso en Y
                        player.setVelocity(velocity);

                        // Desactiva el daño por caída mientras esté saltando
                        player.setFallDistance(0); // Resetear la distancia de caída

                        // Reducir los saltos restantes
                        int jumpsLeft = remainingJumps.get(playerId) - 1;
                        remainingJumps.put(playerId, jumpsLeft);

                        // Actualizar el cooldown interno del salto
                        jumpCooldowns.put(playerId, System.currentTimeMillis());

                        // Si ya no quedan saltos, quitar el efecto
                        if (jumpsLeft == 0) {
                            remainingJumps.remove(playerId);
                            player.sendMessage(ChatColor.RED + "You have no more jumps left.");
                            cancel(); // Detener el runnable
                        }
                    }
                } else {
                    cancel(); // Detener el runnable si no quedan saltos
                }

                // Detener el runnable después de 50 segundos
                if (System.currentTimeMillis() - currentTime > MAX_DURATION) {
                    remainingJumps.remove(playerId);
                    cancel();
                }
            }
        }.runTaskTimer(Main.getInstance(), 0L, 10L); // Se ejecuta cada 10 ticks (aproximadamente cada 0.5 segundos)
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

    // Prevenir daño de caída
    @EventHandler
    public void onFallDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            if (remainingJumps.containsKey(player.getUniqueId())) {
                if (event.getCause() == EntityDamageEvent.DamageCause.FALL) {
                    event.setCancelled(true); // Cancelar el daño de caída
                }
            }
        }
    }
}
