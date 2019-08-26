package com.bfn.flows;

import co.paralleluniverse.fibers.Suspendable;
import com.bfn.flows.registration.SupplierRegisterFlow;
import net.corda.core.flows.*;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.utilities.ProgressTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// ******************
// * SupplierRegisterFlowResponder flow *
// ******************
@InitiatedBy(SupplierRegisterFlow.class)
public class SupplierRegisterFlowResponder extends FlowLogic<Void> {
    private final static Logger logger = LoggerFactory.getLogger(SupplierRegisterFlowResponder.class);

    private FlowSession counterPartySession;

    public SupplierRegisterFlowResponder(FlowSession counterPartySession) {
        this.counterPartySession = counterPartySession;
        logger.info("\uD83E\uDD6C \uD83E\uDD6C \uD83E\uDD6C SupplierRegisterFlowResponder constructor ... \uD83E\uDD6C "
                + counterPartySession.getCounterparty().getName().toString());
    }
    private static final ProgressTracker.Step RECEIVING_AND_SENDING_DATA = new ProgressTracker.Step("Sending data between parties.");
    private static final ProgressTracker.Step SIGNING = new ProgressTracker.Step("Responding to CollectSignaturesFlow.");
    private static final ProgressTracker.Step FINALISATION = new ProgressTracker.Step("Finalising a transaction.");

    private final ProgressTracker progressTracker = new ProgressTracker(
            RECEIVING_AND_SENDING_DATA,
            SIGNING,
            FINALISATION
    );
    @Suspendable
    @Override
    public Void call() throws FlowException {
        // SupplierRegisterFlowResponder flow logic goes here.
        logger.info("\uD83E\uDD6C \uD83E\uDD6C \uD83E\uDD6C SupplierRegisterFlowResponder starting ... \uD83E\uDD6C ");

        progressTracker.setCurrentStep(RECEIVING_AND_SENDING_DATA);
        Object obj = counterPartySession.receive(Object.class).unwrap(data -> data);
        logger.info(obj.toString());

        logger.info("\uD83E\uDDE9 \uD83E\uDDE9 \uD83E\uDDE9 verify Transaction from sender ...... ");
        SignedTransaction verifiedTransaction = subFlow(new ReceiveTransactionFlow(counterPartySession));
        logger.info("\uD83E\uDDE9 \uD83E\uDDE9 \uD83E\uDDE9 verifiedTransaction: " + verifiedTransaction.getId());
        logger.info("\uD83E\uDDE1 \uD83D\uDC9B \uD83D\uDC9A \uD83D\uDC99 TransactionSignature: Hopefully, the responder has written to vault. \uD83D\uDD35 \uD83D\uDD35 How to check?");
        progressTracker.setCurrentStep(FINALISATION);
        counterPartySession.send(true);
        return null;
    }
}
