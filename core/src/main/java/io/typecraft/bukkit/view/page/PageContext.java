package io.typecraft.bukkit.view.page;

import lombok.Data;
import lombok.With;

@Data
@With
public class PageContext {
    private final int maxPage;
    private final int page;

    public boolean canNext() {
        return getPage() < getMaxPage();
    }

    public boolean canPrev() {
        return getPage() >= 2;
    }
}
