package io.typst.view.action;

import io.typst.view.ViewContents;
import lombok.Value;
import lombok.With;

/**
 * Update the current inventory of player to the given contents.
 * This won't cause InventoryCloseEvent and InventoryOpenEvent.
 * The inputted items by player will be overridden.
 */
@Value
@With
public class UpdateAction<I, P> implements ViewAction<I, P> {
    ViewContents<I, P> contents;
}
