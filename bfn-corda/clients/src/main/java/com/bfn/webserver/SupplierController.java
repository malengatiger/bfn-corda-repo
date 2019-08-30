package com.bfn.webserver;

import com.bfn.flows.invoices.AddInvoiceFlow;
import com.bfn.states.InvoiceState;
import com.bfn.states.SupplierState;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.r3.businessnetworks.membership.flows.member.RequestMembershipFlow;
import com.r3.businessnetworks.membership.states.MembershipState;
import com.r3.businessnetworks.membership.states.MembershipStatus;
import net.corda.core.concurrent.CordaFuture;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.identity.CordaX500Name;
import net.corda.core.identity.Party;
import net.corda.core.messaging.CordaRPCOps;
import net.corda.core.messaging.FlowHandle;
import net.corda.core.node.NodeInfo;
import net.corda.core.transactions.SignedTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.List;
import java.util.Random;

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
    @GetMapping(value = "/startRegisterFlow", produces = "application/json")
    private String startRegisterFlow() {

        Party party = proxy.nodeInfo().getLegalIdentities().get(0);
        CordaX500Name cordaX500Name = new CordaX500Name("Sandton","Sandton","ZA");
        Party bno = proxy.wellKnownPartyFromX500Name(cordaX500Name);
        logger.info("\uD83E\uDD1F \uD83E\uDD1F party: ".concat(party.toString()).concat(" \uD83C\uDFC0  will start flow; bno: \uD83C\uDF4A " + bno.getName().toString() + " \uD83C\uDF4A"));
        SupplierState supplierState = new SupplierState(party, "SupplierA", "supllier.a@gmail.com", "099 778 5643", "", "");

        MembershipState membershipState = new MembershipState(party,bno, supplierState,
                new Date().toInstant(),new Date().toInstant(), MembershipStatus.ACTIVE, new UniqueIdentifier());
        proxy.startTrackedFlowDynamic(RequestMembershipFlow.class, membershipState, bno);
        logger.info("\uD83C\uDF4F flow should be started ... \uD83C\uDF4F \uD83C\uDF4F any evidence of this?");



        return GSON.toJson(new PingResult(" \uD83E\uDDE1 \uD83D\uDC9B \uD83D\uDC9A Flow started ...", " \uD83E\uDD1E \uD83E\uDD1E Do not know if we're good"));
    }
    Random random = new Random(System.currentTimeMillis());
    @GetMapping(value = "/startAddInvoiceFlow", produces = "application/json")
    private String startAddInvoiceFlow() {


        logger.info("\uD83C\uDF4F .... start AddInvoiceFlow ...");
        try {
            int num = random.nextInt(100);


            CordaFuture<SignedTransaction> signedTransactionCordaFuture   = proxy.startFlowDynamic(AddInvoiceFlow.class, "PO99800134", "invoiceID_001","NewYork@New York@US","Sandton@Sandton@ZA", "London@London@GB","walletID_001", "user_001",
                    "invNum_002","Description here", "Reference Data", 200000.00 * num, 15.0, 230000.00 * num).getReturnValue();
            SignedTransaction issueTx = signedTransactionCordaFuture.get();

            logger.info("\uD83C\uDF4F flow should be started ... \uD83C\uDF4F \uD83C\uDF4F any evidence of this????  \uD83D\uDD06 \uD83D\uDD06 \uD83D\uDD06  isDone: " + issueTx.toString());
        } catch (Exception e) {
            logger.error(e.getMessage());
        }



        return GSON.toJson(new PingResult(" \uD83E\uDDE1 \uD83D\uDC9B \uD83D\uDC9A Flow started ...", " \uD83E\uDD1E \uD83E\uDD1E Do not know if we're good"));
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
