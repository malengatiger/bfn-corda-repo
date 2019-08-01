package com.bfn.flows;

import co.paralleluniverse.fibers.Suspendable;
import net.corda.core.flows.FlowException;
import net.corda.core.flows.FlowLogic;
import net.corda.core.flows.FlowSession;
import net.corda.core.flows.InitiatedBy;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.utilities.UntrustworthyData;
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
    }

    @Suspendable
    @Override
    public Void call() throws FlowException {
        // SupplierRegisterFlowResponder flow logic goes here.
        logger.info("\uD83E\uDD6C \uD83E\uDD6C \uD83E\uDD6C SupplierRegisterFlowResponder starting ... \uD83E\uDD6C ");
        SignedTransaction data = counterPartySession.receive(SignedTransaction.class).unwrap(it -> {

            return it;
        });

        FlowSession session = initiateFlow(counterPartySession.getCounterparty());
        session.send("OK");
        return null;
    }
}
