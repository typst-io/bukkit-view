package io.typst.view;

import lombok.Value;
import lombok.With;

import java.util.Set;

@Value
@With
public class InputSlot {
    int slot;
    Set<String> whitelist;
}
