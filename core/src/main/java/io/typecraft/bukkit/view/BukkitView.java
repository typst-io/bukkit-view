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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;

public class BukkitView {
    public static void openView(ChestView view, Player player, Plugin plugin) {
        ViewHolder holder = new ViewHolder(plugin);
        holder.setView(view);
        Inventory inv = Bukkit.createInventory(holder, view.getRow() * 9, view.getTitle());
        holder.setInventory(inv);
        for (Map.Entry<Integer, ViewItem> pair : view.getItems().entrySet()) {
            inv.setItem(pair.getKey(), pair.getValue().getItem());
        }
        player.openInventory(inv);
    }

    public static Listener viewListener(Plugin plugin) {
        return new BukkitViewListener(plugin);
    }

    public static void register(Plugin plugin) {
        Bukkit.getPluginManager().registerEvents(viewListener(plugin), plugin);
    }

    private static class BukkitViewListener implements Listener {
        private final Plugin plugin;

        public BukkitViewListener(Plugin plugin) {
            this.plugin = plugin;
        }

        @EventHandler
        public void onClick(InventoryClickEvent e) {
            Inventory topInv = e.getView().getTopInventory();
            ViewHolder holder = topInv.getHolder() instanceof ViewHolder ? ((ViewHolder) topInv.getHolder()) : null;
            if (holder == null || !holder.getPlugin().getName().equals(plugin.getName())) {
                return;
            }
            ChestView view = holder.getView();
            Player p = (Player) e.getWhoClicked();
            ViewItem viewItem = view.getItems().get(e.getRawSlot());
            if (viewItem != null) {
                ViewAction action = ViewAction.NOTHING;
                try {
                    action = viewItem.getOnClick().apply(new ClickEvent(p, e.getClick(), e.getAction(), e.getHotbarButton()));
                } catch (Exception ex) {
                    plugin.getLogger().log(Level.WARNING, ex, () -> "Error on InventoryClick!");
                }
                if (action instanceof ViewAction.Open) {
                    ViewAction.Open open = (ViewAction.Open) action;
                    Bukkit.getScheduler().runTask(plugin, () -> openView(open.getView(), p, plugin));
                } else if (action instanceof ViewAction.Close) {
                    Bukkit.getScheduler().runTask(plugin, p::closeInventory);
                } else if (action instanceof ViewAction.OpenAsync) {
                    ViewAction.OpenAsync openAsync = ((ViewAction.OpenAsync) action);
                    Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                        try {
                            ChestView chestView = openAsync.getViewFuture().get(30, TimeUnit.SECONDS);
                            Bukkit.getScheduler().runTask(plugin, () -> openView(chestView, p, plugin));
                        } catch (InterruptedException | ExecutionException | TimeoutException ex) {
                            plugin.getLogger().log(Level.WARNING, ex, () -> "Error while waiting to get a chest view.");
                        }
                    });
                }
            }
            e.setCancelled(true);
        }

        @EventHandler
        public void onDrag(InventoryDragEvent e) {
            Inventory topInv = e.getView().getTopInventory();
            ViewHolder holder = topInv.getHolder() instanceof ViewHolder ? ((ViewHolder) topInv.getHolder()) : null;
            if (holder == null || !holder.getPlugin().getName().equals(plugin.getName())) {
                return;
            }
            ChestView view = holder.getView();
            if (e.getRawSlots().stream()
                    .anyMatch(a -> view.getItems().get(a) != null)) {
                e.setCancelled(true);
            }
        }

        @EventHandler
        public void onClose(InventoryCloseEvent e) {
            Inventory topInv = e.getView().getTopInventory();
            ViewHolder holder = topInv.getHolder() instanceof ViewHolder ? ((ViewHolder) topInv.getHolder()) : null;
            if (holder == null || !holder.getPlugin().getName().equals(plugin.getName())) {
                return;
            }
            ChestView view = holder.getView();
            Player p = (Player) e.getPlayer();
            if (view.getCloseEvent() != null) {
                ViewAction action = ViewAction.NOTHING;
                try {
                    action = view.getCloseEvent().apply(new CloseEvent(p, e.getView(), e.getInventory()));
                } catch (Exception ex) {
                    plugin.getLogger().log(Level.WARNING, ex, () -> "Error on InventoryClose!");
                }
                if (action instanceof ViewAction.Cancel) {
                    Bukkit.getScheduler().runTask(plugin, () -> openView(view, p, plugin));
                } else if (action instanceof ViewAction.Open) {
                    ViewAction.Open open = (ViewAction.Open) action;
                    Bukkit.getScheduler().runTask(plugin, () -> openView(open.getView(), p, plugin));
                }
            }
        }
    }
}
