package io.typecraft.bukkit.view;

import lombok.Value;
import lombok.With;
import org.bukkit.entity.Player;

@Value
@With
public class OpenEvent {
    ChestView view;
    Player viewer;
}
