package com.bfn.flows.invoices;

import co.paralleluniverse.fibers.Suspendable;
import com.bfn.contracts.InvoiceContract;
import com.bfn.states.InvoiceState;
import com.bfn.states.OfferState;
import com.google.common.collect.ImmutableList;
import com.r3.businessnetworks.membership.flows.ConfigUtils;
import com.r3.businessnetworks.membership.flows.member.RequestMembershipFlow;
import com.r3.businessnetworks.membership.states.SimpleMembershipMetadata;
import net.corda.core.contracts.Command;
import net.corda.core.flows.*;
import net.corda.core.identity.CordaX500Name;
import net.corda.core.identity.Party;
import net.corda.core.node.ServiceHub;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;
import net.corda.core.utilities.UntrustworthyData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@InitiatingFlow
@StartableByRPC
public class AddInvoiceFlow extends FlowLogic<SignedTransaction> {
    private final static Logger logger = LoggerFactory.getLogger(AddInvoiceFlow.class);

    final InvoiceState invoiceState;


    public AddInvoiceFlow(InvoiceState invoiceState) {
        this.invoiceState = invoiceState;
        logger.info(" \uD83C\uDFC0  \uD83C\uDFC0  \uD83C\uDFC0 AddInvoiceFlow constructor - invoiceState: "
                .concat(invoiceState.getInvoiceNumber()
                .concat(" ") + invoiceState.getTotalAmount()));
    }

    @Override
    @Suspendable
    public SignedTransaction call() throws FlowException {
        // We retrieve the notary identity from the network map.
        final ServiceHub serviceHub = getServiceHub();
        logger.info(" \uD83E\uDD1F \uD83E\uDD1F  \uD83E\uDD1F \uD83E\uDD1F  ... AddInvoiceFlow call started ...");
        Party notary = serviceHub.getNetworkMapCache().getNotaryIdentities().get(0);
        //Party owner = getOurIdentity();
        CordaX500Name name = new CordaX500Name("London","London","GB");
        Party counterParty = serviceHub.getNetworkMapCache().getNodeByLegalName(name).getLegalIdentities().get(0);
        CordaX500Name name2 = new CordaX500Name("Sandton","Sandton","ZA");
        Party owner = serviceHub.getNetworkMapCache().getNodeByLegalName(name2).getLegalIdentities().get(0);
        invoiceState.setParty(owner);
        invoiceState.setDateRegistered(new Date());
        InvoiceContract.Register command = new InvoiceContract.Register();
        logger.info(" \uD83D\uDD06 \uD83D\uDD06 \uD83D\uDD06 Notary: " + notary.getName().toString()
                + "  \uD83C\uDF4A invoice party: " + owner.getName().toString() + " counterParty: " + counterParty.getName().toString() + "  \uD83C\uDF4A invoice: "
                + invoiceState.getInvoiceNumber().concat("  \uD83D\uDC9A totalAmount") + invoiceState.getTotalAmount());

        // We create a transaction builder and add the components.
        TransactionBuilder txBuilder = new TransactionBuilder(notary)
                .addOutputState(invoiceState, InvoiceContract.ID)
                .addCommand(command, owner.getOwningKey());

        txBuilder.verify(serviceHub);
        logger.info(" \uD83D\uDD06 \uD83D\uDD06 \uD83D\uDD06 Invoice Register Transaction verified");
        // Signing the transaction.
        SignedTransaction signedTx = serviceHub.signInitialTransaction(txBuilder);
        logger.info(" \uD83D\uDD06 \uD83D\uDD06 \uD83D\uDD06 Invoice Register Transaction signed");
        FlowSession flowSession = initiateFlow(owner);
        FlowSession counterPartyFlowSession = initiateFlow(counterParty);
        counterPartyFlowSession.send(invoiceState);
        UntrustworthyData<OfferState> packet1 = counterPartyFlowSession.receive(OfferState.class);
        OfferState offerState = packet1.unwrap(data -> {
            logger.info(data.toString());
            return data;
        });
        if (offerState != null) {
            logger.info("\uD83E\uDD1F \uD83E\uDD1F \uD83E\uDD1F Received OfferState from counterParty \uD83C\uDF45 \uD83C\uDF45 " + offerState.toString());
        }

        return null;
    }
}
