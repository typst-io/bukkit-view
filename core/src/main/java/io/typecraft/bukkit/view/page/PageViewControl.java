package io.typecraft.bukkit.view.page;

import io.typecraft.bukkit.view.ClickEvent;
import io.typecraft.bukkit.view.ViewAction;
import io.typecraft.bukkit.view.ViewControl;
import lombok.Data;
import lombok.With;
import org.bukkit.inventory.ItemStack;

import java.util.function.Consumer;
import java.util.function.Function;

@Data
@With
public class PageViewControl {
    private final ItemStack item;
    private final Function<ClickEvent, PageViewAction> onClick;

    @Deprecated
    public PageViewControl(ItemStack item, Function<ClickEvent, PageViewAction> onClick) {
        this.item = item;
        this.onClick = onClick;
    }

    public static PageViewControl of(ItemStack item, Function<ClickEvent, PageViewAction> onClick) {
        return new PageViewControl(item, onClick);
    }

    public static PageViewControl just(ItemStack item) {
        return of(item, ignore -> new PageViewAction.Prime(ViewAction.NOTHING));
    }

    public static PageViewControl consumer(ItemStack item, Consumer<ClickEvent> onClick) {
        return of(item, e -> {
            onClick.accept(e);
            return PageViewAction.NOTHING;
        });
    }

    public static PageViewControl from(ViewControl viewControl) {
        return new PageViewControl(viewControl.getItem(), e -> new PageViewAction.Prime(viewControl.getOnClick().apply(e)));
    }

    public ItemStack getItem() {
        return new ItemStack(item);
    }
}
