package com.bfn.flows;

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
import java.util.ArrayList;
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
    @Override
    public ProgressTracker getProgressTracker() {
        return progressTracker;
    }

    public SupplierRegisterFlow(SupplierState supplierState) {
        this.supplierState = supplierState;
    }

    @Suspendable
    @Override
    public SignedTransaction call() throws FlowException {
        // SupplierRegisterFlow flow logic goes here.

        ServiceHub serviceHub = getServiceHub();
        NodeInfo info = serviceHub.getMyInfo();
        logger.info("\uD83C\uDFC8 \uD83C\uDFC8 NodeInfo: \uD83C\uDFC8 ".concat(info.toString()));
        Party notary = serviceHub.getNetworkMapCache().getNotaryIdentities().get(0);
        Party party = getOurIdentity();
        logger.info("\uD83C\uDFC8 \uD83C\uDFC8 Supplier Party involved: \uD83D\uDC99 " + party.toString());
        logger.info("\uD83C\uDFC8 \uD83C\uDFC8 Notary involved: \uD83D\uDC99 " + notary.toString());

        List<PublicKey> requiredSigners = ImmutableList.of(getOurIdentity().getOwningKey());
        TransactionBuilder tb = new TransactionBuilder();
        tb
                .addOutputState(supplierState, SupplierContract.ID)
                .addCommand(new SupplierContract.Register(), requiredSigners)
                .setNotary(notary);

        // Stage 2.
        progressTracker.setCurrentStep(VERIFYING_TRANSACTION);
        // Verify that the transaction is valid.
        tb.verify(getServiceHub());
        logger.info("\uD83D\uDC9A\uD83D\uDC9A\uD83D\uDC9A Transaction verified");

        // Stage 3.
        progressTracker.setCurrentStep(SIGNING_TRANSACTION);
        // Sign the transaction.
        final SignedTransaction partSignedTx = getServiceHub().signInitialTransaction(tb);
        logger.info("Transaction signed  \uD83E\uDDE1  \uD83E\uDDE1  \uD83E\uDDE1");
        // Stage 4.
        progressTracker.setCurrentStep(GATHERING_SIGS);
        // Send the state to the counterparty, and receive it back with their signature.
        List<NodeInfo> nodes = serviceHub.getNetworkMapCache().getAllNodes();
        List<FlowSession> sessions = new ArrayList<>();
        for (NodeInfo nodeInfo: nodes) {
            List<Party> parties = nodeInfo.getLegalIdentities();
            for (Party mParty: parties) {
                if (mParty.getOwningKey() != party.getOwningKey()) {
                    FlowSession otherPartySession = initiateFlow(mParty);
                    sessions.add(otherPartySession);
                    logger.info("\uD83C\uDF4E \uD83C\uDF4E Flow session with \uD83C\uDF4F " + mParty.getName().toString() + " added \uD83C\uDF4F");
                } else {
                    logger.info("\uD83C\uDF00 This party is myself. No need to create flow session");
                }
            }
        }
        logger.info("☘️☘️☘️ Created flow sessions: \uD83D\uDD35 " + sessions.size() + " \uD83D\uDD35 ");
        final SignedTransaction fullySignedTx = subFlow(
                new CollectSignaturesFlow(partSignedTx, sessions, CollectSignaturesFlow.Companion.tracker()));
        logger.info("\uD83E\uDDE9 \uD83E\uDDE9 \uD83E\uDDE9 Transaction fully signed: \uD83D\uDD34 " + fullySignedTx.getId().toString() + "  \uD83D\uDD34");
        // Stage 5.
        progressTracker.setCurrentStep(FINALISING_TRANSACTION);
        // Notarise and record the transaction in both parties' vaults.
        return subFlow(new FinalityFlow(fullySignedTx, sessions));

    }
}
