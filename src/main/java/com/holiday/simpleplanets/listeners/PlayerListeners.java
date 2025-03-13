package com.holiday.simpleplanets.listeners;

import com.holiday.simpleplanets.Main;
import com.holiday.simpleplanets.interfaces.Abilities;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class PlayerListeners implements Listener {

    Main main;
    Map<Player, BukkitTask> tasks = new HashMap<>();
    ItemStack stick;

    public PlayerListeners(Main main) {
        this.main = main;
        createStick();
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        Inventory inventory = event.getPlayer().getInventory();
        if (Arrays.stream(inventory.getContents()).noneMatch(item -> item != null && item.isSimilar(stick))) {
            event.getPlayer().getInventory().addItem(stick);
        }
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        if (event.getItemDrop().getItemStack().isSimilar(stick)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();

        // Iterate through the drops and remove the "Activating Wand" if present
        event.getDrops().removeIf(itemStack -> itemStack.isSimilar(stick));

        // Optionally, you can add the item back to the player's inventory
        if (!player.getInventory().contains(stick)) {
            player.getInventory().addItem(stick);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        ItemStack clickedItem = event.getCurrentItem();

        if (clickedItem != null && clickedItem.isSimilar(stick)) {
            if (event.getAction() == InventoryAction.PICKUP_ALL) {
                event.setCancelled(true);
            }
            if (event.getAction() == InventoryAction.PLACE_ALL) {
                event.setCancelled(true);
            }

            // Check if the inventory clicked is a valid container (like the player's inventory)
            if (event.getClickedInventory().getType() != InventoryType.PLAYER) {
                event.setCancelled(true); // Prevent the item from being moved
                player.sendMessage("§cYou cannot move the Activating Wand outside of your inventory.");
            }
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Inventory inv = event.getPlayer().getInventory();
        if (Arrays.stream(inv.getContents()).noneMatch(item -> item != null && item.isSimilar(stick))) {
            event.getPlayer().getInventory().addItem(stick);
        }
        if (!this.main.getPlayerDataManager().existPlayerData(event.getPlayer().getUniqueId())) {
            startRoulette(event.getPlayer());
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getHand() == EquipmentSlot.HAND) {
            ItemStack item = event.getItem();
            if (item == null || item.getType() == Material.AIR) return;

            if (item.getType() == Material.BOOK) {
                ItemMeta itemMeta = item.getItemMeta();
                if (itemMeta != null) {
                    if (itemMeta.getPersistentDataContainer().has(new NamespacedKey(Main.getInstance(), "reroll"))) {
                        if (!tasks.containsKey(event.getPlayer())) {
                            int amount = item.getAmount();
                            if (amount < 1) {
                                event.getPlayer().getInventory().removeItem(item);
                            }
                            item.setAmount(amount - 1);
                            startRouletteTitle(event.getPlayer());
                            return;
                        }
                    }
                }
            }

            if (item.isSimilar(stick)) {
                for (Abilities abilities : main.getAbilities()) {
                    abilities.startAbilities(event, event.getPlayer());
                }
            }
        }
    }

    public void createStick() {
        ItemStack item = new ItemStack(Material.GREEN_DYE);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName("§aActivating Wand");
        itemMeta.getPersistentDataContainer().set(new NamespacedKey(Main.getInstance(), "stick"), PersistentDataType.BOOLEAN, true);
        item.setItemMeta(itemMeta);
        stick = item;
    }

    public void startRoulette(Player player) {
        final List<String> rouletteOptions = Arrays.asList("Earth", "Mars", "Mercury", "Venus");
        final Random random = new Random();

        BukkitTask task = new BukkitRunnable() {
            int tick = 0;

            @Override
            public void run() {
                if (tick < 10) {  // Display for 10 seconds
                    String randomElement = rouletteOptions.get(random.nextInt(rouletteOptions.size()));
                    player.playSound(player.getLocation(), Sound.BLOCK_SWEET_BERRY_BUSH_PICK_BERRIES, 1.5f, 1.5f);
                    player.sendTitle("§7Select...", "§f" + randomElement, 0, 20, 0);  // Display spinning title
                    tick++;
                } else {
                    // After 10 seconds, stop the roulette and display the final element
                    String selectedElement = rouletteOptions.get(random.nextInt(rouletteOptions.size()));
                    Main.getInstance().getPlayerDataManager().setElement(player.getUniqueId(), selectedElement);
                    player.sendTitle("§aElement Selected!", "§f" + selectedElement, 10, 20, 10);  // Final result title
                    tasks.remove(player);
                    cancel();  // Stop the task
                }
            }
        }.runTaskTimer(Main.getInstance(), 0L, 20L);  // Update every 1 second (20 ticks)

        tasks.put(player, task);
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
                    player.sendTitle("§aElement Selected!", "§f" + selectedElement, 10, 20, 10);  // Final result title
                    tasks.remove(player);
                    cancel();
                }
            }
        }.runTaskTimer(Main.getInstance(), 0L, 20L);  // Update every 1 second (20 ticks)

        tasks.put(player, task);
    }
}
