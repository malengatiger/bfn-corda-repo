package com.bfn.webserver;

import com.bfn.dto.InvoiceDTO;
import com.bfn.flows.invoices.RegisterInvoiceFlow;
import com.bfn.states.InvoiceState;
import com.bfn.states.SupplierState;
import com.bfn.util.Member;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.r3.businessnetworks.membership.flows.member.RequestMembershipFlow;
import com.r3.businessnetworks.membership.states.MembershipState;
import com.r3.businessnetworks.membership.states.MembershipStatus;
import net.corda.core.concurrent.CordaFuture;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.identity.CordaX500Name;
import net.corda.core.identity.Party;
import net.corda.core.messaging.CordaRPCOps;
import net.corda.core.node.NodeInfo;
import net.corda.core.transactions.SignedTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.ExecutionException;

/**
 * Define your API endpoints here.
 */
@RestController
@RequestMapping("/supplier") // The paths for HTTP requests are relative to this base path.
public class SupplierController {
    private final CordaRPCOps proxy;
    private final static Logger logger = LoggerFactory.getLogger(SupplierController.class);
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public SupplierController(NodeRPCConnection rpc) {
        this.proxy = rpc.proxy;
        logger.info("\uD83C\uDF3A \uD83C\uDF3A \uD83C\uDF3A SupplierController: NodeRPCConnection proxy has been injected: \uD83C\uDF3A " + proxy.nodeInfo().toString());
    }

    @GetMapping(value = "/hello", produces = "text/plain")
    private String hello() {
        logger.info("/ requested. will say hello  \uD83D\uDC9A  \uD83D\uDC9A  \uD83D\uDC9A");
        return " \uD83E\uDD6C  \uD83E\uDD6C BFNWebApi: SupplierController says  \uD83E\uDD6C HELLO WORLD!  \uD83D\uDC9A  \uD83D\uDC9A";
    }

    @GetMapping(value = "/ping", produces = "application/json")
    private String ping() {
        String msg = "\uD83E\uDDE1 \uD83D\uDC9B \uD83D\uDC9A SupplierController:BFN Web API pinged: " + new Date().toString()
                + " \uD83E\uDDE1 \uD83D\uDC9B \uD83D\uDC9A";

        logger.info("\uD83E\uDDA0 \uD83E\uDDA0 \uD83E\uDDA0 " + proxy.getNetworkParameters().toString() + " \uD83E\uDDA0 \uD83E\uDDA0 \uD83E\uDDA0 ");
        PingResult pingResult = new PingResult(msg, proxy.nodeInfo().toString());
        logger.info("\uD83C\uDF3A SupplierController: node pinged: \uD83C\uDF3A  \uD83E\uDDE9\uD83E\uDDE9\uD83E\uDDE9 : " + proxy.nodeInfo().getLegalIdentities().get(0).getName().toString() + " \uD83E\uDDE9");

        List<NodeInfo> nodes = proxy.networkMapSnapshot();
        return GSON.toJson(pingResult);
    }

    @GetMapping(value = "/nodes", produces = "application/json")
    private String listNodes() {


        List<NodeInfo> nodes = proxy.networkMapSnapshot();
        StringBuilder sb = new StringBuilder();
        for (NodeInfo info : nodes) {
            logger.info("\uD83C\uDF3A \uD83C\uDF3A BFN Corda Supplier Node: \uD83C\uDF3A " + info.getLegalIdentities().get(0).getName().toString());
            sb.append("Node: " + info.getLegalIdentities().get(0).getName().toString()).append("\n");
        }
        return GSON.toJson(new PingResult(" \uD83E\uDDE1 \uD83D\uDC9B \uD83D\uDC9A List of Nodes", sb.toString()));
    }

    @PostMapping(value = "/startMemberRegistrationFlow", produces = "application/json")
    private String startMemberRegistrationFlow(@RequestBody Member member) throws ExecutionException, InterruptedException {

        try {
            logger.info(" \uD83C\uDD7F️ \uD83C\uDD7F️ \uD83C\uDD7F️ Member Input Parameters: ".concat(GSON.toJson(member)));
            Party party = proxy.nodeInfo().getLegalIdentities().get(0);
            CordaX500Name cordaX500Name = new CordaX500Name("Sandton", "Sandton", "ZA");
            Party bno = proxy.wellKnownPartyFromX500Name(cordaX500Name);
            logger.info("\uD83E\uDD1F \uD83E\uDD1F party: ".concat(party.toString()).concat(" \uD83C\uDFC0 \uD83C\uDFC0  bno: \uD83C\uDF4A " + bno.getName().toString() + " \uD83C\uDF4A"));

            MembershipState membershipState = new MembershipState(party, bno, member,
                    new Date().toInstant(), new Date().toInstant(), MembershipStatus.ACTIVE, new UniqueIdentifier());
            logger.info("\uD83C\uDF4F MemberRegistrationFlow :: \uD83D\uDC99 \uD83D\uDC99 \uD83D\uDC99 membershipState created");
            CordaFuture<SignedTransaction> signedTransactionCordaFuture = proxy.startFlowDynamic(
                    RequestMembershipFlow.class, membershipState, bno).getReturnValue();

            logger.info("\uD83C\uDF4F MemberRegistrationFlow started ... \uD83C\uDF4F \uD83C\uDF4F waiting for signedTransaction ....");
            SignedTransaction issueTx = signedTransactionCordaFuture.get();
            logger.info("\uD83C\uDF4F \uD83C\uDF4F \uD83C\uDF4F \uD83C\uDF4F MemberRegistrationFlow completed... " +
                    "\uD83C\uDF4F \uD83C\uDF4F \uD83D\uDD06 \uD83D\uDD06 \uD83D\uDD06  \n\uD83D\uDC4C \uD83D\uDC4C " +
                    "\uD83D\uDC4C \uD83D\uDC4C  signedTransaction returned: \uD83E\uDD4F " +
                    issueTx.toString().concat(" \uD83E\uDD4F \uD83E\uDD4F "));
            String publicKey = membershipState.getMember().getOwningKey().toString();
            logger.info(" \uD83D\uDD11  \uD83D\uDD11  \uD83D\uDD11 Public Key returned: \uD83C\uDF81 "
                    .concat(publicKey).concat(" \uD83C\uDF81"));
            return publicKey;
//            return issueTx.getId().toString();
        } catch (Exception e) {
            logger.error(e.getMessage());
            throw e;
        }
    }

    @PostMapping(value = "getInvoiceStates")
    public String getInvoiceStates() {

        List<StateAndRef<InvoiceState>> states = proxy.vaultQuery(InvoiceState.class).getStates();
        int cnt = 0;
        for (StateAndRef<InvoiceState> ref: states) {
            cnt++;
            logger.info(" \uD83C\uDF3A InvoiceState: #".concat("" + cnt + " :: ").concat(ref.getState().getData().getSupplier().getName().toString()
            .concat(" \uD83E\uDD4F total amount: ").concat(ref.getState().getData().getTotalAmount().toString())
            .concat(" \uD83E\uDD4F ")));
        }
        return "\uD83C\uDF3A  \uD83C\uDF3A done listing states:  \uD83C\uDF3A " + states.size();
    }

    @PostMapping(value = "startRegisterInvoiceFlow")
    public String startRegisterInvoiceFlow(@RequestBody InvoiceDTO invoice) throws ExecutionException, InterruptedException {

        logger.info("Input Parameters; \uD83C\uDF4F \uD83C\uDF4F \uD83C\uDF4F InvoiceDTO: " + GSON.toJson(invoice) + " \uD83C\uDF4F \uD83C\uDF4F \uD83C\uDF4F");
        try {
            logger.info("\uD83C\uDF4F ORG: ".concat(invoice.getSupplier().getOrganization()).concat("  \uD83D\uDD06 LOCALITY: ").concat(invoice.getSupplier().getLocality()
            .concat("  \uD83E\uDDE1 CNTRY: ").concat(invoice.getSupplier().getCountry())));
            logger.info("\uD83C\uDF4F ORG: ".concat(invoice.getCustomer().getOrganization()).concat("  \uD83D\uDD06 LOCALITY: ").concat(invoice.getCustomer().getLocality()
                    .concat(" \uD83E\uDDE1 CNTRY: ").concat(invoice.getCustomer().getCountry())));

            String org1 = invoice.getSupplier().getOrganization();
            String loc1 = invoice.getSupplier().getLocality();
            String cntry1 = invoice.getSupplier().getCountry();
            CordaX500Name supplier = new CordaX500Name(org1, loc1, cntry1);
            Party supplierParty = proxy.wellKnownPartyFromX500Name(supplier);
            logger.info("\uD83D\uDC4C \uD83D\uDC4C supplierParty: " + supplierParty.getName().toString().concat(" \uD83C\uDF3A \uD83C\uDF3A "));

            String org2 = invoice.getCustomer().getOrganization();
            String loc2 = invoice.getCustomer().getLocality();
            String cntry2 = invoice.getCustomer().getCountry();
            CordaX500Name customer = new CordaX500Name(org2, loc2, cntry2);
            Party customerParty = proxy.wellKnownPartyFromX500Name(customer);
            logger.info("\uD83D\uDC4C \uD83D\uDC4C customerParty: " + customerParty.getName().toString().concat(" \uD83C\uDF3A \uD83C\uDF3A "));


            logger.info("we have names and parties \uD83C\uDF4F");
            InvoiceState invoiceState = new InvoiceState(
                    invoice.getPurchaseOrder(),
                    invoice.getInvoiceId(),
                    invoice.getWallet(),
                    invoice.getUser(),
                    invoice.getInvoiceNumber(),
                    invoice.getDescription(),
                    invoice.getReference(),
                    invoice.getAmount(),
                    invoice.getTotalAmount(),
                    invoice.getValueAddedTax(), new Date(),
                    supplierParty, customerParty);

            logger.info("\uD83C\uDF4F ...... start the flow ...");
            CordaFuture<SignedTransaction> signedTransactionCordaFuture = proxy.startFlowDynamic(
                    RegisterInvoiceFlow.class, invoiceState).getReturnValue();

            SignedTransaction issueTx = signedTransactionCordaFuture.get();
            logger.info("\uD83C\uDF4F \uD83C\uDF4F \uD83C\uDF4F \uD83C\uDF4F flow completed... \uD83C\uDF4F \uD83C\uDF4F \uD83D\uDD06 \uD83D\uDD06 \uD83D\uDD06  \n\uD83D\uDC4C \uD83D\uDC4C \uD83D\uDC4C \uD83D\uDC4C  signedTransaction returned: \uD83E\uDD4F " + issueTx.toString().concat(" \uD83E\uDD4F \uD83E\uDD4F "));
            return issueTx.getId().toString();
        } catch (Exception e) {
            logger.error(e.getMessage());
            throw e;
        }
    }

    private class PingResult {
        String message;
        String nodeInfo;

        PingResult(String message, String nodeInfo) {
            this.message = message;
            this.nodeInfo = nodeInfo;
        }
    }
}
