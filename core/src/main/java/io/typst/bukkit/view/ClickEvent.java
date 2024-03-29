package io.typst.bukkit.view;

import lombok.Data;
import lombok.With;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;

@Data
@With
public class ClickEvent {
    private final ChestView view;
    private final Player player;
    // TODO: Can this normalize?
    private final ClickType click;
    private final InventoryAction action;
    private final int hotbarKey;
}
