package com.bfn.states;

import net.corda.core.contracts.Amount;
import net.corda.core.contracts.ContractState;
import net.corda.core.identity.AbstractParty;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class OfferState implements ContractState {
    final boolean wantToBuy;
    final double amount;

    public OfferState(boolean wantToBuy, double amount) {
        this.wantToBuy = wantToBuy;
        this.amount = amount;
    }

    @NotNull
    @Override
    public List<AbstractParty> getParticipants() {
        return null;
    }

    public boolean isWantToBuy() {
        return wantToBuy;
    }

    public double getAmount() {
        return amount;
    }
    @Override
    public String toString() {
        return "wantToBuy: " + wantToBuy + " amount: " + amount;
    }
}
