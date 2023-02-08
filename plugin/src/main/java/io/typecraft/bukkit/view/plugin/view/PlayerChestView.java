package io.typecraft.bukkit.view.plugin.view;

import io.typecraft.bukkit.view.ChestView;
import io.typecraft.bukkit.view.ViewAction;
import io.typecraft.bukkit.view.ViewContents;
import io.typecraft.bukkit.view.ViewControl;
import io.typecraft.bukkit.view.item.BukkitItem;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class PlayerChestView {
    public static ChestView create() {
        Map<Integer, ViewControl> controls = new HashMap<>();
        // wall at 0~8 slot
        ViewControl wall = ViewControl.just(new ItemStack(Material.BLACK_STAINED_GLASS_PANE));
        for (int i = 0; i < 9; i++) {
            controls.put(i, wall);
        }
        // exit at 8 slot
        ViewControl exit = ViewControl.of(
            BukkitItem.ofJust(Material.BARRIER)
                .withName("§cEXIT")
                .build(),
            e -> ViewAction.CLOSE
        );
        controls.put(8, exit);

        return ChestView.just("Chest", 6, ViewContents.ofControls(controls));
    }
}
