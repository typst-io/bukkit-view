package io.typecraft.bukkit.view;

import lombok.Data;
import lombok.With;
import org.bukkit.inventory.ItemStack;

import java.util.function.Consumer;
import java.util.function.Function;

@Data
@With
public class ViewItem {
    private final ItemStack item;
    private final Function<ClickEvent, ViewAction> onClick;

    @Deprecated
    public ViewItem(ItemStack item, Function<ClickEvent, ViewAction> onClick) {
        this.item = item;
        this.onClick = onClick;
    }

    public static ViewItem of(ItemStack item, Function<ClickEvent, ViewAction> onClick) {
        return new ViewItem(item, onClick);
    }

    public static ViewItem just(ItemStack item) {
        return of(item, ignore -> ViewAction.NOTHING);
    }

    public static ViewItem consumer(ItemStack item, Consumer<ClickEvent> onClick) {
        return of(item, e -> {
            onClick.accept(e);
            return ViewAction.NOTHING;
        });
    }

    public ItemStack getItem() {
        return new ItemStack(item);
    }
}
