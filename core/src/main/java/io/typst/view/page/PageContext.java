package io.typst.view.page;

import lombok.Data;
import lombok.With;

@Data(staticConstructor = "of")
@With
public class PageContext<I, P> {
    private final PageViewLayout<I, P> layout;
    private final int maxPage;
    private final int page;

    public boolean canNext() {
        return getPage() < getMaxPage();
    }

    public boolean canPrev() {
        return getPage() >= 2;
    }
}
