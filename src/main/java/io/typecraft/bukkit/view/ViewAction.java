package io.typecraft.bukkit.view;

import lombok.Data;

public interface ViewAction {
    Nothing NOTHING = new Nothing();
    Close CLOSE = new Close();

    class Nothing implements ViewAction {
        private Nothing() {
        }
    }

    @Data
    class Open implements ViewAction {
        private final View view;
    }

    @Data
    class Close implements ViewAction {
        private Close() {
        }
    }
}
