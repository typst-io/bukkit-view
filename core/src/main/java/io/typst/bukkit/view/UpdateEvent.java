package io.typst.bukkit.view;

import lombok.Value;
import lombok.With;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

@Value
@With
public class UpdateEvent {
    Player player;
    Map<Integer, ItemStack> items;
}
