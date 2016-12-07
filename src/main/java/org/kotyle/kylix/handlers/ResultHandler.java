package org.kotyle.kylix.handlers;


import org.kotyle.kylix.error.Fault;

public interface ResultHandler<S,T> {
    public T onFailure(Fault fault);
    public T onSuccess(S s);
}
