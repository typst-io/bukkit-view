package io.typst.view.action;

/**
 * Open the current view as a new inventory.
 * This is equal to {@link OpenAction} with the current view as a parameter.
 */
public class ReopenAction<I, P> implements ViewAction<I, P> {
    @SuppressWarnings("rawtypes")
    private static final ReopenAction INSTANCE = new ReopenAction();

    @SuppressWarnings("unchecked")
    public static <I, P> ReopenAction<I, P> of() {
        return INSTANCE;
    }
}
