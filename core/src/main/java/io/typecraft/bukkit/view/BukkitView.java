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
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BukkitView {
    /**
     * Open the given view as a new inventory.
     * This will cause InventoryCloseEvent and InventoryOpenEvent.
     */
    public static void openView(ChestView view, Player player, Plugin plugin) {
        ViewHolder holder = new ViewHolder(plugin);
        holder.setView(view);
        Inventory inv = Bukkit.createInventory(holder, view.getRow() * 9, view.getTitle());
        holder.setInventory(inv);
        OpenEvent event = new OpenEvent(view, player);
        updateInventory(view.getContents(), inv, event);
        player.openInventory(inv);
    }

    /**
     * Update the inventory contents to the given contents of view.
     * This won't cause InventoryCloseEvent and InventoryOpenEvent.
     *
     * @return false if the title and size of inventory player seeing is different from the given view; true if success.
     */
    public static boolean updateView(ChestView view, Player player) {
        Inventory topInv = player.getOpenInventory().getTopInventory();
        InventoryHolder holder = topInv.getHolder();
        String title = player.getOpenInventory().getTitle();
        int size = topInv.getSize();
        if (holder instanceof ViewHolder && title.equals(view.getTitle()) && size == (view.getRow() * 9)) {
            ViewHolder viewHolder = (ViewHolder) holder;
            viewHolder.setView(view);
            // TODO: Is this naming `OpenEvent` correct even for update?
            updateInventory(view.getContents(), topInv, new OpenEvent(view, player));
            return true;
        }
        return false;
    }

    private static Optional<ViewHolder> getOpenedViewHolder(UUID playerId) {
        Player p = Bukkit.getPlayer(playerId);
        InventoryHolder holder = p != null ? p.getOpenInventory().getTopInventory().getHolder() : null;
        return holder instanceof ViewHolder
                ? Optional.of((ViewHolder) holder)
                : Optional.empty();
    }

    /**
     * Get an opened view from the given player id.
     */
    public static Optional<ChestView> getOpenedView(UUID playerId) {
        return getOpenedViewHolder(playerId)
                .flatMap(holder -> Optional.ofNullable(holder.getView()));
    }

    private static void updateInventory(ViewContents contents, Inventory inv, OpenEvent event) {
        inv.clear();
        for (Map.Entry<Integer, ViewControl> pair : contents.getControls().entrySet()) {
            inv.setItem(pair.getKey(), pair.getValue().getItem().apply(event));
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
            if (view == null) {
                e.setCancelled(true);
                return;
            }
            // update user input items
            Player p = (Player) e.getWhoClicked();
            ViewControl viewControl = view.getContents().getControls().get(e.getRawSlot());
            // Cancel if tried to move the control items
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
            // Notify control onClick
            if (viewControl != null) {
                ViewAction action = ViewAction.NOTHING;
                try {
                    action = viewControl.getOnClick().apply(new ClickEvent(view, p, e.getClick(), e.getAction(), e.getHotbarButton()));
                } catch (Exception ex) {
                    plugin.getLogger().log(Level.WARNING, ex, () -> "Error on inventory click!");
                }
                handleAction(p, holder, action);
            }
            // update user input items
            runSync(holder::updateViewContents);
        }

        @EventHandler
        public void onDrag(InventoryDragEvent e) {
            Inventory topInv = e.getView().getTopInventory();
            ViewHolder holder = topInv.getHolder() instanceof ViewHolder ? ((ViewHolder) topInv.getHolder()) : null;
            if (holder == null || !holder.getPlugin().getName().equals(plugin.getName())) {
                return;
            }
            ChestView view = holder.getView();
            if (view == null) {
                e.setCancelled(true);
                return;
            }
            // update user input items
            ChestView newView = view.withContents(view.getContents().updated(topInv));
            holder.setView(newView);
            if (
                    e.getRawSlots().stream()
                            .anyMatch(a -> newView.getContents().getControls().get(a) != null)
            ) {
                e.setCancelled(true);
            }
            runSync(holder::updateViewContents);
        }

        @EventHandler
        public void onClose(InventoryCloseEvent e) {
            Inventory topInv = e.getView().getTopInventory();
            ViewHolder holder = topInv.getHolder() instanceof ViewHolder ? ((ViewHolder) topInv.getHolder()) : null;
            if (holder == null || !holder.getPlugin().getName().equals(plugin.getName())) {
                return;
            }
            ChestView view = holder.getView();
            if (view == null) {
                return;
            }
            Player p = (Player) e.getPlayer();
            ViewAction action = ViewAction.NOTHING;
            try {
                action = view.getOnClose().apply(new CloseEvent(view, p));
            } catch (Exception ex) {
                plugin.getLogger().log(Level.WARNING, ex, () -> "Error on inventory close!");
            }
            boolean giveBackInputItems = true;
            if (action instanceof ViewAction.Close) {
                ViewAction.Close close = (ViewAction.Close) action;
                giveBackInputItems = close.isGiveBackItems();
            } else {
                handleAction(p, holder, action);
            }
            // give back the items
            if (giveBackInputItems) {
                runSync(() -> giveBackContents(view, p));
            }
        }

        private void handleAction(Player p, ViewHolder holder, ViewAction action) {
            ChestView currentView = holder.getView();
            if (currentView == null) {
                return;
            }
            if (action instanceof ViewAction.Open) {
                ViewAction.Open open = (ViewAction.Open) action;
                holder.setView(null);
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
                        handleException(plugin.getLogger(), ex);
                    }
                });
            } else if (action instanceof ViewAction.Update) {
                ViewAction.Update update = (ViewAction.Update) action;
                ChestView newView = currentView.withContents(update.getContents());
                updateInventory(update.getContents(), holder.getInventory(), new OpenEvent(newView, p));
                holder.setView(newView);
            } else if (action instanceof ViewAction.UpdateAsync) {
                ViewAction.UpdateAsync updateAsync = (ViewAction.UpdateAsync) action;
                runAsync(() -> {
                    try {
                        ViewContents contents = updateAsync.getContentsFuture().get(30, TimeUnit.SECONDS);
                        runSync(() -> {
                            ChestView newView = currentView.withContents(contents);
                            updateInventory(contents, holder.getInventory(), new OpenEvent(newView, p));
                            holder.setView(newView);
                        });
                    } catch (Exception ex) {
                        handleException(plugin.getLogger(), ex);
                    }
                });
            }
        }

        private static void handleException(Logger logger, Throwable throwable) {
            if (throwable instanceof ExecutionException) {
                handleException(logger, throwable.getCause());
            } else if (!(throwable instanceof CancellationException) && !(throwable instanceof TimeoutException)) {
                logger.log(Level.WARNING, throwable, () -> "Error while getting a view to open.");
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
