package io.typst.view.action;

/**
 * Action nothing.
 * Return this as an empty or default action.
 */
public class NothingAction<I, P> implements ViewAction<I, P> {
    @SuppressWarnings("rawtypes")
    private static final NothingAction EMPTY = new NothingAction<>();

    @SuppressWarnings("unchecked")
    public static <I, P> NothingAction<I, P> of() {
        return EMPTY;
    }
}
