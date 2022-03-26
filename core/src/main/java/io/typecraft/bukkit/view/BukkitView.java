package io.typecraft.bukkit.view;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class BukkitView {
    public static void openView(ChestView view, Player player, Plugin plugin) {
        ViewHolder holder = new ViewHolder(plugin);
        holder.setView(view);
        Inventory inv = Bukkit.createInventory(holder, view.getRow() * 9, view.getTitle());
        holder.setInventory(inv);
        updateInventory(view.getContents(), inv);
        player.openInventory(inv);
    }

    private static void updateInventory(ViewContents contents, Inventory inv) {
        inv.clear();
        for (Map.Entry<Integer, ViewControl> pair : contents.getControls().entrySet()) {
            inv.setItem(pair.getKey(), pair.getValue().getItem());
        }
        for (Map.Entry<Integer, ItemStack> pair : contents.getItems().entrySet()) {
            inv.setItem(pair.getKey(), pair.getValue());
        }
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
            if (e.getAction() == InventoryAction.NOTHING) {
                return;
            }
            Inventory topInv = e.getView().getTopInventory();
            ViewHolder holder = topInv.getHolder() instanceof ViewHolder ? ((ViewHolder) topInv.getHolder()) : null;
            if (holder == null || !holder.getPlugin().getName().equals(plugin.getName())) {
                return;
            }
            ChestView view = holder.getView();
            Player p = (Player) e.getWhoClicked();
            ViewControl viewControl = view.getContents().getControls().get(e.getRawSlot());
            // Cancel
            switch (e.getClick()) {
                case LEFT:
                case RIGHT:
                    if (viewControl != null) {
                        e.setCancelled(true);
                    }
                    break;
                default:
                    e.setCancelled(true);
                    break;
            }
            // Notify
            if (viewControl != null) {
                ViewAction action = ViewAction.NOTHING;
                try {
                    action = viewControl.getOnClick().apply(new ClickEvent(view, p, e.getClick(), e.getAction(), e.getHotbarButton()));
                } catch (Exception ex) {
                    plugin.getLogger().log(Level.WARNING, ex, () -> "Error on inventory click!");
                }
                handleAction(p, holder, action);
            }
            // Update view
            runSync(() -> updateView(topInv, view));
        }

        @EventHandler
        public void onDrag(InventoryDragEvent e) {
            Inventory topInv = e.getView().getTopInventory();
            ViewHolder holder = topInv.getHolder() instanceof ViewHolder ? ((ViewHolder) topInv.getHolder()) : null;
            if (holder == null || !holder.getPlugin().getName().equals(plugin.getName())) {
                return;
            }
            ChestView view = holder.getView();
            if (
                e.getRawSlots().stream()
                    .anyMatch(a -> view.getContents().getControls().get(a) != null)
            ) {
                e.setCancelled(true);
            }
            // Update view
            runSync(() -> updateView(topInv, view));
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
            handleAction(p, holder, action);
            // Get back the items
            runSync(() -> giveBackContents(view, p));
        }

        private void handleAction(Player p, ViewHolder holder, ViewAction action) {
            ChestView currentView = holder.getView();
            if (action instanceof ViewAction.Open) {
                ViewAction.Open open = (ViewAction.Open) action;
                runSync(() -> openView(open.getView(), p, plugin));
            } else if (action instanceof ViewAction.Reopen) {
                runSync(() -> openView(currentView, p, plugin));
            } else if (action instanceof ViewAction.Close) {
                runSync(p::closeInventory);
            } else if (action instanceof ViewAction.OpenAsync) {
                ViewAction.OpenAsync openAsync = ((ViewAction.OpenAsync) action);
                runAsync(() -> {
                    try {
                        ChestView chestView = openAsync.getViewFuture().get(30, TimeUnit.SECONDS);
                        runSync(() -> handleAction(p, holder, new ViewAction.Open(chestView)));
                    } catch (Exception ex) {
                        plugin.getLogger().log(Level.WARNING, ex, () -> "Error while getting a view to open.");
                    }
                });
            } else if (action instanceof ViewAction.Update) {
                ViewAction.Update update = (ViewAction.Update) action;
                giveBackContents(currentView, p);
                updateInventory(update.getContents(), holder.getInventory());
                holder.setView(currentView.withContents(update.getContents()));
            } else if (action instanceof ViewAction.UpdateAsync) {
                ViewAction.UpdateAsync updateAsync = (ViewAction.UpdateAsync) action;
                giveBackContents(currentView, p);
                runAsync(() -> {
                    try {
                        ViewContents contents = updateAsync.getContentsFuture().get(30, TimeUnit.SECONDS);
                        runSync(() -> {
                            updateInventory(contents, holder.getInventory());
                            holder.setView(currentView.withContents(contents));
                        });
                    } catch (Exception ex) {
                        plugin.getLogger().log(Level.WARNING, ex, () -> "Error while getting a view to update.");
                    }
                });
            }
        }

        private static void updateView(Inventory inv, ChestView view) {
            ItemStack[] contents = inv.getContents();
            for (int i = 0; i < contents.length; i++) {
                ItemStack item = contents[i];
                if (
                    item != null && item.getType() != Material.AIR &&
                        !view.getContents().getControls().containsKey(i)
                ) {
                    view.getContents().getItems().put(i, item);
                } else {
                    view.getContents().getItems().remove(i);
                }
            }
        }

        private static void giveBackContents(ChestView view, Player p) {
            ItemStack[] items = view.getContents().getItems().values().stream()
                .filter(item -> item != null && item.getType() != Material.AIR)
                .toArray(ItemStack[]::new);
            HashMap<Integer, ItemStack> failures = p.getInventory().addItem(items);
            for (ItemStack item : failures.values()) {
                p.getWorld().dropItem(p.getEyeLocation(), item);
            }
            view.getContents().getItems().clear();
        }

        private void runSync(Runnable runnable) {
            Bukkit.getScheduler().runTask(plugin, runnable);
        }

        private void runAsync(Runnable runnable) {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, runnable);
        }
    }
}
