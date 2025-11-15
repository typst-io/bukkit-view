package io.typst.view.bukkit.plugin.view;

import io.typst.view.ChestView;
import io.typst.view.ViewContents;
import io.typst.view.ViewControl;
import io.typst.view.action.CloseAction;
import io.typst.view.action.OpenAction;
import io.typst.inventory.bukkit.BukkitItem;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class PlayerChestView {
    public static ChestView<ItemStack, Player> main(Player p) {
        Map<Integer, ViewControl<ItemStack, Player>> controls = new HashMap<>();
        setupLayout(controls);
        // show player armor contents at 19 slot
        ViewControl<ItemStack, Player> equip = ViewControl.<ItemStack, Player>of(
                e -> BukkitItem.builder()
                        .material(Material.IRON_CHESTPLATE)
                        .displayName(String.format("Show %s's equipments.", p.getName()))
                        .build().create(),
                e -> new OpenAction<>(equip(p))
        );
        controls.put(19, equip);
        return ChestView.<ItemStack, Player>builder()
                .title("Chest")
                .contents(ViewContents.ofControls(controls))
                .build();
    }

    public static ChestView<ItemStack, Player> equip(Player p) {
        Map<Integer, ViewControl<ItemStack, Player>> controls = new HashMap<>();
        setupLayout(controls);
        // prev at 7 slot
        controls.put(7, ViewControl.<ItemStack, Player>of(
                e -> BukkitItem.builder()
                        .material(Material.COMPASS)
                        .displayName("Go to previous")
                        .build().create(),
                e -> new OpenAction<>(main(p))
        ));
        // refresh at 6 slot
        controls.put(6, ViewControl.<ItemStack, Player>of(
                e -> BukkitItem.builder()
                        .material(Material.CLOCK)
                        .displayName("Refresh")
                        .build().create(),
                e -> new OpenAction<>(equip(p))
        ));
        // equips
        int i = 0;
        for (ItemStack item : p.getInventory().getArmorContents()) {
            controls.put(9 + i, ViewControl.just(item));
            i++;
        }
        return ChestView.<ItemStack, Player>builder()
                .title("Equip")
                .contents(ViewContents.ofControls(controls))
                .build();
    }

    private static void setupLayout(Map<Integer, ViewControl<ItemStack, Player>> controls) {
        // wall at 0~8 slot
        ViewControl<ItemStack, Player> wall = ViewControl.just(new ItemStack(Material.BLACK_STAINED_GLASS_PANE));
        for (int i = 0; i < 9; i++) {
            controls.put(i, wall);
        }
        // exit at 8 slot
        ViewControl<ItemStack, Player> exit = ViewControl.<ItemStack, Player>of(
                e -> BukkitItem.builder()
                        .material(Material.BARRIER)
                        .displayName("§cEXIT")
                        .build().create(),
                e -> new CloseAction<>(true)
        );
        controls.put(8, exit);
    }
}
