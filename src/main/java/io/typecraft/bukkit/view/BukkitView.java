package io.typecraft.bukkit.view;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.Plugin;

import java.util.Map;

public class BukkitView {
    public static void openView(View view, Player player) {
        ViewHolder holder = new ViewHolder();
        holder.setView(view);
        Inventory inv = Bukkit.createInventory(holder, view.getRow() * 9, view.getTitle());
        holder.setInventory(inv);
        for (Map.Entry<Integer, ViewItem> pair : view.getItems().entrySet()) {
            inv.setItem(pair.getKey(), pair.getValue().getItem());
        }
        player.openInventory(inv);
    }

    public static Listener viewListener(Plugin plugin) {
        return new Listener() {
            @EventHandler
            public void onClick(InventoryClickEvent e) {
                Inventory topInv = e.getView().getTopInventory();
                ViewHolder holder = topInv.getHolder() instanceof ViewHolder ? ((ViewHolder) topInv.getHolder()) : null;
                View view = holder != null ? holder.getView() : null;
                if (holder == null || view == null) {
                    return;
                }
                Player p = (Player) e.getWhoClicked();
                ViewItem viewItem = view.getItems().get(e.getRawSlot());
                if (viewItem != null) {
                    ViewAction action = viewItem.getOnClick().apply(new ClickEvent(p));
                    if (action instanceof ViewAction.Open) {
                        ViewAction.Open open = (ViewAction.Open) action;
                        Bukkit.getScheduler().runTask(plugin, () -> openView(open.getView(), p));
                    } else if (action instanceof ViewAction.Close) {
                        Bukkit.getScheduler().runTask(plugin, p::closeInventory);
                    }
                }
                e.setCancelled(true);
            }

            @EventHandler
            public void onDrag(InventoryDragEvent e) {
                Inventory topInv = e.getView().getTopInventory();
                ViewHolder holder = topInv.getHolder() instanceof ViewHolder ? ((ViewHolder) topInv.getHolder()) : null;
                if (holder == null) {
                    return;
                }
                View view = holder.getView();
                if (e.getRawSlots().stream()
                        .anyMatch(a -> view.getItems().get(a) != null)) {
                    e.setCancelled(true);
                }
            }

            @EventHandler
            public void onClose(InventoryCloseEvent e) {

            }
        };
    }
}
