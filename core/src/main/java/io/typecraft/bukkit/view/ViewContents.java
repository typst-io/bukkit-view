package io.typecraft.bukkit.view;

import lombok.Data;
import lombok.With;
import org.bukkit.inventory.ItemStack;

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

    public static ViewContents ofControls(Map<Integer, ViewControl> controls) {
        return new ViewContents(controls, new HashMap<>());
    }
}
