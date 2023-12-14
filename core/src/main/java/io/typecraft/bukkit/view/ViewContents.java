package io.typecraft.bukkit.view;

import lombok.Data;
import lombok.With;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Data(staticConstructor = "of")
@With
public class ViewContents {
    /**
     * Fixed, readonly slot controls.
     */
    private final Map<Integer, ViewControl> controls;
    /**
     * Player accessible slot items.
     */
    private final Map<Integer, ItemStack> items;

    /**
     * Update the player accessible slots from the given inventory.
     */
    public ViewContents updated(Inventory inv) {
        ItemStack[] contents = inv.getContents();
        Map<Integer, ItemStack> newItems = new HashMap<>(items);
        for (int i = 0; i < contents.length; i++) {
            if (controls.containsKey(i)) continue;
            ItemStack item = contents[i];
            if (item != null && item.getType() != Material.AIR) {
                newItems.put(i, item);
            } else {
                newItems.remove(i);
            }
        }
        return withItems(newItems);
    }

    public static ViewContents ofControls(Map<Integer, ViewControl> controls) {
        return new ViewContents(controls, Collections.emptyMap());
    }
}
