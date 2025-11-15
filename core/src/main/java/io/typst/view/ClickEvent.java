package io.typst.view;

import lombok.Data;
import lombok.With;

@Data
@With
public class ClickEvent<I, P> {
    private final ChestView<I, P> view;
    private final P player;
    // TODO: Can this normalize?
    private final String click;
    private final String action;
    private final int hotbarKey;
}
