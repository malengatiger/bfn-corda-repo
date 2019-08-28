package com.bfn.states;

import com.bfn.contracts.InvoiceContract;
import net.corda.core.contracts.BelongsToContract;
import net.corda.core.contracts.ContractState;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.Party;
import net.corda.core.schemas.MappedSchema;
import net.corda.core.schemas.PersistentState;
import net.corda.core.schemas.QueryableState;
import net.corda.core.serialization.CordaSerializable;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

// *********
// * State *
// *********
@BelongsToContract(InvoiceContract.class)
@CordaSerializable
public class InvoiceState implements ContractState {

    String purchaseOrder;
    String invoiceId;
    String deliveryNote;
    String company;
    String customer;
    String wallet;
    String user;
    String invoiceNumber;
    String description;
    String reference;

    boolean isOnOffer, isSettled;
    double amount, totalAmount, valueAddedTax;
    private Date dateRegistered;
    private Party supplier, investor;

    public InvoiceState(String purchaseOrder, String invoiceId, String deliveryNote, String company, String customer, String wallet, String user, String invoiceNumber, String description, String reference, boolean isOnOffer, boolean isSettled, double amount, double totalAmount, double valueAddedTax, Date dateRegistered, Party supplier, Party investor) {
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
        this.supplier = supplier;
        this.investor = investor;
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

    public Party getSupplier() {
        return supplier;
    }

    public void setDateRegistered(Date dateRegistered) {
        this.dateRegistered = dateRegistered;
    }

    public void setSupplier(Party supplier) {
        this.supplier = supplier;
    }

    @Override
    public List<AbstractParty> getParticipants() {

        return Arrays.asList(supplier, investor);
    }


}
