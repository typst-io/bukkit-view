package io.typst.bukkit.view;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

public class ViewHolder implements InventoryHolder {
    private final Plugin plugin;
    private ChestView view;
    private Inventory inventory = null;
    private boolean giveBackItems = true;

    public ViewHolder(Plugin plugin) {
        this.plugin = plugin;
    }

    /*
    user input contents 업데이트 시점:
    - onClick
    - onDrag
     */
    void updateViewContents() {
        ChestView view = getView();
        Inventory inv = getInventory();
        if (view == null) {
            return;
        }
        setView(view.withContents(view.getContents().updated(inv)));
    }

    void updateViewContentsWithPlayer(Player player) {
        updateViewContents();
        ChestView view = getView();
        if (view != null) {
            view.getOnContentsUpdate().accept(new UpdateEvent(player, view.getContents().getItems()));
        }
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

    public boolean isGiveBackItems() {
        return giveBackItems;
    }

    public void setGiveBackItems(boolean giveBackItems) {
        this.giveBackItems = giveBackItems;
    }
}
