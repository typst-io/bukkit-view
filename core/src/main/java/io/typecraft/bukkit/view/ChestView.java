package io.typecraft.bukkit.view;

import lombok.Data;
import lombok.With;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Data
@With
public class ChestView {
    private final String title;
    private final int row;
    private final Map<Integer, ViewItem> controls;
    private final Map<Integer, ItemStack> contents;
    private final Function<CloseEvent, ViewAction> onClose;

    public ChestView(String title, int row, Map<Integer, ViewItem> controls, Map<Integer, ItemStack> contents, Function<CloseEvent, ViewAction> onClose) {
        this.title = title;
        this.row = row;
        this.controls = controls;
        this.contents = contents;
        this.onClose = onClose;
    }

    public ChestView(String title, int row, Map<Integer, ViewItem> controls) {
        this(title, row, controls, new HashMap<>(), ignored -> ViewAction.NOTHING);
    }

    @Deprecated
    public Map<Integer, ViewItem> getItems() {
        return getControls();
    }
}
