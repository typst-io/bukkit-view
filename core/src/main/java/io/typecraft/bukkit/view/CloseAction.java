package io.typecraft.bukkit.view;

import lombok.Data;

public interface CloseAction {
    CloseAction NOTHING = new CloseAction.Prime(ViewAction.NOTHING);
    CloseAction CANCEL = new CloseAction.Prime(ViewAction.CANCEL);

    @Data
    class Prime implements CloseAction {
        private final ViewAction action;
    }
}

