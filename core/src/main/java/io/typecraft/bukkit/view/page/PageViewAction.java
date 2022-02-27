package io.typecraft.bukkit.view.page;

import io.typecraft.bukkit.view.ViewAction;
import lombok.Data;

public interface PageViewAction {
    PageViewAction NOTHING = new Prime(ViewAction.NOTHING);
    PageViewAction CLOSE = new Prime(ViewAction.CLOSE);

    @Data
    class Prime implements PageViewAction {
        private final ViewAction action;
    }

    @Data
    class SetPage implements PageViewAction {
        private final int page;
    }
}
