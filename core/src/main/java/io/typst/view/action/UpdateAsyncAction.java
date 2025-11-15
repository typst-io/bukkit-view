package io.typst.view.action;

import io.typst.view.ViewContents;
import lombok.Value;
import lombok.With;

import java.util.concurrent.Future;

@Value
@With
public class UpdateAsyncAction<I, P> implements ViewAction<I, P> {
    Future<ViewContents<I, P>> contentsFuture;
}
