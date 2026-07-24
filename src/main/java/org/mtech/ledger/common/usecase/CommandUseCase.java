package org.mtech.ledger.common.usecase;

@FunctionalInterface
public interface CommandUseCase<C, R> {

    R invoke(C command);
}
