package com.bfn.webserver;

import com.bfn.flows.SupplierRegisterFlow;
import com.bfn.states.SupplierState;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.corda.core.identity.CordaX500Name;
import net.corda.core.identity.Party;
import net.corda.core.messaging.CordaRPCOps;
import net.corda.core.node.NodeInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.List;

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
    @GetMapping(value = "/startFlow", produces = "application/json")
    private String startFlow() {

        Party party = proxy.nodeInfo().getLegalIdentities().get(0);
        CordaX500Name cordaX500Name = new CordaX500Name("CapeTownCustomer","Cape Town","ZA");
        Party counterParty = proxy.wellKnownPartyFromX500Name(cordaX500Name);
        logger.info("\uD83E\uDD1F \uD83E\uDD1F party: ".concat(party.toString()).concat(" \uD83C\uDFC0  will start flow; counterParty: \uD83C\uDF4A " + counterParty.getName().toString() + " \uD83C\uDF4A"));
        SupplierState state = new SupplierState(party, "SupplierA", "supllier.a@gmail.com", "099 778 5643", "", "");

        proxy.startTrackedFlowDynamic(SupplierRegisterFlow.class, state, counterParty);
        logger.info("\uD83C\uDF4F flow should be started ... \uD83C\uDF4F \uD83C\uDF4F any evidence of this?");
        List<String> flows = proxy.registeredFlows();
        for (String f : flows) {
            logger.info("\uD83E\uDD4F Registered Flow: \uD83E\uDD4F \uD83E\uDD4F ".concat(f));
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