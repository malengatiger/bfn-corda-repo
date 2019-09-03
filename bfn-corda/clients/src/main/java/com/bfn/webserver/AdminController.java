package com.bfn.webserver;

import com.bfn.dto.AccountInfoDTO;
import com.bfn.flows.bno.RegisterAccountFlow;
import com.bfn.states.InvoiceState;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.r3.corda.lib.accounts.contracts.states.AccountInfo;
import net.corda.core.concurrent.CordaFuture;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.identity.Party;
import net.corda.core.messaging.CordaRPCOps;
import net.corda.core.node.NodeInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Define your API endpoints here.
 */
@RestController
@RequestMapping("/admin") // The paths for HTTP requests are relative to this base path.
public class AdminController {
    private final CordaRPCOps proxy;
    private final static Logger logger = LoggerFactory.getLogger(AdminController.class);
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public AdminController(NodeRPCConnection rpc) {
        this.proxy = rpc.proxy;
        logger.info("\uD83C\uDF3A \uD83C\uDF3A \uD83C\uDF3A AdminController: NodeRPCConnection proxy has been injected: \uD83C\uDF3A " + proxy.nodeInfo().toString());
    }
    @PostMapping(value = "/startAccountRegistrationFlow", produces = "application/json")
    private AccountInfoDTO startAccountRegistrationFlow(@RequestParam String accountName) throws ExecutionException, InterruptedException {

        try {
            logger.info("\uD83D\uDD35 \uD83D\uDD35 \uD83D\uDD35  startAccountRegistrationFlow: Input Parameters: ".concat(accountName));

            CordaFuture<AccountInfo> accountInfoCordaFuture = proxy.startTrackedFlowDynamic(
                    RegisterAccountFlow.class, accountName).getReturnValue();

            logger.info("\uD83C\uDF4F AccountRegistrationFlow started ... \uD83C\uDF4F \uD83C\uDF4F waiting for signedTransaction ....");
            AccountInfo accountInfo = accountInfoCordaFuture.get();
            logger.info("\uD83C\uDF4F \uD83C\uDF4F \uD83C\uDF4F \uD83C\uDF4F AccountRegistrationFlow completed... " +
                    "\uD83C\uDF4F \uD83C\uDF4F \uD83D\uDD06 \uD83D\uDD06 \uD83D\uDD06  \n\uD83D\uDC4C \uD83D\uDC4C " +
                    "\uD83D\uDC4C \uD83D\uDC4C  accountInfo returned: \uD83E\uDD4F " +
                    accountInfo.toString().concat(" \uD83E\uDD4F \uD83E\uDD4F "));

            getAccounts();
            AccountInfoDTO dto = new AccountInfoDTO();
            dto.setHost(accountInfo.getHost().toString());
            dto.setIdentifier(accountInfo.getIdentifier().toString());
            dto.setName(accountInfo.getName());
            dto.setStatus(accountInfo.getStatus().name());
            return dto;
//
        } catch (Exception e) {
            logger.error(e.getMessage());
            throw e;
        }
    }

    @PostMapping(value = "getAccounts")
    public String getAccounts() {

        List<StateAndRef<AccountInfo>> accounts = proxy.vaultQuery(AccountInfo.class).getStates();
        int cnt = 0;
        for (StateAndRef<AccountInfo> ref: accounts) {
            cnt++;
            logger.info(" \uD83C\uDF3A AccountInfo: #".concat("" + cnt + " :: ").concat(ref.getState().getData().toString()
                    .concat(" \uD83E\uDD4F ")));
        }
        return "\uD83C\uDF3A  \uD83C\uDF3A done listing accounts:  \uD83C\uDF3A " + accounts.size();
    }
    @PostMapping(value = "getInvoiceStates")
    public String getInvoiceStates() {

        List<StateAndRef<InvoiceState>> states = proxy.vaultQuery(InvoiceState.class).getStates();
        int cnt = 0;
        for (StateAndRef<InvoiceState> ref: states) {
            cnt++;
            logger.info(" \uD83C\uDF3A InvoiceState: #".concat("" + cnt + " :: ").concat(ref.getState().getData().getSupplierInfo().getName().toString()
                    .concat(" \uD83E\uDD4F total amount: ").concat(ref.getState().getData().getTotalAmount().toString())
                    .concat(" \uD83E\uDD4F ")));
        }
        return "\uD83C\uDF3A  \uD83C\uDF3A done listing states:  \uD83C\uDF3A " + states.size();
    }

    @GetMapping(value = "/hello", produces = "text/plain")
    private String hello() {
        logger.info("/ requested. will say hello  \uD83D\uDC9A  \uD83D\uDC9A  \uD83D\uDC9A");
        return "\uD83D\uDC9A  BFNWebApi: AdminController says  \uD83E\uDD6C HELLO WORLD!  \uD83D\uDC9A  \uD83D\uDC9A";
    }
    @GetMapping(value = "/ping", produces = "application/json")
    private String ping() {
        String msg = "\uD83E\uDDE1 \uD83D\uDC9B \uD83D\uDC9A AdminController:BFN Web API pinged: " + new Date().toString()
                + " \uD83E\uDDE1 \uD83D\uDC9B \uD83D\uDC9A";

        logger.info("\uD83E\uDDA0 \uD83E\uDDA0 \uD83E\uDDA0 " + proxy.getNetworkParameters().toString() + " \uD83E\uDDA0 \uD83E\uDDA0 \uD83E\uDDA0 ");
        PingResult pingResult = new PingResult(msg,proxy.nodeInfo().toString());
        logger.info("\uD83C\uDF3A AdminController: node pinged: \uD83C\uDF3A  \uD83E\uDDE9\uD83E\uDDE9\uD83E\uDDE9 : " + proxy.nodeInfo().getLegalIdentities().get(0).getName().toString() + " \uD83E\uDDE9");

        List<NodeInfo> nodes = proxy.networkMapSnapshot();
        return GSON.toJson(pingResult);
    }
    @GetMapping(value = "/nodes", produces = "application/json")
    private String listNodes() {

        List<NodeInfo> nodes = proxy.networkMapSnapshot();
        StringBuilder sb = new StringBuilder();
        for (NodeInfo info: nodes) {
            logger.info("\uD83C\uDF3A \uD83C\uDF3A BFN Corda Node: \uD83C\uDF3A " + info.getLegalIdentities().get(0).getName().toString());
            sb.append("Node: " + info.getLegalIdentities().get(0).getName().toString()).append("\n");
        }
        return GSON.toJson(new PingResult("List of Nodes",sb.toString()));
    }
    @GetMapping(value = "/notaries", produces = "application/json")
    private List<String> listNotaries() {

        List<Party> notaryIdentities = proxy.notaryIdentities();
        List<String> list = new ArrayList<>();
        for (Party info: notaryIdentities) {
            logger.info(" \uD83D\uDD35  \uD83D\uDD35 BFN Corda Notary: \uD83C\uDF3A " + info.getName().toString());
            list.add(info.getName().toString());
        }
        return list;
    }
    @GetMapping(value = "/flows", produces = "application/json")
    private List<String> listFlows() {

        logger.info("🥬 🥬 🥬 🥬 Registered Flows on Corda BFN ...  \uD83E\uDD6C ");
        List<String> flows = proxy.registeredFlows();
        int cnt = 0;
        for (String info: flows) {
            cnt++;
            logger.info("\uD83E\uDD4F \uD83E\uDD4F #$"+cnt+" \uD83E\uDD6C BFN Corda RegisteredFlow:  \uD83E\uDD4F" + info + "   \uD83C\uDF4E ");
        }

        logger.info("🥬 🥬 🥬 🥬 Total Registered Flows  \uD83C\uDF4E  " + cnt + "  \uD83C\uDF4E \uD83E\uDD6C ");
        return flows;
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
