package com.holiday.simpleplanets.abstracts;

import com.holiday.simpleplanets.Main;
import com.holiday.simpleplanets.enums.AbilitiesSlot;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;

public abstract class AbstractAbilities implements Listener {

    String name;
    String element;
    AbilitiesSlot slot;

    public AbstractAbilities(String name, String element, Main main, AbilitiesSlot abilitiesSlot) {
        this.name = name;
        this.element = element;
        this.slot = abilitiesSlot;
        main.getServer().getPluginManager().registerEvents(this, main);
    }

    public AbilitiesSlot getSlot() {
        return slot;
    }

    public String getElement() {
        return element;
    }

    public boolean isRight(Action action) {
        return action == Action.RIGHT_CLICK_BLOCK || action == Action.RIGHT_CLICK_AIR;
    }

    public boolean isRightShift(Action action, Player player) {
        if (!player.isSneaking()) {
            return false;
        }
        return action == Action.RIGHT_CLICK_BLOCK || action == Action.RIGHT_CLICK_AIR;
    }

    public boolean isElement(Player player) {
        String elementPlayer = Main.getInstance().getPlayerDataManager().getElement(player.getUniqueId());
        return this.element.equalsIgnoreCase(elementPlayer);
    }

    public boolean isLeft(Action action) {
        return action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK;
    }

    public String getName() {
        if (element.equalsIgnoreCase("Earth")) {
            return ChatColor.GREEN + name;
        } else if (element.equalsIgnoreCase("Mars")) {
            return ChatColor.GOLD + name;
        } else if (element.equalsIgnoreCase("Sun")) {
            return ChatColor.YELLOW + name;
        } else if (element.equalsIgnoreCase("Venus")) {
            return ChatColor.BLACK + name;
        } else if (element.equalsIgnoreCase("Mercury")) {
            return ChatColor.GOLD + name;
        }
        return name;
    }
}
