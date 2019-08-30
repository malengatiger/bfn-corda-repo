package com.bfn.dto;

import java.util.Date;

public class InvoiceDTO {
    String purchaseOrder;
    String invoiceId;
    String wallet;
    String user;
    String invoiceNumber;
    String description;
    String reference;

    Double amount, totalAmount, valueAddedTax;
    private Date dateRegistered;
    private MyCordaName supplier, customer;

    public InvoiceDTO() {
    }

    public InvoiceDTO(String purchaseOrder, String invoiceId, String wallet, String user, String invoiceNumber,
                      String description, String reference, Double amount, Double totalAmount, Double valueAddedTax,
                      Date dateRegistered, MyCordaName supplier, MyCordaName customer) {
        this.purchaseOrder = purchaseOrder;
        this.invoiceId = invoiceId;
        this.wallet = wallet;
        this.user = user;
        this.invoiceNumber = invoiceNumber;
        this.description = description;
        this.reference = reference;
        this.amount = amount;
        this.totalAmount = totalAmount;
        this.valueAddedTax = valueAddedTax;
        this.dateRegistered = dateRegistered;
        this.supplier = supplier;
        this.customer = customer;
    }

    public String getPurchaseOrder() {
        return purchaseOrder;
    }

    public void setPurchaseOrder(String purchaseOrder) {
        this.purchaseOrder = purchaseOrder;
    }

    public String getInvoiceId() {
        return invoiceId;
    }

    public void setInvoiceId(String invoiceId) {
        this.invoiceId = invoiceId;
    }

    public String getWallet() {
        return wallet;
    }

    public void setWallet(String wallet) {
        this.wallet = wallet;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public Double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(Double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public Double getValueAddedTax() {
        return valueAddedTax;
    }

    public void setValueAddedTax(Double valueAddedTax) {
        this.valueAddedTax = valueAddedTax;
    }

    public Date getDateRegistered() {
        return dateRegistered;
    }

    public void setDateRegistered(Date dateRegistered) {
        this.dateRegistered = dateRegistered;
    }

    public MyCordaName getSupplier() {
        return supplier;
    }

    public void setSupplier(MyCordaName supplier) {
        this.supplier = supplier;
    }

    public MyCordaName getCustomer() {
        return customer;
    }

    public void setCustomer(MyCordaName customer) {
        this.customer = customer;
    }
}
