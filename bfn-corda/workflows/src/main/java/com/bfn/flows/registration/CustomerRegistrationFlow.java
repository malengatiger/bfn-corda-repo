package com.bfn.flows.registration;

import co.paralleluniverse.fibers.Suspendable;
import com.bfn.states.CustomerState;
import com.r3.businessnetworks.membership.states.MembershipState;
import net.corda.core.flows.*;
import net.corda.core.identity.Party;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.utilities.ProgressTracker;

@InitiatingFlow
@StartableByRPC
public class CustomerRegistrationFlow extends FlowLogic<SignedTransaction> {
    private final CustomerState customerState;
    private final MembershipState membershipState;
    private final Party regulator;

    public CustomerRegistrationFlow(CustomerState customerState, MembershipState membershipState, Party regulator) {
        this.customerState = customerState;
        this.membershipState = membershipState;
        this.regulator = regulator;
    }

    /*----------------------------------
         * WIRING UP THE PROGRESS TRACKER *
        ----------------------------------*/
    // Giving our flow a progress tracker allows us to see the flow's
    // progress visually in our node's CRaSH shell.

    private static final ProgressTracker.Step ID_OTHER_NODES = new ProgressTracker.Step("Identifying other nodes on the network.");
    private static final ProgressTracker.Step SENDING_AND_RECEIVING_DATA = new ProgressTracker.Step("Sending data between parties.");
    private static final ProgressTracker.Step EXTRACTING_VAULT_STATES = new ProgressTracker.Step("Extracting states from the vault.");
    private static final ProgressTracker.Step OTHER_TX_COMPONENTS = new ProgressTracker.Step("Gathering a transaction's other components.");
    private static final ProgressTracker.Step TX_BUILDING = new ProgressTracker.Step("Building a transaction.");
    private static final ProgressTracker.Step TX_SIGNING = new ProgressTracker.Step("Signing a transaction.");
    private static final ProgressTracker.Step TX_VERIFICATION = new ProgressTracker.Step("Verifying a transaction.");
    private static final ProgressTracker.Step SIGS_GATHERING = new ProgressTracker.Step("Gathering a transaction's signatures.") {
        // Wiring up a child progress tracker allows us to see the
        // subflow's progress steps in our flow's progress tracker.
        @Override
        public ProgressTracker childProgressTracker() {
            return CollectSignaturesFlow.tracker();
        }
    };
    private static final ProgressTracker.Step VERIFYING_SIGS = new ProgressTracker.Step("Verifying a transaction's signatures.");
    private static final ProgressTracker.Step FINALISATION = new ProgressTracker.Step("Finalising a transaction.") {
        @Override
        public ProgressTracker childProgressTracker() {
            return FinalityFlow.tracker();
        }
    };

    private final ProgressTracker progressTracker = new ProgressTracker(
            ID_OTHER_NODES,
            SENDING_AND_RECEIVING_DATA,
            EXTRACTING_VAULT_STATES,
            OTHER_TX_COMPONENTS,
            TX_BUILDING,
            TX_SIGNING,
            TX_VERIFICATION,
            SIGS_GATHERING,
            FINALISATION
    );


    @Suspendable
    @Override
    public SignedTransaction call() throws FlowException {
        return null;
    }
}
