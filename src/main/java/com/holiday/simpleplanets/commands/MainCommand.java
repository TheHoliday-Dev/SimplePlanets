package com.holiday.simpleplanets.commands;

import com.holiday.simpleplanets.Main;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class MainCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {

        if (commandSender instanceof Player player) {
            if (player.isOp()) {
                startRouletteTitle(player);
            }
        }

        return false;
    }

    public void startRouletteTitle(Player player) {
        final List<String> rouletteOptions = Arrays.asList("Earth", "Mars", "Mercury", "Venus");
        final Random random = new Random();

        BukkitTask task = new BukkitRunnable() {
            int tick = 0;

            @Override
            public void run() {
                if (tick < 10) {  // Display for 10 seconds
                    String randomElement = rouletteOptions.get(random.nextInt(rouletteOptions.size()));
                    player.playSound(player.getLocation(), Sound.BLOCK_SWEET_BERRY_BUSH_PICK_BERRIES, 1.5f, 1.5f);
                    player.sendTitle("§7Rolling...", "§f" + randomElement, 0, 20, 0);  // Display spinning title
                    tick++;
                } else {
                    String selectedElement = rouletteOptions.get(random.nextInt(rouletteOptions.size()));
                    Main.getInstance().getPlayerDataManager().setElement(player.getUniqueId(), selectedElement);
                    player.sendTitle("§aElement Selected!", "§f" + selectedElement, 10, 20, 10);  // Final result title;
                    cancel();
                }
            }
        }.runTaskTimer(Main.getInstance(), 0L, 20L);  // Update every 1 second (20 ticks)

    }
}
