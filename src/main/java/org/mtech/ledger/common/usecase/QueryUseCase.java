package org.mtech.ledger.common.usecase;

@FunctionalInterface
public interface QueryUseCase<Q, R> {

    R invoke(Q query);
}
