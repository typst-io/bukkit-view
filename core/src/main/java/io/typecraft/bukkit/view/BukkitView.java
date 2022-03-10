package io.typecraft.bukkit.view;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
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
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;

public class BukkitView {
    private static final AtomicReference<Listener> listenerRef = new AtomicReference<>();

    public static void openView(ChestView view, Player player) {
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
        return listenerRef.updateAndGet(prevListener -> prevListener != null ? prevListener : new BukkitViewListener(plugin));
    }

    public static void register(Plugin plugin) {
        Listener listener = viewListener(plugin);
        HandlerList.unregisterAll(listener);
        Bukkit.getPluginManager().registerEvents(listener, plugin);
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
            ChestView view = holder != null ? holder.getView() : null;
            if (holder == null || view == null) {
                return;
            }
            Player p = (Player) e.getWhoClicked();
            ViewItem viewItem = view.getItems().get(e.getRawSlot());
            if (viewItem != null) {
                ViewAction action = viewItem.getOnClick().apply(new ClickEvent(p, e.getClick(), e.getAction(), e.getHotbarButton()));
                if (action instanceof ViewAction.Open) {
                    ViewAction.Open open = (ViewAction.Open) action;
                    Bukkit.getScheduler().runTask(plugin, () -> openView(open.getView(), p));
                } else if (action instanceof ViewAction.Close) {
                    Bukkit.getScheduler().runTask(plugin, p::closeInventory);
                } else if (action instanceof ViewAction.OpenAsync) {
                    ViewAction.OpenAsync openAsync = ((ViewAction.OpenAsync) action);
                    Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                        try {
                            ChestView chestView = openAsync.getViewFuture().get(30, TimeUnit.SECONDS);
                            Bukkit.getScheduler().runTask(plugin, () -> openView(chestView, p));
                        } catch (InterruptedException | ExecutionException | TimeoutException ex) {
                            Bukkit.getLogger().log(Level.WARNING, ex, () -> "[BukkitView] Error while waiting to get a chest view.");
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
            if (holder == null) {
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

        }
    }
}
