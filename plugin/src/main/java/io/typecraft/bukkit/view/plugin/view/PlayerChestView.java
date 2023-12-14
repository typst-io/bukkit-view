package io.typecraft.bukkit.view.plugin.view;

import io.typecraft.bukkit.view.ChestView;
import io.typecraft.bukkit.view.ViewAction;
import io.typecraft.bukkit.view.ViewContents;
import io.typecraft.bukkit.view.ViewControl;
import io.typecraft.bukkit.view.item.BukkitItem;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class PlayerChestView {
    public static ChestView main(Player p) {
        Map<Integer, ViewControl> controls = new HashMap<>();
        setupLayout(controls);
        // show player armor contents at 19 slot
        ViewControl equip = ViewControl.of(
            e -> BukkitItem.ofJust(Material.IRON_CHESTPLATE)
                .withName(String.format("Show %s's equipments.", p.getName()))
                .build(),
            e -> new ViewAction.Open(equip(p))
        );
        controls.put(19, equip);

        return ChestView.just("Chest", 6, ViewContents.ofControls(controls));
    }

    public static ChestView equip(Player p) {
        Map<Integer, ViewControl> controls = new HashMap<>();
        setupLayout(controls);
        // prev at 7 slot
        controls.put(7, ViewControl.of(
            e -> BukkitItem.ofJust(Material.COMPASS)
                .withName("Go to previous")
                .build(),
            e -> new ViewAction.Open(main(p))
        ));
        // refresh at 6 slot
        controls.put(6, ViewControl.of(
                e -> BukkitItem.ofJust(Material.CLOCK)
                .withName("Refresh")
                .build(),
            e -> new ViewAction.Open(equip(p))
        ));
        // equips
        int i = 0;
        for (ItemStack item : p.getInventory().getArmorContents()) {
            controls.put(9 + i, ViewControl.just(item));
            i++;
        }
        return ChestView.just("Equip", 6, ViewContents.ofControls(controls));
    }

    private static void setupLayout(Map<Integer, ViewControl> controls) {
        // wall at 0~8 slot
        ViewControl wall = ViewControl.just(new ItemStack(Material.BLACK_STAINED_GLASS_PANE));
        for (int i = 0; i < 9; i++) {
            controls.put(i, wall);
        }
        // exit at 8 slot
        ViewControl exit = ViewControl.of(
                e -> BukkitItem.ofJust(Material.BARRIER)
                .withName("§cEXIT")
                .build(),
            e -> ViewAction.CLOSE
        );
        controls.put(8, exit);
    }
}
