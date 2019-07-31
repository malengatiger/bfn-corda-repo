package com.bfn.states;

import com.bfn.contracts.SupplierContract;
import net.corda.core.contracts.BelongsToContract;
import net.corda.core.contracts.ContractState;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.Party;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

// *********
// * State *
// *********
@BelongsToContract(SupplierContract.class)
public class SupplierState implements ContractState {

    private String name, email, cellphone, fcmToken;
    private List<String> sectors;
    private Date dateRegistered;
    private Party party;

    public SupplierState(Party party,  String name, String email, String cellphone, String fcmToken, List<String> sectors) {
        this.name = name;
        this.email = email;
        this.cellphone = cellphone;
        this.fcmToken = fcmToken;
        this.sectors = sectors;
        this.party = party;
    }

    public Party getParty() {
        return party;
    }

    public String getName() {
        return name;
    }

    public List<String> getSectors() {
        return sectors;
    }

    public String getEmail() {
        return email;
    }

    public String getCellphone() {
        return cellphone;
    }

    public String getFcmToken() {
        return fcmToken;
    }

    public Date getDateRegistered() {
        return dateRegistered;
    }

    public void setDateRegistered(Date dateRegistered) {
        this.dateRegistered = dateRegistered;
    }

    @Override
    public List<AbstractParty> getParticipants() {
        List<AbstractParty> list = new ArrayList<>();

        return list;
    }
}
