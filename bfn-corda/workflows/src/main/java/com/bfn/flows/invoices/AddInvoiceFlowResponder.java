package com.bfn.flows.invoices;

import co.paralleluniverse.fibers.Suspendable;
import com.bfn.states.InvoiceState;
import com.bfn.states.OfferState;
import net.corda.core.contracts.Amount;
import net.corda.core.flows.FlowException;
import net.corda.core.flows.FlowLogic;
import net.corda.core.flows.FlowSession;
import net.corda.core.flows.InitiatedBy;
import net.corda.core.identity.Party;
import net.corda.core.node.ServiceHub;
import net.corda.core.utilities.UntrustworthyData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

@InitiatedBy(AddInvoiceFlow.class)
public class AddInvoiceFlowResponder extends FlowLogic<Void> {
    private final static Logger logger = LoggerFactory.getLogger(AddInvoiceFlowResponder.class);
    private final FlowSession counterPartySession;

    public AddInvoiceFlowResponder(FlowSession counterPartySession) {
        this.counterPartySession = counterPartySession;
        logger.info("AddInvoiceFlowResponder Constructor fired: \uD83C\uDF45 \uD83C\uDF45 \uD83C\uDF45");
    }

    @Override
    @Suspendable
    public Void call() throws FlowException {
        logger.info("\uD83E\uDD6C \uD83E\uDD6C Responder call method at " + new Date().toString());
        final ServiceHub serviceHub = getServiceHub();
        Party myself = serviceHub.getMyInfo().getLegalIdentities().get(0);
        Party party = counterPartySession.getCounterparty();
        logger.info("\uD83C\uDF45 \uD83C\uDF45 This party: ".concat(myself.getName().toString()).concat(", party from session: \uD83C\uDF45 ".concat(party.getName().toString())));

        logger.info("\uD83C\uDF45 \uD83C\uDF45 getCounterPartyFlowInfo: " + counterPartySession.getCounterpartyFlowInfo().toString());

        UntrustworthyData<InvoiceState> packet1 = counterPartySession.receive(InvoiceState.class);
        InvoiceState invoiceState = packet1.unwrap(data -> {
            logger.info(data.toString());
            return data;
        });
        logger.info("\uD83E\uDDE9 \uD83E\uDDE9 InvoiceState received from supplier: \uD83E\uDDE9 " + invoiceState.toString());
        OfferState offerState = new OfferState(false, 0.00);
        counterPartySession.send(offerState);
        logger.info("🤟 🤟 🤟 Responded to invoice offer by sending \uD83E\uDD1F OfferState");
        return null;
    }
}
