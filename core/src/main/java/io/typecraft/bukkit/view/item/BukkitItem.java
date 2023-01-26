package io.typecraft.bukkit.view.item;

import lombok.Data;
import lombok.With;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Data(staticConstructor = "of")
@With
public class BukkitItem {
    private final Material material;
    private final int amount;
    private final short durability;
    private final String name;
    private final List<String> lore;

    public static BukkitItem ofJust(Material material) {
        return of(material, 1, (short) 0, "", Collections.emptyList());
    }

    public static BukkitItem ofSimple(Material material, String name) {
        return of(material, 1, (short) 0, name, Collections.emptyList());
    }

    public static BukkitItem ofSimple(Material material, String name, List<String> lores) {
        return of(material, 1, (short) 0, name, lores);
    }

    @SuppressWarnings("deprecation")
    public void update(ItemStack x) {
        // header
        x.setAmount(getAmount());
        x.setDurability(getDurability());
        // meta
        ItemMeta meta = x.getItemMeta();
        if (meta != null && !getName().isEmpty()) {
            meta.setDisplayName(getName());
        }
        if (meta != null && !getLore().isEmpty()) {
            meta.setLore(new ArrayList<>(getLore()));
        }
        if (meta != null) {
            x.setItemMeta(meta);
        }

    }

    public ItemStack build() {
        ItemStack item = new ItemStack(material);
        update(item);
        return item;
    }
}
