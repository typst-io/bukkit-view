package io.typst.view.action;

import io.typst.view.ChestView;
import lombok.Data;
import lombok.Value;

import java.util.concurrent.Future;

/**
 * Open the given future of view as a new inventory.
 * This is equal to {@link OpenAction} after the future completed.
 * The internal handler will open it in the main thread.
 */
@Data
@Value
public class OpenAsyncAction<I, P> implements ViewAction<I, P> {
    Future<ChestView<I, P>> future;

    public OpenAsyncAction(Future<ChestView<I, P>> future) {
        this.future = future;
    }
}
