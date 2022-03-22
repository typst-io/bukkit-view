package io.typecraft.bukkit.view.plugin;

import io.typecraft.bukkit.view.*;
import io.typecraft.bukkit.view.page.PageContext;
import io.typecraft.bukkit.view.page.PageViewLayout;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
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
        Map<Integer, ViewControl> controls = new HashMap<>();
        ViewControl wall = ViewControl.just(new ItemStack(Material.BLACK_STAINED_GLASS_PANE));
        for (int i = 0; i < 9; i++) {
            controls.put(i, wall);
        }

        ItemStack barrierItem = new ItemStack(Material.BARRIER);
        ItemMeta barrierItemMeta = barrierItem.getItemMeta();
        assert barrierItemMeta != null;
        barrierItemMeta.setDisplayName("§cEXIT");
        barrierItem.setItemMeta(barrierItemMeta);

        controls.put(8, ViewControl.of(barrierItem, e -> ViewAction.CLOSE));
        return ChestView.just("Chest", 6, ViewContents.ofControls(controls));
    }

    private static PageViewLayout createMyViewLayout(String title) {
        List<Function<PageContext, ViewControl>> pagingContents = Arrays.stream(Material.values())
                .filter(mat -> mat.isItem() && !mat.isAir())
                .map(material -> (Function<PageContext, ViewControl>) ctx -> {
                    ItemStack item = new ItemStack(material);
                    return ViewControl.of(item, e -> {
                        Player clicker = e.getClicker();
                        if (clicker.isOp()) {
                            clicker.getInventory().addItem(new ItemStack(material));
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
