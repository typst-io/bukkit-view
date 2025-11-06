package io.typst.bukkit.view;

import lombok.Value;
import lombok.With;
import org.bukkit.Material;

import java.util.Set;

@Value
@With
public class InputSlot {
    int slot;
    Set<Material> whitelist;
}
