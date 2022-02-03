package io.typecraft.bukkit.view.page;

import io.typecraft.bukkit.view.ClickEvent;
import io.typecraft.bukkit.view.ViewAction;
import lombok.Data;
import org.bukkit.inventory.ItemStack;

import java.util.function.Consumer;
import java.util.function.Function;

@Data
public class PageViewControl {
    private final ItemStack item;
    private final Function<ClickEvent, PageViewAction> onClick;

    public static PageViewControl just(ItemStack item) {
        return new PageViewControl(item, ignore -> new PageViewAction.Action(ViewAction.NOTHING));
    }

    public static PageViewControl consumer(ItemStack item, Consumer<ClickEvent> onClick) {
        return new PageViewControl(item, e -> {
            onClick.accept(e);
            return PageViewAction.NOTHING;
        });
    }

    public ItemStack getItem() {
        return new ItemStack(item);
    }
}
