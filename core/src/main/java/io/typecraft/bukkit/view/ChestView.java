package io.typecraft.bukkit.view;

import lombok.Data;
import lombok.With;

import java.util.Map;
import java.util.function.Function;

@Data
@With
public class ChestView {
    private final String title;
    private final int row;
    private final Map<Integer, ViewItem> items;
    private final Function<CloseEvent, ViewAction> onClose;

    public ChestView(String title, int row, Map<Integer, ViewItem> items, Function<CloseEvent, ViewAction> onClose) {
        this.title = title;
        this.row = row;
        this.items = items;
        this.onClose = onClose;
    }

    public ChestView(String title, int row, Map<Integer, ViewItem> items) {
        this(title, row, items, ignored -> ViewAction.NOTHING);
    }
}
