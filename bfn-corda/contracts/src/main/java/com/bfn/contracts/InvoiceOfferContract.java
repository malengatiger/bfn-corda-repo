package com.bfn.contracts;

import com.bfn.states.InvoiceOfferState;
import com.bfn.states.InvoiceState;
import net.corda.core.contracts.Command;
import net.corda.core.contracts.CommandData;
import net.corda.core.contracts.Contract;
import net.corda.core.contracts.ContractState;
import net.corda.core.identity.Party;
import net.corda.core.transactions.LedgerTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.PublicKey;
import java.util.List;

// ************
// * Contract *
// ************

public class InvoiceOfferContract implements Contract {
    // This is used to identify our contract when building a transaction.
    public static final String ID = InvoiceOfferContract.class.getName();
    private final static Logger logger = LoggerFactory.getLogger(InvoiceOfferContract.class);

    @Override
    public void verify(LedgerTransaction tx) throws IllegalArgumentException{

        logger.info(" \uD83D\uDD06 \uD83D\uDD06 \uD83D\uDD06 InvoiceOfferContract: verify starting ..... \uD83E\uDD6C \uD83E\uDD6C ");
        if (tx.getInputStates().size() != 1) {
            throw new IllegalArgumentException("Input state is required");
        }
        if (tx.getOutputStates().size() != 1) {
            throw new IllegalArgumentException("One output InvoiceOfferState is required");
        }
        if (tx.getCommands().size() != 1) {
            throw new IllegalArgumentException("Only one command allowed");
        }
        Command command = tx.getCommand(0);
        if (!(command.getValue() instanceof Register)) {
            throw new IllegalArgumentException("Only Register command allowed");
        }
        List<PublicKey> requiredSigners = command.getSigners();
        logger.info(" \uD83D\uDD34  \uD83D\uDD34 Required signers: " + requiredSigners.size());
        for (PublicKey key: requiredSigners) {
            logger.info(" \uD83D\uDD34 publicKey: ".concat(key.toString()));
        }
        ContractState contractState = tx.getOutput(0);
        if (!(contractState instanceof InvoiceOfferState)) {
            throw new IllegalArgumentException("Output state must be InvoiceOfferState");
        }
        InvoiceOfferState invoiceState = (InvoiceOfferState)contractState;
        if (invoiceState.getSupplier() == null) {
            throw new IllegalArgumentException("Supplier is required");
        }
//        if (invoiceState.getin() == null) {
//            throw new IllegalArgumentException("Customer is required");
//        }
        Party party = invoiceState.getSupplier().getHost();
        PublicKey supplierPublicKey = party.getOwningKey();
        if (!requiredSigners.contains(supplierPublicKey)) {
            throw new IllegalArgumentException("Supplier Party must sign");
        }
//        Party party2 = invoiceState.getCustomerInfo().getHost();
//        PublicKey customerPublicKey = party2.getOwningKey();
//        if (!requiredSigners.contains(customerPublicKey)) {
//            throw new IllegalArgumentException("Customer Party must sign");
//        }
        logger.info(" \uD83D\uDD06 \uD83D\uDD06 \uD83D\uDD06 InvoiceOfferContract: verification done OK! .....\uD83E\uDD1F \uD83E\uDD1F ");

    }
//
//    // Used to indicate the transaction's intent.
//    public interface Commands extends CommandData {
//        class Action implements Commands {}
//    }

    public static class Register implements CommandData {}
    public static class MakeOffer implements CommandData {}
}
