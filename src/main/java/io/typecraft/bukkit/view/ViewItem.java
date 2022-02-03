package io.typecraft.bukkit.view;

import lombok.Data;
import org.bukkit.inventory.ItemStack;

import java.util.function.Consumer;
import java.util.function.Function;


@Data
public class ViewItem {
    private final ItemStack item;
    private final Function<ClickEvent, ViewAction> onClick;

    public static ViewItem just(ItemStack item) {
        return new ViewItem(item, ignore -> ViewAction.NOTHING);
    }

    public static ViewItem consumer(ItemStack item, Consumer<ClickEvent> onClick) {
        return new ViewItem(item, e -> {
            onClick.accept(e);
            return ViewAction.NOTHING;
        });
    }

    public ItemStack getItem() {
        return new ItemStack(item);
    }
}
