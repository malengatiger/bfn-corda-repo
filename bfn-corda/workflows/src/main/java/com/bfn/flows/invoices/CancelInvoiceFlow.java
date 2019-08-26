package com.bfn.flows.invoices;

import net.corda.core.flows.FlowException;
import net.corda.core.flows.FlowLogic;
import net.corda.core.flows.InitiatingFlow;
import net.corda.core.flows.StartableByRPC;
import net.corda.core.transactions.SignedTransaction;

@InitiatingFlow
@StartableByRPC
public class CancelInvoiceFlow extends FlowLogic<SignedTransaction> {
    @Override
    public SignedTransaction call() throws FlowException {
        return null;
    }
}
