package com.bfn.webserver;

import com.bfn.dto.AccountInfoDTO;
import com.bfn.dto.InvoiceDTO;
import com.bfn.dto.InvoiceOfferDTO;
import com.bfn.flows.invoices.RegisterInvoiceFlow;
import com.bfn.states.InvoiceState;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.r3.corda.lib.accounts.contracts.states.AccountInfo;
import net.corda.core.concurrent.CordaFuture;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.messaging.CordaRPCOps;
import net.corda.core.node.NodeInfo;
import net.corda.core.transactions.SignedTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.*;

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
    @GetMapping(value = "/getAccountInfoByID", produces = "application/json")
    private AccountInfoDTO getAccountInfoByID(@RequestParam String id) throws Exception {
        //todo - learn how to use criteria or SQL queries
        try {
//            QueryCriteria generalCriteria = new VaultQueryCriteria(Vault.StateStatus.ALL);
//            FieldInfo attributeId = getField("identifier", AccountInfo.class);
//            logger.info("getField executed: ".concat(attributeId.getName()));
//            CriteriaExpression criteriaExpression = Builder.equal(attributeId, new UniqueIdentifier(id));
//
//
//            //QueryCriteria queryCriteria = new VaultQueryCriteria(Vault.StateStatus.ALL, ImmutableSet.of(AccountInfo.class));
//            QueryCriteria queryCriteria = new VaultCustomQueryCriteria<>(criteriaExpression, Vault.StateStatus.ALL, ImmutableSet.of(AccountInfo.class));
//                    QueryCriteria criteria = generalCriteria.and(queryCriteria);
            List<StateAndRef<AccountInfo>> results = proxy.vaultQuery(AccountInfo.class).getStates();
            if (results.size() == 0) {
                throw new Exception("AccountInfo not found: ".concat(id));
            }
            logger.info("\uD83D\uDC99 \uD83D\uDC99 \uD83D\uDC99 AccountInfo's found: " + results.size() + " \uD83D\uDC99");
            AccountInfo info = null;
            for (StateAndRef<AccountInfo> ref: results) {
                if (ref.getState().getData().getIdentifier().getId().toString().equalsIgnoreCase(id)) {
                    info = ref.getState().getData();
                }
            }
            if (info == null) {
                throw new Exception(" \uD83E\uDDE1 AccountInfo not found");
            }
            AccountInfoDTO dto = new AccountInfoDTO(
                    info.getIdentifier().getId().toString(),
                    info.getHost().toString(),
                    info.getName(), info.getStatus().name());
            logger.info(" \uD83E\uDDE1  \uD83E\uDDE1 AccountInfo found  \uD83E\uDDE1 ".concat(GSON.toJson(dto)));
            return dto;
        } catch (Exception e) {
            e.printStackTrace();
            if (e.getMessage() == null) {
                throw new Exception("getAccountInfoByID encountered unknown error");
            } else {
                throw new Exception(e.getMessage());
            }
        }
    }


    @PostMapping(value = "startRegisterInvoiceFlow")
    public InvoiceDTO startRegisterInvoiceFlow(@RequestBody InvoiceDTO invoice) throws Exception {

        logger.info("Input Parameters; \uD83C\uDF4F \uD83C\uDF4F \uD83C\uDF4F InvoiceDTO: " + GSON.toJson(invoice) + " \uD83C\uDF4F \uD83C\uDF4F \uD83C\uDF4F");
        try {
            logger.info("\uD83C\uDF4F SUPPLIER: ".concat(invoice.getSupplierId()).concat("  \uD83D\uDD06  ")
            .concat("  \uD83E\uDDE1 CUSTOMER: ").concat(invoice.getCustomerId()));

            List<StateAndRef<AccountInfo>> accounts = proxy.vaultQuery(AccountInfo.class).getStates();
            logger.info(" \uD83C\uDF4F \uD83C\uDF4F AccountInfo's found by vaultQuery: \uD83D\uDD34 " + accounts.size() + " \uD83D\uDD34 ");
            AccountInfo supplierInfo = null, customerInfo = null;
            for (StateAndRef<AccountInfo> info: accounts) {
                logger.info(" \uD83C\uDF4F \uD83C\uDF4F AccountInfo found: ".concat(info.toString()));
                if (info.getState().getData().getIdentifier().toString().equalsIgnoreCase(invoice.getCustomerId())) {
                    customerInfo = info.getState().getData();
                }
                if (info.getState().getData().getIdentifier().toString().equalsIgnoreCase(invoice.getSupplierId())) {
                    supplierInfo = info.getState().getData();
                }
            }
            if (supplierInfo == null) {
                throw new Exception("Supplier is fucking missing");
            }
            if (customerInfo == null) {
                throw new Exception("Customer is bloody missing");
            }
            logger.info("we have names and parties \uD83C\uDF4F");
            InvoiceState invoiceState = new InvoiceState(
                    invoice.getInvoiceNumber(),
                    invoice.getDescription(),
                    invoice.getAmount(),
                    invoice.getTotalAmount(),
                    invoice.getValueAddedTax(),
                    new Date(proxy.currentNodeTime().toEpochMilli()),
                    supplierInfo,customerInfo,
                    UUID.randomUUID());

            logger.info("\uD83C\uDF4F ...... start the flow ...");
            CordaFuture<SignedTransaction> signedTransactionCordaFuture = proxy.startTrackedFlowDynamic(
                    RegisterInvoiceFlow.class, invoiceState).getReturnValue();

            SignedTransaction issueTx = signedTransactionCordaFuture.get();
            logger.info("\uD83C\uDF4F \uD83C\uDF4F \uD83C\uDF4F \uD83C\uDF4F flow completed... \uD83C\uDF4F \uD83C\uDF4F \uD83D\uDD06 \uD83D\uDD06 \uD83D\uDD06  \n\uD83D\uDC4C \uD83D\uDC4C \uD83D\uDC4C \uD83D\uDC4C  signedTransaction returned: \uD83E\uDD4F " + issueTx.toString().concat(" \uD83E\uDD4F \uD83E\uDD4F "));
            return getDTO(invoiceState);
        } catch (Exception e) {
            if (e.getMessage() != null) {
                throw new Exception("Failed to register invoice. ".concat(e.getMessage()));
            } else {
                throw new Exception("Failed to register invoice. Unknown cause");
            }
        }
    }

    @PostMapping(value = "startInvoiceOfferFlow")
    public InvoiceDTO startInvoiceOfferFlow(@RequestBody InvoiceOfferDTO invoice) throws Exception {

        logger.info("Input Parameters; \uD83C\uDF4F \uD83C\uDF4F \uD83C\uDF4F InvoiceOfferDTO: " + GSON.toJson(invoice) + " \uD83C\uDF4F \uD83C\uDF4F \uD83C\uDF4F");
        try {
            logger.info("\uD83C\uDF4F SUPPLIER: ".concat(invoice.getSupplierId()).concat("  \uD83D\uDD06  ")
                    .concat("  \uD83E\uDDE1 CUSTOMER: ").concat(invoice.getInvestorId()));

            List<StateAndRef<AccountInfo>> accounts = proxy.vaultQuery(AccountInfo.class).getStates();
            logger.info(" \uD83C\uDF4F \uD83C\uDF4F AccountInfo's found by vaultQuery: \uD83D\uDD34 " + accounts.size() + " \uD83D\uDD34 ");
            AccountInfo supplierInfo = null, investorInfo = null;
            for (StateAndRef<AccountInfo> info: accounts) {
                logger.info(" \uD83C\uDF4F \uD83C\uDF4F AccountInfo found: ".concat(info.toString()));
                if (info.getState().getData().getIdentifier().toString().equalsIgnoreCase(invoice.getInvestorId())) {
                    investorInfo = info.getState().getData();
                }
                if (info.getState().getData().getIdentifier().toString().equalsIgnoreCase(invoice.getSupplierId())) {
                    supplierInfo = info.getState().getData();
                }
            }
            if (supplierInfo == null) {
                throw new Exception("Supplier is fucking missing");
            }
            if (investorInfo == null) {
                throw new Exception("Customer is bloody missing");
            }
            logger.info("we have names and parties \uD83C\uDF4F");
            InvoiceState invoiceState = new InvoiceState(
                    invoice.getInvoiceNumber(),
                    invoice.getDescription(),
                    invoice.getAmount(),
                    invoice.getTotalAmount(),
                    invoice.getValueAddedTax(),
                    new Date(proxy.currentNodeTime().toEpochMilli()),
                    supplierInfo,investorInfo,
                    UUID.randomUUID());

            logger.info("\uD83C\uDF4F ...... start the flow ...");
            CordaFuture<SignedTransaction> signedTransactionCordaFuture = proxy.startTrackedFlowDynamic(
                    RegisterInvoiceFlow.class, invoiceState).getReturnValue();

            SignedTransaction issueTx = signedTransactionCordaFuture.get();
            logger.info("\uD83C\uDF4F \uD83C\uDF4F \uD83C\uDF4F \uD83C\uDF4F flow completed... \uD83C\uDF4F \uD83C\uDF4F \uD83D\uDD06 \uD83D\uDD06 \uD83D\uDD06  \n\uD83D\uDC4C \uD83D\uDC4C \uD83D\uDC4C \uD83D\uDC4C  signedTransaction returned: \uD83E\uDD4F " + issueTx.toString().concat(" \uD83E\uDD4F \uD83E\uDD4F "));
            return getDTO(invoiceState);
        } catch (Exception e) {
            if (e.getMessage() != null) {
                throw new Exception("Failed to register invoice. ".concat(e.getMessage()));
            } else {
                throw new Exception("Failed to register invoice. Unknown cause");
            }
        }
    }

    @GetMapping(value = "getInvoiceStates")
    public List<InvoiceDTO> getInvoiceStates() {

        List<StateAndRef<InvoiceState>> states = proxy.vaultQuery(InvoiceState.class).getStates();
        List<InvoiceDTO> list = new ArrayList<>();
        int cnt = 0;
        for (StateAndRef<InvoiceState> ref: states) {
            cnt++;
            logger.info(" \uD83C\uDF3A InvoiceState: #".concat("" + cnt + " :: Supplier: ").concat(ref.getState().getData().getSupplierInfo().getName()
                    .concat("   \uD83D\uDD06  \uD83D\uDD06  Customer: ").concat(ref.getState().getData().getCustomerInfo().getName())
                    .concat(" \uD83E\uDD4F total amount: ").concat(ref.getState().getData().getTotalAmount().toString())
                    .concat(" \uD83E\uDD4F ")));
            InvoiceState m = ref.getState().getData();
            list.add(getDTO(m));
        }
        String m = " \uD83C\uDF3A  \uD83C\uDF3A done listing states:  \uD83C\uDF3A " + list.size();
        logger.info(GSON.toJson(list));

        return list;
    }

    private InvoiceDTO getDTO(InvoiceState state) {
        InvoiceDTO invoice = new InvoiceDTO(
                state.getInvoiceId().toString(),
                state.getInvoiceNumber(),
                state.getDescription(),
                state.getAmount(),
                state.getTotalAmount(),
                state.getValueAddedTax(),
                state.getSupplierInfo().getIdentifier().getId().toString(),
                state.getCustomerInfo().getIdentifier().getId().toString());
        invoice.setDateRegistered(state.getDateRegistered());
        return invoice;
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
