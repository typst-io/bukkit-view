package io.typst.bukkit.view;

import lombok.Value;
import lombok.With;
import org.bukkit.entity.Player;

@Value
@With
public class OpenEvent {
    Player player;
    ChestView view;
}
