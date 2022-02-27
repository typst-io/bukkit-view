package io.typecraft.bukkit.view.page;

import io.typecraft.bukkit.view.ClickEvent;
import io.typecraft.bukkit.view.ViewAction;
import io.typecraft.bukkit.view.ViewItem;
import lombok.Data;
import org.bukkit.inventory.ItemStack;

import java.util.function.Consumer;
import java.util.function.Function;

@Data
public class PageViewControl {
    private final ItemStack item;
    private final Function<ClickEvent, PageViewAction> onClick;

    public static PageViewControl just(ItemStack item) {
        return new PageViewControl(item, ignore -> new PageViewAction.Prime(ViewAction.NOTHING));
    }

    public static PageViewControl consumer(ItemStack item, Consumer<ClickEvent> onClick) {
        return new PageViewControl(item, e -> {
            onClick.accept(e);
            return PageViewAction.NOTHING;
        });
    }

    public static PageViewControl from(ViewItem viewItem) {
        return new PageViewControl(viewItem.getItem(), e -> new PageViewAction.Prime(viewItem.getOnClick().apply(e)));
    }

    public ItemStack getItem() {
        return new ItemStack(item);
    }
}
