package io.typecraft.bukkit.view.plugin;

import io.typecraft.bukkit.view.BukkitView;
import io.typecraft.bukkit.view.ChestView;
import io.typecraft.bukkit.view.ViewAction;
import io.typecraft.bukkit.view.ViewItem;
import io.typecraft.bukkit.view.page.PageContext;
import io.typecraft.bukkit.view.page.PageViewLayout;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ViewPlugin extends JavaPlugin {
    @Override
    public void onEnable() {
        BukkitView.register(this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        Player p = sender instanceof Player ? ((Player) sender) : null;
        String head = args.length >= 1 ? args[0] : "";
        switch (head) {
            case "page": {
                if (p != null && p.isOp()) {
                    BukkitView.openView(createMyViewLayout(p.getName()).toView(1), p, this);
                }
                break;
            }
            case "chest": {
                if (p != null && p.isOp()) {
                    BukkitView.openView(createChestView(), p, this);
                }
                break;
            }
            default: {
                sender.sendMessage(String.format("§a/%s page: §fshow a demo pagination view.", label));
                sender.sendMessage(String.format("§a/%s chest: §fshow a demo chest view.", label));
                break;
            }
        }
        return true;
    }

    private static ChestView createChestView() {
        Map<Integer, ViewItem> controls = new HashMap<>();
        ViewItem wall = ViewItem.just(new ItemStack(Material.BLACK_STAINED_GLASS_PANE));
        for (int i = 0; i < 9; i++) {
            controls.put(i, wall);
        }

        ItemStack barrierItem = new ItemStack(Material.BARRIER);
        ItemMeta barrierItemMeta = barrierItem.getItemMeta();
        assert barrierItemMeta != null;
        barrierItemMeta.setDisplayName("§c나가기");
        barrierItem.setItemMeta(barrierItemMeta);

        controls.put(8, new ViewItem(barrierItem, e -> ViewAction.CLOSE));
        return new ChestView("Chest", 6, controls);
    }

    private static PageViewLayout createMyViewLayout(String title) {
        List<Function<PageContext, ViewItem>> pagingContents = Bukkit.getOnlinePlayers().stream()
                .map(p -> (Function<PageContext, ViewItem>) ctx -> {
                    ItemStack headItem = new ItemStack(Material.PLAYER_HEAD);
                    SkullMeta meta = (SkullMeta) headItem.getItemMeta();
                    if (meta != null) {
                        meta.setOwningPlayer(p);
                        headItem.setItemMeta(meta);
                    }
                    return new ViewItem(headItem, e -> {
                        Player clicker = e.getClicker();
                        if (!p.isOnline()) {
                            clicker.sendMessage(ChatColor.RED + "Player '" + p.getName() + "' not in online!");
                        } else if (clicker.isOp()) {
                            clicker.teleport(p.getLocation());
                            return ViewAction.CLOSE;
                        } else {
                            clicker.sendMessage(ChatColor.RED + "You are not a op!");
                        }
                        return ViewAction.NOTHING;
                    });
                })
                .collect(Collectors.toList());
        return PageViewLayout.ofDefault(
                title,
                6,
                Material.STONE_BUTTON,
                pagingContents
        );
    }
}
