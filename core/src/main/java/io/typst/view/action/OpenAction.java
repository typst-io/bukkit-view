package io.typst.view.action;

import io.typst.view.ChestView;
import lombok.Getter;

/**
 * Open the given view as a new inventory.
 * This will cause InventoryCloseEvent and InventoryOpenEvent.
 * The inputted items by player will be returned back.
 */
@Getter
public class OpenAction<I, P> implements ViewAction<I, P> {
    private final ChestView<I, P> view;

    public OpenAction(ChestView<I, P> view) {
        this.view = view;
    }
}
