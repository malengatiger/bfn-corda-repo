package com.bfn.states;

import com.bfn.contracts.InvoiceContract;
import com.bfn.contracts.SupplierContract;
import com.bfn.schemas.SupplierSchemaV1;
import com.google.common.collect.ImmutableList;
import net.corda.core.contracts.BelongsToContract;
import net.corda.core.contracts.ContractState;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.Party;
import net.corda.core.schemas.MappedSchema;
import net.corda.core.schemas.PersistentState;
import net.corda.core.schemas.QueryableState;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

// *********
// * State *
// *********
@BelongsToContract(InvoiceContract.class)
public class InvoiceState implements ContractState, QueryableState {

    String supplier,
            purchaseOrder,
            invoiceId,
            deliveryNote,
            company,
            customer,
            wallet,
            user,
            invoiceNumber,
            description,
            reference;

    boolean isOnOffer, isSettled;
    double amount, totalAmount, valueAddedTax;
    private Date dateRegistered;
    private Party party;

    public InvoiceState(String supplier, String purchaseOrder, String invoiceId, String deliveryNote, String company, String customer, String wallet, String user, String invoiceNumber, String description, String reference, boolean isOnOffer, boolean isSettled, double amount, double totalAmount, double valueAddedTax, Date dateRegistered, Party party) {
        this.supplier = supplier;
        this.purchaseOrder = purchaseOrder;
        this.invoiceId = invoiceId;
        this.deliveryNote = deliveryNote;
        this.company = company;
        this.customer = customer;
        this.wallet = wallet;
        this.user = user;
        this.invoiceNumber = invoiceNumber;
        this.description = description;
        this.reference = reference;
        this.isOnOffer = isOnOffer;
        this.isSettled = isSettled;
        this.amount = amount;
        this.totalAmount = totalAmount;
        this.valueAddedTax = valueAddedTax;
        this.dateRegistered = dateRegistered;
        this.party = party;
    }

    public String getSupplier() {
        return supplier;
    }

    public String getPurchaseOrder() {
        return purchaseOrder;
    }

    public String getInvoiceId() {
        return invoiceId;
    }

    public String getDeliveryNote() {
        return deliveryNote;
    }

    public String getCompany() {
        return company;
    }

    public String getCustomer() {
        return customer;
    }

    public String getWallet() {
        return wallet;
    }

    public String getUser() {
        return user;
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public String getDescription() {
        return description;
    }

    public String getReference() {
        return reference;
    }

    public boolean isOnOffer() {
        return isOnOffer;
    }

    public boolean isSettled() {
        return isSettled;
    }

    public double getAmount() {
        return amount;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public double getValueAddedTax() {
        return valueAddedTax;
    }

    public Date getDateRegistered() {
        return dateRegistered;
    }

    public Party getParty() {
        return party;
    }

    public void setDateRegistered(Date dateRegistered) {
        this.dateRegistered = dateRegistered;
    }

    public void setParty(Party party) {
        this.party = party;
    }

    @Override
    public List<AbstractParty> getParticipants() {

        return Arrays.asList(party);
    }

    @NotNull
    @Override
    public PersistentState generateMappedObject(@NotNull MappedSchema schema) {
        return null;
    }

    @NotNull
    @Override
    public Iterable<MappedSchema> supportedSchemas() {
        return null;
    }

    @Override
    public String toString() {
        return "supplier: " + supplier + " customer: " + customer + " amount: " + totalAmount;
    }
}
