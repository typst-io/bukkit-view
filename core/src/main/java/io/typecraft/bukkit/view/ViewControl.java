package io.typecraft.bukkit.view;

import lombok.Data;
import lombok.With;
import org.bukkit.inventory.ItemStack;

import java.util.function.Consumer;
import java.util.function.Function;

@Data(staticConstructor = "of")
@With
public class ViewControl {
    private final ItemStack item;
    private final Function<ClickEvent, ViewAction> onClick;

    public static ViewControl just(ItemStack item) {
        return of(item, ignore -> ViewAction.NOTHING);
    }

    public static ViewControl consumer(ItemStack item, Consumer<ClickEvent> onClick) {
        return of(item, e -> {
            onClick.accept(e);
            return ViewAction.NOTHING;
        });
    }

    public ItemStack getItem() {
        return new ItemStack(item);
    }
}
