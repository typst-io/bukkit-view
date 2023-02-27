package io.typecraft.bukkit.view;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

public class ViewHolder implements InventoryHolder {
    private final Plugin plugin;
    private ChestView view;
    private Inventory inventory;

    public ViewHolder(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    @Nullable
    public ChestView getView() {
        return view;
    }

    public void setView(ChestView view) {
        this.view = view;
    }

    public Plugin getPlugin() {
        return plugin;
    }
}
