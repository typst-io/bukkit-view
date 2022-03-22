package io.typecraft.bukkit.view;

import lombok.Data;
import lombok.With;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;

@Data
@With
public class CloseEvent {
    private final ChestView view;
    private final Player player;
}
