package com.holiday.simpleplanets.interfaces;

import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;

public interface Abilities {
    void startAbilities(PlayerInteractEvent event, Player caster);

    int getCooldown(Player player);
}
