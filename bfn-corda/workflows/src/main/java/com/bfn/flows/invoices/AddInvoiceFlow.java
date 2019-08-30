package com.bfn.flows.invoices;

import co.paralleluniverse.fibers.Suspendable;
import com.bfn.contracts.InvoiceContract;
import com.bfn.states.InvoiceState;
import com.google.common.collect.ImmutableList;
import net.corda.core.flows.*;
import net.corda.core.identity.CordaX500Name;
import net.corda.core.identity.Party;
import net.corda.core.node.ServiceHub;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;
import net.corda.core.utilities.ProgressTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

@InitiatingFlow
@StartableByRPC
public class AddInvoiceFlow extends FlowLogic<SignedTransaction> {
    private final static Logger logger = LoggerFactory.getLogger(AddInvoiceFlow.class);

    final String purchaseOrder;
    final String invoiceId;
    final String customer, supplier, investor;
    final String wallet;
    final String user;
    final String invoiceNumber;
    final String description;
    final String reference;
    final double amount, valueAddedTax, totalAmount;
    private final ProgressTracker.Step SENDING_TRANSACTION = new ProgressTracker.Step("Sending transaction to counterParty");
    private final ProgressTracker.Step GENERATING_TRANSACTION = new ProgressTracker.Step("Generating transaction based on new IOU.");
    private final ProgressTracker.Step VERIFYING_TRANSACTION = new ProgressTracker.Step("Verifying contract constraints.");
    private final ProgressTracker.Step SIGNING_TRANSACTION = new ProgressTracker.Step("Signing transaction with our private key.");
    private final ProgressTracker.Step GATHERING_SIGS = new ProgressTracker.Step("Gathering the counterparty's signature.") {
        @Override
        public ProgressTracker childProgressTracker() {
            logger.info("\uD83C\uDF3A \uD83C\uDF3A ProgressTracker childProgressTracker ...");
            return CollectSignaturesFlow.Companion.tracker();
        }
    };
    private final ProgressTracker.Step FINALISING_TRANSACTION = new ProgressTracker.Step("Obtaining notary signature and recording transaction.") {
        @Override
        public ProgressTracker childProgressTracker() {
            return FinalityFlow.Companion.tracker();
        }
    };

    // The progress tracker checkpoints each stage of the flow and outputs the specified messages when each
    // checkpoint is reached in the code. See the 'progressTracker.currentStep' expressions within the call()
    // function.
    private final ProgressTracker progressTracker = new ProgressTracker(
            GENERATING_TRANSACTION,
            VERIFYING_TRANSACTION,
            SIGNING_TRANSACTION,
            GATHERING_SIGS,
            FINALISING_TRANSACTION,
            SENDING_TRANSACTION
    );

    @Override
    public ProgressTracker getProgressTracker() {
        return progressTracker;
    }

    public AddInvoiceFlow(String purchaseOrder, String invoiceId, String customer, String supplier, String investor, String wallet,
                          String user, String invoiceNumber, String description, String reference,
                          double amount, double valueAddedTax, double totalAmount) {
        this.purchaseOrder = purchaseOrder;
        this.invoiceId = invoiceId;
        this.customer = customer;
        this.supplier = supplier;
        this.investor = investor;
        this.wallet = wallet;
        this.user = user;
        this.invoiceNumber = invoiceNumber;
        this.description = description;
        this.reference = reference;
        this.totalAmount = totalAmount;
        this.valueAddedTax = valueAddedTax;
        this.amount = amount;
    }

    @Override
    @Suspendable
    public SignedTransaction call() throws FlowException {
        // We retrieve the notary identity from the network map.
        final ServiceHub serviceHub = getServiceHub();
        logger.info(" \uD83E\uDD1F \uD83E\uDD1F  \uD83E\uDD1F \uD83E\uDD1F  ... AddInvoiceFlow call started ...");
        Party notary = serviceHub.getNetworkMapCache().getNotaryIdentities().get(0);
        //Party supplierParty = getOurIdentity();
        String[] invList = investor.split("@");
        String[] custList = customer.split("@");
        String[] suppList = supplier.split("@");
        CordaX500Name name = new CordaX500Name(invList[0],invList[1],invList[2]);
        Party investorParty = serviceHub.getNetworkMapCache().getNodeByLegalName(name).getLegalIdentities().get(0);
        CordaX500Name name2 = new CordaX500Name(suppList[0],suppList[1],suppList[2]);
        Party supplierParty = serviceHub.getNetworkMapCache().getNodeByLegalName(name2).getLegalIdentities().get(0);
        CordaX500Name name3 = new CordaX500Name(custList[0],custList[1],custList[2]);
        Party customerParty = serviceHub.getNetworkMapCache().getNodeByLegalName(name3).getLegalIdentities().get(0);

        InvoiceState invoiceState = new InvoiceState(purchaseOrder,invoiceId,wallet,
                user,invoiceNumber,description,reference,
                amount,totalAmount, valueAddedTax,new Date(),supplierParty,investorParty, customerParty);
        invoiceState.setDateRegistered(new Date());

        InvoiceContract.Register command = new InvoiceContract.Register();
        logger.info(" \uD83D\uDD06 \uD83D\uDD06 \uD83D\uDD06 Notary: " + notary.getName().toString()
                + "  \uD83C\uDF4A supplierParty: " + supplierParty.getName().toString() + " \uD83E\uDDE9 investorParty: " + investorParty.getName().toString()
                + "  \uD83C\uDF4A customerParty: "+customerParty.getName().toString() +" \uD83C\uDF4E  invoice: "
                + invoiceState.getInvoiceNumber().concat("  \uD83D\uDC9A totalAmount") + invoiceState.getTotalAmount());

        progressTracker.setCurrentStep(GENERATING_TRANSACTION);
        TransactionBuilder txBuilder = new TransactionBuilder(notary)
                .addOutputState(invoiceState, InvoiceContract.ID)
                .addCommand(command, supplierParty.getOwningKey(), investorParty.getOwningKey(), customerParty.getOwningKey());

        progressTracker.setCurrentStep(VERIFYING_TRANSACTION);
        txBuilder.verify(serviceHub);
        logger.info(" \uD83D\uDD06 \uD83D\uDD06 \uD83D\uDD06 Invoice Register TransactionBuilder verified");
        // Signing the transaction.
        progressTracker.setCurrentStep(SIGNING_TRANSACTION);
        SignedTransaction signedTx = serviceHub.signInitialTransaction(txBuilder);
        logger.info(" \uD83D\uDD06 \uD83D\uDD06 \uD83D\uDD06 Invoice Register Transaction signInitialTransaction executed ...");
        logger.info(" \uD83D\uDD06 \uD83D\uDD06 \uD83D\uDD06 Transaction signInitialTransaction: ".concat(signedTx.toString()));

        FlowSession investorFlowSession = initiateFlow(investorParty);
        FlowSession customerFlowSession = initiateFlow(customerParty);

        progressTracker.setCurrentStep(GATHERING_SIGS);
        logger.info(" \uD83D\uDD06 \uD83D\uDD06 \uD83D\uDD06 \uD83D\uDD06 Collecting Signatures ....");
        SignedTransaction signedTransaction = subFlow(new CollectSignaturesFlow(signedTx, ImmutableList.of(investorFlowSession, customerFlowSession), GATHERING_SIGS.childProgressTracker()));
        logger.info("\uD83C\uDFBD \uD83C\uDFBD \uD83C\uDFBD \uD83C\uDFBD  Signatures collected OK!  \uD83D\uDE21 \uD83D\uDE21 will call FinalityFlow ... \uD83C\uDF3A \uD83C\uDF3A  \uD83C\uDF3A \uD83C\uDF3A : ".concat(signedTransaction.toString()));

        SignedTransaction mSignedTransactionDone = subFlow(new FinalityFlow(signedTransaction, ImmutableList.of(investorFlowSession, customerFlowSession), FINALISING_TRANSACTION.childProgressTracker()));
        logger.info(" \uD83D\uDD06 \uD83D\uDD06 \uD83D\uDD06 \uD83D\uDD06 FinalityFlow has been executed ... \uD83E\uDD66  are we good? \uD83E\uDD66 ❄️ ❄️ ❄️");
        logger.info(" \uD83D\uDD06 \uD83D\uDD06 \uD83D\uDD06 \uD83D\uDD06 returning mSignedTransactionDone:  ❄️ ❄️ : ".concat(mSignedTransactionDone.toString()));
        return mSignedTransactionDone;
    }
}
