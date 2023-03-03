package io.typecraft.bukkit.view.item;

import lombok.Data;
import lombok.With;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

@Data(staticConstructor = "of")
@With
public class BukkitItem {
    private final Material material;
    private final int amount;
    private final short durability;
    private final String name;
    private final List<String> lore;
    private final Integer modelData;
    private final Map<Enchantment, Integer> enchants;

    public static BukkitItem ofJust(Material material) {
        return of(material, 1, (short) 0, "", Collections.emptyList(), 0, Collections.emptyMap());
    }

    public BukkitItem withEnchant(Enchantment enchant, int level) {
        Map<Enchantment, Integer> enchants = this.enchants.isEmpty() ? new HashMap<>() : this.enchants;
        enchants.put(enchant, level);
        return withEnchants(enchants);
    }

    public Map<Enchantment, Integer> getEnchants() {
        return new HashMap<>(enchants);
    }

    @SuppressWarnings("deprecation")
    public void update(ItemStack x) {
        // header
        x.setAmount(getAmount());
        x.setDurability(getDurability());
        // meta
        ItemMeta meta = x.getItemMeta();
        if (meta != null) {
            if (!getName().isEmpty()) {
                meta.setDisplayName(getName());
            }
            if (!getLore().isEmpty()) {
                meta.setLore(new ArrayList<>(getLore()));
            }
            if (getModelData() != 0) {
                meta.setCustomModelData(getModelData());
            }
            for (Map.Entry<Enchantment, Integer> pair : getEnchants().entrySet()) {
                meta.addEnchant(pair.getKey(), pair.getValue(), true);
            }
            x.setItemMeta(meta);
        }
    }

    public ItemStack build() {
        ItemStack item = new ItemStack(material);
        update(item);
        return item;
    }
}
