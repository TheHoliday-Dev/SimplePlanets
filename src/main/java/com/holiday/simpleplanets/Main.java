package com.holiday.simpleplanets;

import com.holiday.simpleplanets.abilities.*;
import com.holiday.simpleplanets.commands.MainCommand;
import com.holiday.simpleplanets.data.PlayerDataManager;
import com.holiday.simpleplanets.interfaces.Abilities;
import com.holiday.simpleplanets.listeners.PlayerListeners;
import com.holiday.simpleplanets.task.EffectTask;
import com.holiday.simpleplanets.task.EnderEggTask;
import com.holiday.simpleplanets.task.HotBarTask;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.Set;

public final class Main extends JavaPlugin {

    PlayerDataManager playerDataManager;
    static Main instance;

    Set<Abilities> abilities = new HashSet<>();

    @Override
    public void onEnable() {
        saveDefaultConfig();

        instance = this;
        playerDataManager = new PlayerDataManager();

        getServer().getPluginManager().registerEvents(new PlayerListeners(this), this);

        loadAbilities();

        new HotBarTask().runTaskTimer(this, 0, 20L);
        new EnderEggTask().runTaskTimer(this, 0, 20L);
        new EffectTask().runTaskTimer(this, 0, 10L);

        getCommand("reroll").setExecutor(new MainCommand());

        registerCustomRecipe(this);
    }

    @Override
    public void onDisable() {
    }

    public void registerCustomRecipe(JavaPlugin plugin) {
        // Create the custom item (replace with your custom item if necessary)
        ItemStack customItem = new ItemStack(Material.BOOK);  // Change this to the desired output item
        ItemMeta customItemMeta = customItem.getItemMeta();
        customItemMeta.setDisplayName("§aReroll Book");
        customItemMeta.getPersistentDataContainer().set(new NamespacedKey(this, "reroll"), PersistentDataType.BOOLEAN, true);
        customItem.setItemMeta(customItemMeta);

        // Create the recipe for a 3x3 crafting grid
        ShapedRecipe recipe = new ShapedRecipe(customItem);

        // Define the shape of the recipe grid (3x3)
        recipe.shape(
                "NDN",
                "DTD",
                "NDN"
        );

        // Define the ingredients (corresponding to the positions in the grid)
        recipe.setIngredient('N', Material.NETHERITE_INGOT);  // 'N' is Netherite Ingot
        recipe.setIngredient('D', Material.DIAMOND_BLOCK);  // 'D' is Diamond Block
        recipe.setIngredient('T', Material.TOTEM_OF_UNDYING);  // 'T' is Totem of Undying

        // Register the custom recipe
        Bukkit.addRecipe(recipe);
    }

    void loadAbilities() {
        HeatResistance heatResistance = new HeatResistance(this);
        abilities.add(heatResistance);

        SpeedSurge speedSurge = new SpeedSurge(this);
        abilities.add(speedSurge);

        SolarFlare solarFlare = new SolarFlare(this);
        abilities.add(solarFlare);

        ToxicCloud toxicCloud = new ToxicCloud(this);
        abilities.add(toxicCloud);

        ExtremeHeat extremeHeat = new ExtremeHeat(this);
        abilities.add(extremeHeat);

        MagneticPull magneticPull = new MagneticPull(this);
        abilities.add(magneticPull);

        SelfHeal selfHeal = new SelfHeal(this);
        abilities.add(selfHeal);

        TectonicShockwave tectonicShockwave = new TectonicShockwave(this);
        abilities.add(tectonicShockwave);

        Rockfall rockfall = new Rockfall(this);
        abilities.add(rockfall);

        DustStorm dustStorm = new DustStorm(this);
        abilities.add(dustStorm);

        LowGravityLeap lowGravityLeap = new LowGravityLeap(this);
        abilities.add(lowGravityLeap);

        IronStrength ironStrength = new IronStrength(this);
        abilities.add(ironStrength);

        SolarFlareUp solarFlareUp = new SolarFlareUp(this);
        abilities.add(solarFlareUp);

        Sunbeam sunbeam = new Sunbeam(this);
        abilities.add(sunbeam);

        Supernova supernova = new Supernova(this);
        abilities.add(supernova);
    }

    public static Main getInstance() {
        return instance;
    }

    public Set<Abilities> getAbilities() {
        return abilities;
    }

    public PlayerDataManager getPlayerDataManager() {
        return playerDataManager;
    }
}
