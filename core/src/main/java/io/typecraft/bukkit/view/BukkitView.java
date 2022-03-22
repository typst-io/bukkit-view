package io.typecraft.bukkit.view;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
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
        for (Map.Entry<Integer, ViewItem> pair : view.getControls().entrySet()) {
            inv.setItem(pair.getKey(), pair.getValue().getItem());
        }
        for (Map.Entry<Integer, ItemStack> pair : view.getContents().entrySet()) {
            inv.setItem(pair.getKey(), pair.getValue());
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
            ViewItem viewItem = view.getControls().get(e.getRawSlot());
            // Cancel
            switch (e.getClick()) {
                case LEFT:
                case SHIFT_LEFT:
                case RIGHT:
                case SHIFT_RIGHT:
                    if (viewItem != null) {
                        e.setCancelled(true);
                    }
                    break;
                default:
                    e.setCancelled(true);
                    break;
            }
            // Notify
            if (viewItem != null) {
                ViewAction action = ViewAction.NOTHING;
                try {
                    action = viewItem.getOnClick().apply(new ClickEvent(view, p, e.getClick(), e.getAction(), e.getHotbarButton()));
                } catch (Exception ex) {
                    plugin.getLogger().log(Level.WARNING, ex, () -> "Error on inventory click!");
                }
                handleAction(p, view, action);
            }
            // Update contents
            runTask(() -> {
                ItemStack[] contents = topInv.getContents();
                for (int i = 0; i < contents.length; i++) {
                    ItemStack item = contents[i];
                    if (item != null && item.getType() != Material.AIR &&
                            !view.getControls().containsKey(i)) {
                        view.getContents().put(i, item);
                    } else {
                        view.getContents().remove(i);
                    }
                }
            });
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
                    .anyMatch(a -> view.getControls().get(a) != null)) {
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
            ViewAction action = ViewAction.NOTHING;
            try {
                action = view.getOnClose().apply(new CloseEvent(view, p));
            } catch (Exception ex) {
                plugin.getLogger().log(Level.WARNING, ex, () -> "Error on inventory close!");
            }
            handleAction(p, view, action);
            // Get back the items
            runTask(() -> {
                ItemStack[] items = view.getContents().values().stream()
                        .filter(item -> item != null && item.getType() != Material.AIR)
                        .toArray(ItemStack[]::new);
                HashMap<Integer, ItemStack> failures = p.getInventory().addItem(items);
                for (ItemStack item : failures.values()) {
                    p.getWorld().dropItem(p.getEyeLocation(), item);
                }
                view.getContents().clear();
            });
        }

        private void handleAction(Player p, ChestView currentView, ViewAction action) {
            if (action instanceof ViewAction.Open) {
                ViewAction.Open open = (ViewAction.Open) action;
                Bukkit.getScheduler().runTask(plugin, () -> openView(open.getView(), p, plugin));
            } else if (action instanceof ViewAction.Reopen) {
                Bukkit.getScheduler().runTask(plugin, () -> openView(currentView, p, plugin));
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

        private void runTask(Runnable runnable) {
            Bukkit.getScheduler().runTask(plugin, runnable);
        }
    }
}
