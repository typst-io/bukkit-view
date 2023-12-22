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

import java.util.*;
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
        OpenEvent event = new OpenEvent(player, view);
        updateInventory(view.getContents(), inv, event);
        player.openInventory(inv);
    }

    /**
     * Update the inventory to the given items of view.
     * This won't cause InventoryCloseEvent and InventoryOpenEvent.
     * If the new controls overwrite a player accessible slot, then the item will be returned back to player.
     *
     * @return false if the view can't be updated -- the title and size of inventory player seeing is different from the given view; true if success.
     */
    public static boolean updateView(ChestView newView, Player player) {
        Inventory topInv = player.getOpenInventory().getTopInventory();
        InventoryHolder holder = topInv.getHolder();
        String title = player.getOpenInventory().getTitle();
        int size = topInv.getSize();
        if (holder instanceof ViewHolder && title.equals(newView.getTitle()) && size == (newView.getRow() * 9)) {
            ViewHolder viewHolder = (ViewHolder) holder;
            // update contents
            viewHolder.setView(newView);
            updateInventory(newView.getContents(), topInv, new OpenEvent(player, newView));
            return true;
        }
        return false;
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
            Inventory bottomInv = e.getView().getBottomInventory();
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
                case SHIFT_LEFT:
                    Inventory clickedInv = e.getClickedInventory();
                    if (clickedInv == null) {
                        break;
                    }
                    if (viewControl != null) {
                        e.setCancelled(true);
                    }
                    Inventory targetInventory = e.getView().getTopInventory().equals(clickedInv)
                            ? bottomInv
                            : e.getView().getTopInventory();
                    int targetSlot = targetInventory.firstEmpty();
                    if (clickedInv.equals(bottomInv) && view.getContents().getControls().containsKey(targetSlot)) {
                        e.setCancelled(true);
                    }
                    List<Integer> overwrittenSlots = view.getOverwriteMoveToOtherInventorySlots();
                    if (clickedInv.equals(bottomInv) && !overwrittenSlots.isEmpty()) {
                        ItemStack item = e.getCurrentItem();
                        if (item == null || item.getType() == Material.AIR) {
                            return;
                        }
                        int targetEmptySlot = view.findFirstSpace(overwrittenSlots, item);
                        if (targetEmptySlot >= 0 && targetSlot != targetEmptySlot) {
                            e.setCancelled(true);
                            runSync(() -> {
                                InventoryHolder theHolder = targetInventory.getHolder();
                                ViewHolder viewHolder = theHolder instanceof ViewHolder ? ((ViewHolder) theHolder) : null;
                                if (viewHolder == null) {
                                    return;
                                }
                                ItemStack clickedItem = clickedInv.getItem(e.getSlot());
                                // check the item is equal after 1 tick
                                if (clickedItem == null || !clickedItem.equals(item)) {
                                    return;
                                }
                                ItemStack targetItem = targetInventory.getItem(targetEmptySlot);
                                if (targetItem == null || targetItem.getType() == Material.AIR) {
                                    targetInventory.setItem(targetEmptySlot, clickedItem);
                                } else if (targetItem.isSimilar(clickedItem) && targetItem.getAmount() + clickedItem.getAmount() <= targetItem.getType().getMaxStackSize()) {
                                    targetItem.setAmount(targetItem.getAmount() + clickedItem.getAmount());
                                }
                                clickedInv.setItem(e.getSlot(), null);
                                viewHolder.updateViewContentsWithPlayer(p);
                            });
                        }
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
            runSync(() -> holder.updateViewContentsWithPlayer(p));
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
            Player p = (Player) e.getWhoClicked();
            ChestView newView = view.withContents(view.getContents().updated(topInv));
            holder.setView(newView);
            if (
                    e.getRawSlots().stream()
                            .anyMatch(a -> newView.getContents().getControls().get(a) != null)
            ) {
                e.setCancelled(true);
            }
            runSync(() -> holder.updateViewContentsWithPlayer(p));
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
            // update user input items
            Player p = (Player) e.getPlayer();
            ViewAction action = ViewAction.NOTHING;
            try {
                action = view.getOnClose().apply(new CloseEvent(p, view));
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
                runSync(() -> {
                    openView(open.getView(), p, plugin);
                    holder.setView(null);
                });
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
                updateInventory(update.getContents(), holder.getInventory(), new OpenEvent(p, newView));
                holder.setView(newView);
            } else if (action instanceof ViewAction.UpdateAsync) {
                ViewAction.UpdateAsync updateAsync = (ViewAction.UpdateAsync) action;
                runAsync(() -> {
                    try {
                        ViewContents contents = updateAsync.getContentsFuture().get(30, TimeUnit.SECONDS);
                        runSync(() -> {
                            ChestView newView = currentView.withContents(contents);
                            updateInventory(contents, holder.getInventory(), new OpenEvent(p, newView));
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
