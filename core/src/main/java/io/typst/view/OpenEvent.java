package io.typst.view;

import lombok.Value;
import lombok.With;

@Value
@With
public class OpenEvent<I, P> {
    P player;
    ChestView<I, P> view;
}
