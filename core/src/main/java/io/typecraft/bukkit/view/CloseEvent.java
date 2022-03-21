package io.typecraft.bukkit.view;

import lombok.Data;
import lombok.With;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;

@Data
@With
public class CloseEvent {
    private final Player player;
    private final InventoryView view;
    private final Inventory action;
}
