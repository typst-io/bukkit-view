package io.typecraft.bukkit.view;


import lombok.Data;

import java.util.Map;

@Data
public class View {
    private final String title;
    private final int row;
    private final Map<Integer, ViewItem> items;
}
