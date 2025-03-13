package com.holiday.simpleplanets.task;

import com.holiday.simpleplanets.Main;
import com.holiday.simpleplanets.abstracts.AbstractAbilities;
import com.holiday.simpleplanets.enums.AbilitiesSlot;
import com.holiday.simpleplanets.interfaces.Abilities;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.Set;

public class HotBarTask extends BukkitRunnable {

    @Override
    public void run() {

        for (Player player : Bukkit.getOnlinePlayers()) {
            String element = Main.getInstance().getPlayerDataManager().getElement(player.getUniqueId());

            if (element.isEmpty()) continue;
            Set<Abilities> a = new HashSet<>();
            for (Abilities abilities : Main.getInstance().getAbilities()) {
                if (abilities instanceof AbstractAbilities abstractAbilities) {
                    if (abstractAbilities.getElement().equalsIgnoreCase(element)) {
                        a.add(abilities);
                    }
                }
            }

            AbstractAbilities primary = null;
            AbstractAbilities secondary = null;
            AbstractAbilities third = null;
            for (Abilities abilities : a) {
                if (abilities instanceof AbstractAbilities abstractAbilities) {
                    if (abstractAbilities.getSlot() == AbilitiesSlot.PRIMARY) {
                        primary = abstractAbilities;
                    } else if (abstractAbilities.getSlot() == AbilitiesSlot.SECONDARY) {
                        secondary = abstractAbilities;
                    } else if (abstractAbilities.getSlot() == AbilitiesSlot.THIRD) {
                        third = abstractAbilities;
                    }
                }
            }

            if (primary == null) continue;
            if (secondary == null) continue;
            if (third == null) continue;

            if (element.equalsIgnoreCase("Earth")) {
                element = ChatColor.GREEN + element;
            } else if (element.equalsIgnoreCase("Venus")) {
                element = ChatColor.GOLD + element;
            } else if (element.equalsIgnoreCase("Mars")) {
                element = ChatColor.GOLD + element;
            } else if (element.equalsIgnoreCase("Sun")) {
                element = ChatColor.YELLOW + element;
            }

            String primaryCooldown = ((Abilities) primary).getCooldown(player) == 0 ? "§aREADY" : "§c" + ((Abilities) primary).getCooldown(player);
            String secondCooldown = ((Abilities) secondary).getCooldown(player) == 0 ? "§aREADY" : "§c" + ((Abilities) secondary).getCooldown(player);
            String thirdCooldown = ((Abilities) third).getCooldown(player) == 0 ? "§aREADY" : "§c" + ((Abilities) third).getCooldown(player);

            TextComponent textComponent = new TextComponent(element.toUpperCase() + " §f§l| §f" + primaryCooldown + " §f§l| §f" + secondCooldown + " §f§l|  §F" + thirdCooldown);
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, textComponent);
        }
    }
}
