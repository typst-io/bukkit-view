package io.typst.view.action;

import lombok.Value;
import lombok.With;

/**
 * Close the inventory player seeing.
 * This will cause InventoryCloseEvent.
 * The inputted items by player will be returned back.
 */
@Value
@With
public class CloseAction<I, P> implements ViewAction<I, P> {
    boolean giveBackItems;
}
