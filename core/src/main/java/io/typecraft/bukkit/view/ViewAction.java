package io.typecraft.bukkit.view;

import lombok.Data;

import java.util.concurrent.Future;

public interface ViewAction {
    Nothing NOTHING = new Nothing();
    Close CLOSE = new Close();
    Cancel CANCEL = new Cancel();

    class Nothing implements ViewAction {
        private Nothing() {
        }
    }

    @Data
    class Open implements ViewAction {
        private final ChestView view;
    }

    @Data
    class Cancel implements ViewAction {
        private Cancel() {
        }
    }

    @Data
    class OpenAsync implements ViewAction {
        private final Future<ChestView> viewFuture;
    }

    @Data
    class Close implements ViewAction {
        private Close() {
        }
    }
}
