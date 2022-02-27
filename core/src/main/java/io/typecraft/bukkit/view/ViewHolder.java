package io.typecraft.bukkit.view;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public class ViewHolder implements InventoryHolder {
    private ChestView view;
    private Inventory inventory;

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    public ChestView getView() {
        return view;
    }

    public void setView(ChestView view) {
        this.view = view;
    }
}
