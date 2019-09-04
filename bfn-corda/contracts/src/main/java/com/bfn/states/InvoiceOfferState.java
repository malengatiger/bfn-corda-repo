package com.bfn.states;

import com.bfn.contracts.InvoiceOfferContract;
import com.google.common.collect.ImmutableList;
import com.r3.corda.lib.accounts.contracts.states.AccountInfo;
import net.corda.core.contracts.Amount;
import net.corda.core.contracts.BelongsToContract;
import net.corda.core.contracts.ContractState;
import net.corda.core.identity.AbstractParty;
import net.corda.core.serialization.CordaSerializable;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@CordaSerializable
@BelongsToContract(InvoiceOfferContract.class)
public class InvoiceOfferState implements ContractState {
    private final UUID invoiceId;
    private final double offerAmount, discount;
    private final AccountInfo supplier, investor;
    private final Date offerDate, investorDate;

    public InvoiceOfferState(UUID invoiceId, double offerAmount, double discount,
                             AccountInfo supplier, AccountInfo investor,
                             Date offerDate, Date investorDate) {
        this.invoiceId = invoiceId;
        this.offerAmount = offerAmount;
        this.discount = discount;
        this.supplier = supplier;
        this.investor = investor;
        this.offerDate = offerDate;
        this.investorDate = investorDate;
    }

    @NotNull
    @Override
    public List<AbstractParty> getParticipants() {

        return ImmutableList.of(supplier.getHost(), investor.getHost());
    }

    public UUID getInvoiceId() {
        return invoiceId;
    }

    public double getOfferAmount() {
        return offerAmount;
    }

    public double getDiscount() {
        return discount;
    }

    public AccountInfo getSupplier() {
        return supplier;
    }

    public AccountInfo getInvestor() {
        return investor;
    }

    public Date getOfferDate() {
        return offerDate;
    }

    public Date getInvestorDate() {
        return investorDate;
    }
}
