package io.typst.bukkit.view;

import lombok.Data;
import lombok.With;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.inventory.ItemStack;

@Data
@With
public class BottomClickEvent {
    private final ChestView view;
    private final Player player;
    private final ClickType click;
    private final ItemStack clickItem;
    private final InventoryAction action;
}
