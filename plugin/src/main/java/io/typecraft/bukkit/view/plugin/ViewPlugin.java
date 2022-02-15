package io.typecraft.bukkit.view.plugin;

import io.typecraft.bukkit.view.BukkitView;
import io.typecraft.bukkit.view.ViewAction;
import io.typecraft.bukkit.view.ViewItem;
import io.typecraft.bukkit.view.page.PageViewLayout;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class ViewPlugin extends JavaPlugin {
    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(BukkitView.viewListener(this), this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        Player p = sender instanceof Player ? ((Player) sender) : null;
        if (p != null && p.isOp()) {
            BukkitView.openView(createMyViewLayout(p.getName()).toView(1), p);
        }
        return true;
    }

    public static PageViewLayout createMyViewLayout(String title) {
        List<Supplier<ViewItem>> pagingContents = Bukkit.getOnlinePlayers().stream()
                .map(p -> (Supplier<ViewItem>) () -> {
                    ItemStack headItem = new ItemStack(Material.PLAYER_HEAD);
                    SkullMeta meta = (SkullMeta) headItem.getItemMeta();
                    if (meta != null) {
                        meta.setOwningPlayer(p);
                        headItem.setItemMeta(meta);
                    }
                    return new ViewItem(headItem, e -> {
                        Player clicker = e.getPlayer();
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
