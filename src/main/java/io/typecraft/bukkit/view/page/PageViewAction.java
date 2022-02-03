package io.typecraft.bukkit.view.page;

import io.typecraft.bukkit.view.ViewAction;
import lombok.Data;

public interface PageViewAction {
    PageViewAction NOTHING = new Action(ViewAction.NOTHING);

    @Data
    class Action implements PageViewAction {
        private final ViewAction action;
    }

    @Data
    class SetPage implements PageViewAction {
        private final int page;
    }
}
