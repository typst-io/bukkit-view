package io.typecraft.bukkit.view.page;

import io.typecraft.bukkit.view.ChestView;
import io.typecraft.bukkit.view.ViewAction;
import io.typecraft.bukkit.view.ViewItem;
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

@Data
@With
public class PageViewLayout {
    private final String title;
    private final int row;
    private final List<Function<PageContext, ViewItem>> contents;
    private final List<Integer> slots;
    private final Map<Integer, Function<PageContext, PageViewControl>> controls;

    public static PageViewLayout ofDefault(String title, int row, Material buttonMaterial, List<Function<PageContext, ViewItem>> elements) {
        int cSize = (row - 1) * 9;
        List<Integer> slots = IntStream.range(0, cSize).boxed().collect(Collectors.toList());
        Map<Integer, Function<PageContext, PageViewControl>> controls = new HashMap<>();
        controls.put(cSize + 3, ctx -> new PageViewControl(
                createItemStack(buttonMaterial, ctx.getPage(), String.format(
                        "<- %s/%s", ctx.getPage(), ctx.getMaxPage()
                )),
                e -> new PageViewAction.SetPage(ctx.getPage() - 1)
        ));
        controls.put(cSize + 5, ctx -> new PageViewControl(
                createItemStack(buttonMaterial, ctx.getPage(), String.format(
                        "%s/%s ->", ctx.getPage(), ctx.getMaxPage()
                )),
                e -> new PageViewAction.SetPage(ctx.getPage() + 1)
        ));
        return new PageViewLayout(title, row, elements, slots, controls);
    }

    public ChestView toView(int page) {
        Map<Integer, ViewItem> items = new HashMap<>();
        int contentSize = getSlots().size();
        int count = getContents().size();
        int maxPage = count / contentSize + Math.min(count % contentSize, 1);
        int coercedPage = Math.max(Math.min(page, maxPage), 1);
        PageContext ctx = new PageContext(maxPage, coercedPage);
        List<Function<PageContext, ViewItem>> subItemList = pagingList(contentSize, coercedPage, getContents());
        // Contents
        for (int i = 0; i < subItemList.size(); i++) {
            int slot = getSlots().get(i);
            items.put(slot, subItemList.get(i).apply(ctx));
        }
        // Controls
        for (Map.Entry<Integer, Function<PageContext, PageViewControl>> pair : getControls().entrySet()) {
            PageViewControl control = pair.getValue().apply(ctx);
            items.put(pair.getKey(), new ViewItem(
                    control.getItem(),
                    event -> {
                        PageViewAction action = control.getOnClick().apply(event);
                        if (action instanceof PageViewAction.Prime) {
                            PageViewAction.Prime prime = ((PageViewAction.Prime) action);
                            return prime.getAction();
                        } else if (action instanceof PageViewAction.SetPage) {
                            PageViewAction.SetPage setPage = (PageViewAction.SetPage) action;
                            return new ViewAction.Open(toView(setPage.getPage()));
                        } else {
                            return ViewAction.NOTHING;
                        }
                    }
            ));
        }
        return new ChestView(getTitle(), getRow(), items, null);
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

    private static ItemStack createItemStack(Material mat, int amount, String display) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(display);
            item.setItemMeta(meta);
        }
        item.setAmount(Math.max(1, amount));
        return item;
    }
}
