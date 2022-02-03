package io.typecraft.bukkit.view;

import lombok.Data;
import org.bukkit.entity.Player;

@Data
public class ClickEvent {
    private final Player player;
}
