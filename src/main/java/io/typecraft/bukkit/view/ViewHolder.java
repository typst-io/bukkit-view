package io.typecraft.bukkit.view;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public class ViewHolder implements InventoryHolder {
    private View view;
    private Inventory inventory;

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    public View getView() {
        return view;
    }

    public void setView(View view) {
        this.view = view;
    }
}
