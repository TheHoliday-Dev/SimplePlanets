package com.holiday.simpleplanets.abilities;

import com.holiday.simpleplanets.Main;
import com.holiday.simpleplanets.abstracts.AbstractAbilities;
import com.holiday.simpleplanets.enums.AbilitiesSlot;
import com.holiday.simpleplanets.interfaces.Abilities;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.UUID;

public class IronStrength extends AbstractAbilities implements Abilities {

    private final HashMap<UUID, Long> cooldowns = new HashMap<>();
    private static final int COOLDOWN_TIME = 20; // Cooldown en segundos
    private static final double DAMAGE_RESISTANCE = 0.5; // 50% de resistencia al daño
    private static final int DURATION = 200; // Duración en ticks (200 ticks = 10 segundos)

    public IronStrength(Main main) {
        super("Iron Strength", "Mars", main, AbilitiesSlot.THIRD);
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
                caster.sendMessage(ChatColor.RED + "Iron Strength is on cooldown for " + (timeLeft / 1000) + " seconds.");
                return;
            }
        }

        // Activar la habilidad
        caster.sendMessage(ChatColor.GREEN + "Iron Strength activated! You will be more resistant to damage for 10 seconds.");
        // Sonido de efecto
        caster.getWorld().playSound(caster.getLocation(), Sound.ENTITY_PLAYER_HURT, 1.0f, 1.0f);

        // Establecer cooldown
        cooldowns.put(playerId, currentTime);

        // Aplicar la resistencia al daño del jugador
        applyDamageResistance(caster);

        // Eliminar el efecto después de la duración
        new BukkitRunnable() {
            @Override
            public void run() {
                removeDamageResistance(caster);
                caster.sendMessage(ChatColor.RED + "Iron Strength effect has ended.");
            }
        }.runTaskLater(Main.getInstance(), DURATION); // El efecto dura 10 segundos (200 ticks)
    }

    private void applyDamageResistance(Player player) {
        // Aumenta la resistencia al daño en un 50%
        player.getAttribute(Attribute.GENERIC_ARMOR).setBaseValue(player.getAttribute(Attribute.GENERIC_ARMOR).getBaseValue() + DAMAGE_RESISTANCE);
    }

    private void removeDamageResistance(Player player) {
        // Restaura la resistencia al daño original
        player.getAttribute(Attribute.GENERIC_ARMOR).setBaseValue(player.getAttribute(Attribute.GENERIC_ARMOR).getBaseValue() - DAMAGE_RESISTANCE);
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
