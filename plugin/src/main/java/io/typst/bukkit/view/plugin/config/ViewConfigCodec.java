package io.typst.bukkit.view.plugin.config;

import io.typst.bukkit.view.ChestView;
import io.typst.bukkit.view.ViewAction;
import io.typst.bukkit.view.ViewContents;
import io.typst.bukkit.view.ViewControl;
import io.typst.bukkit.view.plugin.ViewPlugin;
import io.typst.bukkit.view.plugin.command.StringCommandExecutor;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class ViewConfigCodec {

    public static Map<String, ChestView> loadViewConfigs(Plugin plugin, ConfigurationSection section) {
        Map<String, ChestView> ret = new HashMap<>();
        for (String viewName : section.getKeys(false)) {
            ConfigurationSection sub = section.getConfigurationSection(viewName);
            ChestView view = loadViewConfig(sub).orElse(null);
            if (view == null) {
                plugin.getLogger().warning("Error while reading the view: " + viewName);
                continue;
            }
            ret.put(viewName, view);
        }
        return ret;
    }

    public static Optional<ChestView> loadViewConfig(ConfigurationSection section) {
        int row = section.getInt("row", 1);
        String title = section.getString("title", "");
        Map<Integer, ViewControl> controls = new HashMap<>();
        ConfigurationSection contents = section.getConfigurationSection("contents");
        if (contents == null) {
            contents = new MemoryConfiguration();
        }
        for (String slotStr : contents.getKeys(false)) {
            int slot = parseInt(slotStr).orElse(-1);
            ItemStack item = parseItem(contents.getConfigurationSection(slotStr + ".item")).orElse(null);
            List<String> commands = contents.getStringList(slotStr + ".commands");
            if (item == null) {
                continue;
            }
            if (slot >= 0) {
                controls.put(slot, ViewControl.of(
                        e -> ViewPlugin.inst.replacePlaceholder(e.getPlayer(), item.clone()),
                        e -> {
                            List<String> cmds = commands.stream()
                                    .map(a -> a.replace("%player%", e.getPlayer().getName()))
                                    .collect(Collectors.toList());
                            for (String cmd : cmds) {
                                StringCommandExecutor.execute(ViewPlugin.inst, e.getPlayer(), cmd);
                            }
                            return ViewAction.NOTHING;
                        }
                ));
            }
        }
        return Optional.of(ChestView.builder()
                .title(title)
                .row(row)
                .contents(ViewContents.of(controls, new HashMap<>()))
                .onClose(e -> ViewAction.NOTHING)
                .onContentsUpdate(e -> {})
                .build());
    }

    public static Optional<ItemStack> parseItem(ConfigurationSection xs) {
        if (xs == null) {
            return Optional.empty();
        }
        ViewPlugin plugin = ViewPlugin.inst;
        String mat = xs.getString("type", "");
        String name = xs.getString("name", "");
        int amount = xs.getInt("amount", 1);
        List<String> lore = xs.getStringList("lore");
        Material material = null;
        try {
            material = Material.valueOf(mat.toUpperCase());
            ItemStack item = new ItemStack(material);
            item.setAmount(amount);
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                if (!name.isEmpty()) {
                    meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
                }
                if (!lore.isEmpty()) {
                    meta.setLore(lore.stream()
                            .map(a -> ChatColor.translateAlternateColorCodes('&', a))
                            .collect(Collectors.toList()));
                }
                item.setItemMeta(meta);
            }
            return Optional.of(item);
        } catch (Exception ex) {
            return Optional.empty();
        }
    }

    public static Optional<Integer> parseInt(String x) {
        try {
            return Optional.of(Integer.parseInt(x));
        } catch (Exception ex) {

            return Optional.empty();
        }
    }
}
