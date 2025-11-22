package io.typst.view;

import lombok.Value;
import lombok.With;

import java.util.Map;

@Value
@With
public class UpdateEvent<I, P> {
    P player;
    Map<Integer, I> items;
}
