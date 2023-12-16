package io.typecraft.bukkit.view;

import lombok.Data;
import lombok.Value;
import lombok.With;

import java.util.concurrent.Future;

/**
 * The view actions will be handled by onClick, onClose in {@link ViewControl}
 */
public interface ViewAction {
    /**
     * {@link Nothing}
     */
    Nothing NOTHING = new Nothing();
    /**
     * {@link Close}
     */
    Close CLOSE = new Close(true);
    /**
     * {@link Reopen}
     */
    Reopen REOPEN = new Reopen();

    /**
     * Action nothing.
     * Return this as an empty or default action.
     */
    class Nothing implements ViewAction {
        private Nothing() {
        }
    }


    /**
     * Open the given view as a new inventory.
     * This will cause InventoryCloseEvent and InventoryOpenEvent.
     * The inputted items by player will be returned back.
     */
    @Data
    class Open implements ViewAction {
        private final ChestView view;
    }

    // TODO: remove?

    /**
     * Open the current view as a new inventory.
     * This is equal to {@link Open} with the current view as a parameter.
     */
    @Data
    class Reopen implements ViewAction {
    }

    /**
     * Open the given future of view as a new inventory.
     * This is equal to {@link Open} after the future completed.
     * The internal handler will open it in the main thread.
     */
    @Data
    class OpenAsync implements ViewAction {
        private final Future<ChestView> viewFuture;
    }

    /**
     * Update the current inventory of player to the given contents.
     * This won't cause InventoryCloseEvent and InventoryOpenEvent.
     * The inputted items by player will be overridden.
     */
    @Data
    class Update implements ViewAction {
        private final ViewContents contents;
    }

    /**
     * Update the current inventory of player to the given contents of future.
     * This is equal to {@link Update} after the future completed.
     * The internal handler will open it in the main thread.
     */
    @Data
    class UpdateAsync implements ViewAction {
        private final Future<ViewContents> contentsFuture;
    }

    /**
     * Close the inventory player seeing.
     * This will cause InventoryCloseEvent.
     * The inputted items by player will be returned back.
     */
    @Value
    @With
    class Close implements ViewAction {
        boolean giveBackItems;

        public Close(boolean giveBackItems) {
            this.giveBackItems = giveBackItems;
        }
    }
}
