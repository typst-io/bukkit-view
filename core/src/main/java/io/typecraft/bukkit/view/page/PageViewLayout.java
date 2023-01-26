package io.typecraft.bukkit.view.page;

import io.typecraft.bukkit.view.ChestView;
import io.typecraft.bukkit.view.ViewAction;
import io.typecraft.bukkit.view.ViewContents;
import io.typecraft.bukkit.view.ViewControl;
import io.typecraft.bukkit.view.item.BukkitItem;
import lombok.Data;
import lombok.With;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Data(staticConstructor = "of")
@With
public class PageViewLayout {
    private final String title;
    private final int row;
    private final List<Function<PageContext, ViewControl>> elements;
    private final List<Integer> slots;
    private final Map<Integer, Function<PageContext, PageViewControl>> controls;

    public static PageViewLayout ofDefault(String title, int row, Material buttonMaterial, List<Function<PageContext, ViewControl>> elements) {
        int cSize = (row - 1) * 9;
        List<Integer> slots = IntStream.range(0, cSize).boxed().collect(Collectors.toList());
        Map<Integer, Function<PageContext, PageViewControl>> controls = new HashMap<>();
        controls.put(cSize + 3, ctx -> PageViewControl.of(
            BukkitItem.ofJust(buttonMaterial)
                .withAmount(Math.max(1, ctx.getPage()))
                .withName(String.format(
                    "<- %s/%s", ctx.getPage(), ctx.getMaxPage()
                ))
                .build(),
            e -> new PageViewAction.SetPage(ctx.getPage() - 1)
        ));
        controls.put(cSize + 5, ctx -> PageViewControl.of(
            BukkitItem.ofJust(buttonMaterial)
                .withAmount(Math.max(1, ctx.getPage()))
                .withName(String.format(
                    "%s/%s ->", ctx.getPage(), ctx.getMaxPage()
                ))
                .build(),
            e -> new PageViewAction.SetPage(ctx.getPage() + 1)
        ));
        return of(title, row, elements, slots, controls);
    }

    public ChestView toView(int page) {
        Map<Integer, ViewControl> viewControls = new HashMap<>();
        int contentSize = getSlots().size();
        int count = getElements().size();
        int maxPage = count / contentSize + Math.min(count % contentSize, 1);
        int coercedPage = Math.max(Math.min(page, maxPage), 1);
        PageContext ctx = PageContext.of(maxPage, coercedPage);
        List<Function<PageContext, ViewControl>> subItemList = pagingList(contentSize, coercedPage, getElements());
        // Contents
        for (int i = 0; i < subItemList.size(); i++) {
            int slot = getSlots().get(i);
            viewControls.put(slot, subItemList.get(i).apply(ctx));
        }
        // Controls
        for (Map.Entry<Integer, Function<PageContext, PageViewControl>> pair : getControls().entrySet()) {
            PageViewControl control = pair.getValue().apply(ctx);
            viewControls.put(pair.getKey(), ViewControl.of(
                control.getItem(),
                event -> {
                    PageViewAction action = control.getOnClick().apply(event);
                    if (action instanceof PageViewAction.Prime) {
                        PageViewAction.Prime prime = ((PageViewAction.Prime) action);
                        return prime.getAction();
                    } else if (action instanceof PageViewAction.SetPage) {
                        PageViewAction.SetPage setPage = (PageViewAction.SetPage) action;
                        ChestView newView = toView(setPage.getPage());
                        return new ViewAction.Update(newView.getContents());
                    } else {
                        return ViewAction.NOTHING;
                    }
                }
            ));
        }
        ViewContents contents = ViewContents.ofControls(viewControls);
        return ChestView.just(getTitle(), getRow(), contents);
    }

    private static <T> List<T> pagingList(int elementSize, int page, List<T> list) {
        int start = (page - 1) * elementSize;
        int end = page * elementSize;
        return list.isEmpty()
            ? Collections.emptyList()
            : list.subList(
            Math.min(Math.max(start, 0), list.size()),
            Math.min(Math.max(end, 0), list.size()));
    }

}
