package org.kotyle.kylix.handlers;

import org.kotyle.kylix.result.Result;

public interface SuccessHandler<S,T> {
    public Result<T> onSuccess(S r);
}
