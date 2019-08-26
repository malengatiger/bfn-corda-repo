package com.bfn.flows.registration;

import co.paralleluniverse.fibers.Suspendable;
import com.google.common.collect.ImmutableList;
import com.r3.businessnetworks.membership.states.MembershipContract;
import com.r3.businessnetworks.membership.states.MembershipState;
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
// * RequestRegistrationFlow flow *
// ******************
@InitiatingFlow
@StartableByRPC
public class RequestRegistrationFlow extends FlowLogic<SignedTransaction> {
    private final static Logger logger = LoggerFactory.getLogger(RequestRegistrationFlow.class);

    private MembershipState membershipState;
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

    @Override
    public ProgressTracker getProgressTracker() {
        return progressTracker;
    }

    public RequestRegistrationFlow( MembershipState membershipState) {
        this.membershipState = membershipState;

        logger.info("\n\n\uD83C\uDFC8 \uD83C\uDFC8 RequestRegistrationFlow constructor  \uD83E\uDD66  \uD83E\uDD66  \uD83E\uDD66");
        logger.info("\n\n\uD83C\uDFC8 \uD83C\uDFC8 RequestRegistrationFlow supplier: "+ membershipState.getMember().getName().toString() + " \uD83C\uDFC8 \uD83C\uDFC8");
    }

    @Suspendable
    @Override
    public SignedTransaction call() throws FlowException {
        // RequestRegistrationFlow flow logic goes here.
        logger.info("\n\n\uD83C\uDFC8 \uD83C\uDFC8 " + "  \uD83E\uDD8B RequestRegistrationFlow: call starting .... \uD83E\uDD8B");
        serviceHub = getServiceHub();
        NodeInfo info = serviceHub.getMyInfo();
        logger.info("\uD83C\uDFC8 \uD83C\uDFC8 NodeInfo: \uD83C\uDFC8 ".concat(info.toString()));
        Party notary = serviceHub.getNetworkMapCache().getNotaryIdentities().get(0);
        Party party = getOurIdentity();
        logger.info("\uD83C\uDFC8 \uD83C\uDFC8 Party requesting registration: \uD83D\uDC99 " + party.toString());
        logger.info("\uD83C\uDFC8 \uD83C\uDFC8 Notary involved: \uD83D\uDC99 " + notary.toString());

        List<PublicKey> requiredSigners = ImmutableList.of(party.getOwningKey());
        TransactionBuilder transactionBuilder = new TransactionBuilder(notary);
        transactionBuilder
                .addOutputState(membershipState, MembershipContract.class.getName())
                .addCommand(new MembershipContract.Commands.Request(), requiredSigners);

        // Stage 2.
        progressTracker.setCurrentStep(VERIFYING_TRANSACTION);
        // Verify that the transaction is valid.
        transactionBuilder.verify(getServiceHub());
        logger.info("\uD83D\uDC9A\uD83D\uDC9A\uD83D\uDC9A Transaction verified with " + transactionBuilder.outputStates().size() + " output states");

        // Stage 3.
        progressTracker.setCurrentStep(SIGNING_TRANSACTION);
        // Sign the transaction.
        final SignedTransaction partSignedTx = serviceHub.signInitialTransaction(transactionBuilder);
        logger.info("\uD83D\uDD34 \uD83D\uDD34 SignedTransaction created after serviceHub signInitialTransaction call  \uD83E\uDDE1  \uD83E\uDDE1  \uD83E\uDDE1");
        // Stage 4.
        // progressTracker.setCurrentStep(GATHERING_SIGS);
        // Send the state to the counterparty, and receive it back with their signature.


        // Stage 5.
        progressTracker.setCurrentStep(FINALISING_TRANSACTION);
        // Notarise and record the transaction in both parties' vaults.
        logger.info("\uD83C\uDF00  ☘️☘️ Notarise and record the transaction in ☘️☘️ parties' vaults. \uD83D\uDD96 \uD83D\uDD96 \uD83D\uDD96 Fingers crossed !!! \uD83D\uDD96");

        return subFlow(new FinalityFlow(partSignedTx, progressTracker));

    }

}
