package io.typst.view;

import lombok.Data;
import lombok.With;

@Data
@With
public class CloseEvent<I, P> {
    private final P player;
    private final ChestView<I, P> view;
}
