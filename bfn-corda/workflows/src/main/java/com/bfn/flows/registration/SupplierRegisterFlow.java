package com.bfn.flows.registration;

import co.paralleluniverse.fibers.Suspendable;
import com.bfn.contracts.SupplierContract;
import com.bfn.states.SupplierState;
import com.google.common.collect.ImmutableList;
import net.corda.core.flows.*;
import net.corda.core.identity.Party;
import net.corda.core.node.NodeInfo;
import net.corda.core.node.ServiceHub;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;
import net.corda.core.utilities.ProgressTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.PublicKey;
import java.util.List;

// ******************
// * SupplierRegisterFlow flow *
// ******************
@InitiatingFlow
@StartableByRPC
public class SupplierRegisterFlow extends FlowLogic<SignedTransaction> {
    private final static Logger logger = LoggerFactory.getLogger(SupplierRegisterFlow.class);

    private SupplierState supplierState;
    private final ProgressTracker.Step GENERATING_TRANSACTION = new ProgressTracker.Step("Generating transaction based on new IOU.");
    private final ProgressTracker.Step VERIFYING_TRANSACTION = new ProgressTracker.Step("Verifying contract constraints.");
    private final ProgressTracker.Step SIGNING_TRANSACTION = new ProgressTracker.Step("Signing transaction with our private key.");
    private final ProgressTracker.Step GATHERING_SIGS = new ProgressTracker.Step("Gathering the counterparty's signature.") {
        @Override
        public ProgressTracker childProgressTracker() {
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
            FINALISING_TRANSACTION
    );
    ServiceHub serviceHub;
    Party counterParty;

    @Override
    public ProgressTracker getProgressTracker() {
        return progressTracker;
    }

    public SupplierRegisterFlow(SupplierState supplierState, Party counterParty) {
        this.supplierState = supplierState;
        this.counterParty = counterParty;
        logger.info("\n\n\uD83C\uDFC8 \uD83C\uDFC8 SupplierRegisterFlow constructor  \uD83E\uDD66  \uD83E\uDD66  \uD83E\uDD66");
        logger.info("\n\n\uD83C\uDFC8 \uD83C\uDFC8 SupplierRegisterFlow supplier: "+supplierState.getName() + " \uD83C\uDFC8 \uD83C\uDFC8");
        logger.info("\n\n\uD83C\uDFC8 \uD83C\uDFC8 SupplierRegisterFlow counterParty: "+counterParty.getName().toString() + " \uD83C\uDFC8 \uD83C\uDFC8");
    }

    @Suspendable
    @Override
    public SignedTransaction call() throws FlowException {
        // SupplierRegisterFlow flow logic goes here.
        logger.info("\n\n\uD83C\uDFC8 \uD83C\uDFC8 " + "  \uD83E\uDD8B SupplierRegisterFlow: call starting .... \uD83E\uDD8B");
        serviceHub = getServiceHub();
        NodeInfo info = serviceHub.getMyInfo();
        logger.info("\uD83C\uDFC8 \uD83C\uDFC8 NodeInfo: \uD83C\uDFC8 ".concat(info.toString()));
        Party notary = serviceHub.getNetworkMapCache().getNotaryIdentities().get(0);
        Party party = getOurIdentity();
        logger.info("\uD83C\uDFC8 \uD83C\uDFC8 Supplier Party involved: \uD83D\uDC99 " + party.toString());
        logger.info("\uD83C\uDFC8 \uD83C\uDFC8 Notary involved: \uD83D\uDC99 " + notary.toString());

        List<PublicKey> requiredSigners = ImmutableList.of(getOurIdentity().getOwningKey(), counterParty.getOwningKey());
        TransactionBuilder tb = new TransactionBuilder(notary);
        tb
                .addOutputState(supplierState, SupplierContract.ID)
                .addCommand(new SupplierContract.Register(), requiredSigners);

        // Stage 2.
        progressTracker.setCurrentStep(VERIFYING_TRANSACTION);
        // Verify that the transaction is valid.
        tb.verify(getServiceHub());
        logger.info("\uD83D\uDC9A\uD83D\uDC9A\uD83D\uDC9A Transaction verified with " + tb.outputStates().size() + " output states");

        // Stage 3.
        progressTracker.setCurrentStep(SIGNING_TRANSACTION);
        // Sign the transaction.
        final SignedTransaction partSignedTx = serviceHub.signInitialTransaction(tb);
        logger.info("\uD83D\uDD34 \uD83D\uDD34 SignedTransaction created after serviceHub signInitialTransaction call  \uD83E\uDDE1  \uD83E\uDDE1  \uD83E\uDDE1");
        // Stage 4.
        progressTracker.setCurrentStep(GATHERING_SIGS);
        // Send the state to the counterparty, and receive it back with their signature.

        FlowSession flowSession = initiateFlow(counterParty);
        logger.info("\uD83D\uDD34 \uD83D\uDD34 Flow Session: counterParty:" + flowSession.getCounterparty().getName().toString());
        List<FlowSession> flowSessions = ImmutableList.of(flowSession);
        final SignedTransaction fullySignedTx = subFlow(
                new CollectSignaturesFlow(partSignedTx, flowSessions, CollectSignaturesFlow.Companion.tracker()));
        logger.info("\uD83E\uDDE9 \uD83E\uDDE9 \uD83E\uDDE9 Transaction fully signed: \uD83D\uDD34 " + fullySignedTx.getId().toString() + " ... about to finalize ...  \uD83D\uDD34");

        // Stage 5.
        progressTracker.setCurrentStep(FINALISING_TRANSACTION);
        // Notarise and record the transaction in both parties' vaults.
        logger.info("\uD83C\uDF00  ☘️☘️ Notarise and record the transaction in ALL ☘️☘️ parties' vaults. \uD83D\uDD96 \uD83D\uDD96 \uD83D\uDD96 Fingers crossed !!! \uD83D\uDD96");

        return subFlow(new FinalityFlow(partSignedTx, flowSessions, progressTracker));

    }
//    private SignedTransaction processMultiple() {
//        List<NodeInfo> nodes = serviceHub.getNetworkMapCache().getAllNodes();
//        List<FlowSession> sessions = new ArrayList<>();
//        List<SignedTransaction> signedTransactions = new ArrayList<>();
//        try {
//            for (NodeInfo nodeInfo : nodes) {
//                List<Party> parties = nodeInfo.getLegalIdentities();
//                logger.info("\uD83C\uDF00 This node: \uD83C\uDF38 " + nodeInfo.getLegalIdentities().get(0).getName().toString()
//                        + " \uD83D\uDC7D \uD83D\uDC7D " + parties + " \uD83D\uDC7D \uD83D\uDC7D parties that are legal identities");
//                for (Party mParty : parties) {
//                    if (!mParty.getName().toString().contains("Notary")) {
//                        if (mParty.getOwningKey() != party.getOwningKey()) {
//                            FlowSession otherPartySession = initiateFlow(mParty);
//                            sessions.add(otherPartySession);
//                            logger.info("\uD83C\uDF4E \uD83C\uDF4E Flow session with \uD83C\uDF4F " + mParty.getName().toString() + " added to list \uD83C\uDF4F");
//                            SignedTransaction fullySignedTx = subFlow(new CollectSignaturesFlow(partSignedTx, ImmutableList.of(otherPartySession)));
//                            signedTransactions.add(fullySignedTx);
//                            logger.info("\uD83E\uDDE9 \uD83E\uDDE9 \uD83E\uDDE9 Transaction fully signed: \uD83D\uDD34 " + fullySignedTx.getId().toString() + "  \uD83D\uDD34");
//                            subFlow(new FinalityFlow(fullySignedTx, ImmutableList.of(otherPartySession)));
//                        } else {
//                            logger.info("\uD83C\uDF00 This party is myself. No need to create flow session");
//                        }
//                    } else {
//                        logger.info("\uD83C\uDF00 This party is a Notary. No need to create flow session");
//                    }
//                }
//            }
//        } catch (Exception e) {
//            logger.info(" \uD83D\uDE21 \uD83D\uDE21 \uD83D\uDE21 \uD83D\uDE21 \uD83D\uDE21 \uD83D\uDE21 \uD83D\uDE21 \uD83D\uDE21 \uD83D\uDE21 ERROR! ");
//            logger.error(e.getMessage());
//            throw new IllegalArgumentException(e.getMessage());
//        }
//        logger.info("☘️☘️☘️ Created flow sessions: \uD83D\uDD35 " + sessions.size() + " \uD83D\uDD35 signedTransactions: " + signedTransactions.size());
//        int cnt = 0;
//        for (FlowSession flowSession: sessions) {
//            cnt++;
//            logger.info("\uD83E\uDD6C \uD83E\uDD6C FlowSession #"+cnt+": counterParty \uD83E\uDD6C " + flowSession.getCounterparty().getName().toString());
//        }
//        cnt = 0;
//        for (SignedTransaction tx: signedTransactions) {
//            cnt++;
//            logger.info("\uD83E\uDD6C \uD83E\uDD6C SignedTransaction  #" + cnt + " \uD83E\uDD6C  transactionId: " + tx.getId());
//        }
//    }
}
