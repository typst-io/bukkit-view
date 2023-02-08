package io.typecraft.bukkit.view.plugin;

import io.typecraft.bukkit.view.BukkitView;
import io.typecraft.bukkit.view.ChestView;
import io.typecraft.bukkit.view.plugin.view.ItemListView;
import io.typecraft.bukkit.view.plugin.view.PlayerChestView;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

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
                    ChestView view = ItemListView.create(
                        Arrays.stream(Material.values())
                            .filter(mat -> mat.isItem() && !mat.isAir())
                    );
                    BukkitView.openView(view, p, this);
                }
                break;
            }
            case "chest": {
                if (p != null && p.isOp()) {
                    ChestView view = PlayerChestView.main(p);
                    BukkitView.openView(view, p, this);
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
}
