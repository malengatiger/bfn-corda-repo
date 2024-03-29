package com.bfn.flows.invoices;

import co.paralleluniverse.fibers.Suspendable;
import net.corda.core.flows.*;
import net.corda.core.identity.Party;
import net.corda.core.node.ServiceHub;
import net.corda.core.transactions.SignedTransaction;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

@InitiatedBy(InvoiceRegistrationFlow.class)
public class InvoiceRegistrationFlowResponder extends FlowLogic<SignedTransaction> {
    private final static Logger logger = LoggerFactory.getLogger(InvoiceRegistrationFlowResponder.class);
    private final FlowSession counterPartySession;

    public InvoiceRegistrationFlowResponder(FlowSession counterPartySession) {
        this.counterPartySession = counterPartySession;
        logger.info("AddInvoiceFlowResponder Constructor fired: \uD83C\uDF45 \uD83C\uDF45 \uD83C\uDF45");
    }

    @Override
    @Suspendable
    public SignedTransaction call() throws FlowException {
        logger.info("\uD83E\uDD6C \uD83E\uDD6C Responder call method at " + new Date().toString());
        final ServiceHub serviceHub = getServiceHub();
        Party myself = serviceHub.getMyInfo().getLegalIdentities().get(0);
        Party party = counterPartySession.getCounterparty();
        logger.info("\uD83C\uDF45 \uD83C\uDF45 This party: ".concat(myself.getName().toString())
                .concat(", party from session: \uD83C\uDF45 ".concat(party.getName().toString())));
        logger.info("\uD83C\uDF45 \uD83C\uDF45 getCounterPartyFlowInfo: " +
                counterPartySession.getCounterpartyFlowInfo().toString());

        SignTransactionFlow signTransactionFlow = new SignTransactionFlow(counterPartySession) {
            @Override
            protected void checkTransaction(@NotNull SignedTransaction stx) throws FlowException {
            }
        };
        logger.info("\uD83D\uDD06 \uD83D\uDD06 \uD83D\uDD06 run subFlow SignTransactionFlow ...");
        subFlow(signTransactionFlow);
        SignedTransaction signedTransaction = subFlow(new ReceiveFinalityFlow(counterPartySession));
        logger.info("\uD83E\uDDE1 \uD83D\uDC9B \uD83D\uDC9A \uD83D\uDC99 \uD83D\uDC9C ReceiveFinalityFlow executed \uD83E\uDD1F");
        logger.info("returning signedTransaction \uD83E\uDD1F \uD83C\uDF4F \uD83C\uDF4E ".concat(signedTransaction.toString()));
        return signedTransaction;

    }
}
