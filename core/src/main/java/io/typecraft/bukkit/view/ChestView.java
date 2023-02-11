package io.typecraft.bukkit.view;

import lombok.Data;
import lombok.With;

import java.util.function.Function;

@Data(staticConstructor = "of")
@With
public class ChestView {
    private final String title;
    private final int row;
    private final ViewAction state;
    private final ViewContents contents;
    private final Function<CloseEvent, ViewAction> onClose;

    /**
     * A simple constructor of ChestView
     *
     * @param title    the inventory title
     * @param row      the inventory row
     * @param contents the inventory contents
     * @return ChestView
     */
    public static ChestView just(String title, int row, ViewContents contents) {
        return new ChestView(title, row, ViewAction.NOTHING, contents, e -> ViewAction.NOTHING);
    }
}
