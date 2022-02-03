package io.typecraft.bukkit.view.plugin;

import io.typecraft.bukkit.view.BukkitView;
import io.typecraft.bukkit.view.ViewItem;
import io.typecraft.bukkit.view.page.PageViewLayout;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
        return PageViewLayout.ofDefault(
                title,
                6,
                Material.STONE_BUTTON,
                Arrays.stream(Material.values())
                        .flatMap(mat -> !mat.name().startsWith("LEGACY_") && (mat.isSolid() || mat.isItem()) ? Stream.of(new ItemStack(mat)) : Stream.empty())
                        .map(item -> (Supplier<ViewItem>) () -> ViewItem.consumer(item, e -> {
                            e.getPlayer().sendMessage(item.getType().name());
                        }))
                        .collect(Collectors.toList())
        );
    }
}
