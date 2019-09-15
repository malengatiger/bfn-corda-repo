package com.bfn.util;

import com.bfn.dto.AccountInfoDTO;
import com.bfn.dto.InvoiceDTO;
import com.bfn.dto.InvoiceOfferDTO;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.corda.core.messaging.CordaRPCOps;
import org.apache.commons.io.output.DemuxOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

public class DemoUtil {

    private final static Logger logger = LoggerFactory.getLogger(TheUtil.class);
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static CordaRPCOps proxy;
    private static List<AccountInfoDTO> suppliers, customers, investors;
    private static DemoSummary demoSummary = new DemoSummary();

    public static DemoSummary start(CordaRPCOps mProxy) throws Exception {
        proxy = mProxy;
        logger.info("\n\uD83D\uDD06 \uD83D\uDD06 \uD83D\uDD06 \uD83D\uDD06 DemoUtil started ...  \uD83D\uDD06 \uD83D\uDD06 will list network components");
        demoSummary.setStarted(new Date().toString());
        long start = System.currentTimeMillis();
        suppliers = new ArrayList<>();
        customers = new ArrayList<>();
        investors = new ArrayList<>();
        List nodes = TheUtil.listNodes(proxy);
        demoSummary.setNumberOfNodes(nodes.size());
        TheUtil.listNotaries(proxy);
        List flows = TheUtil.listFlows(proxy);
        demoSummary.setNumberOfFlows(flows.size());
        registerAccounts();
        long end = System.currentTimeMillis();
        demoSummary.setEnded(new Date().toString());
        demoSummary.setElapsedSeconds((end - start)/1000);
        return demoSummary;
    }

    private static void registerAccounts() throws Exception {
        logger.info("\n\n\uD83D\uDD06 \uD83D\uDD06 \uD83D\uDD06 \uD83D\uDD06 registerAccounts started ...  \uD83D\uDD06 \uD83D\uDD06 will add 3 accounts");
        AccountInfoDTO supplier1 = TheUtil.startAccountRegistrationFlow(proxy,"Supplier One Pty Ltd");
        AccountInfoDTO customer1 = TheUtil.startAccountRegistrationFlow(proxy,"Customer One LLC");
        AccountInfoDTO investor1 = TheUtil.startAccountRegistrationFlow(proxy,"Investor One Inc.");

        AccountInfoDTO supplier2 = TheUtil.startAccountRegistrationFlow(proxy,"Supplier Two Pty Ltd");
        AccountInfoDTO customer2 = TheUtil.startAccountRegistrationFlow(proxy,"Customer Two LLC");
        AccountInfoDTO investor2 = TheUtil.startAccountRegistrationFlow(proxy,"Investor Two LLC");

        AccountInfoDTO supplier3 = TheUtil.startAccountRegistrationFlow(proxy,"Supplier Three Pty Ltd");
        AccountInfoDTO customer3 = TheUtil.startAccountRegistrationFlow(proxy,"Customer Three LLC");
        AccountInfoDTO investor3 = TheUtil.startAccountRegistrationFlow(proxy,"Investor Three Pty Ltd");

        AccountInfoDTO investor4 = TheUtil.startAccountRegistrationFlow(proxy,"Investor Four Inc.");
        AccountInfoDTO investor5 = TheUtil.startAccountRegistrationFlow(proxy,"Investor Five LLC");

        AccountInfoDTO supplier4 = TheUtil.startAccountRegistrationFlow(proxy,"Supplier Four Pty Ltd");
        AccountInfoDTO supplier5 = TheUtil.startAccountRegistrationFlow(proxy,"Supplier Five Pty Ltd");
        AccountInfoDTO supplier6 = TheUtil.startAccountRegistrationFlow(proxy,"Supplier Six Pty Ltd");
        AccountInfoDTO supplier7 = TheUtil.startAccountRegistrationFlow(proxy,"Supplier Seven Pty Ltd");

        suppliers.add(supplier1);
        suppliers.add(supplier2);
        suppliers.add(supplier3);
        suppliers.add(supplier4);
        suppliers.add(supplier5);
        suppliers.add(supplier6);
        suppliers.add(supplier7);


        customers.add(customer1);
        customers.add(customer2);
        customers.add(customer3);

        investors.add(investor1);
        investors.add(investor2);
        investors.add(investor3);
        investors.add(investor4);
        investors.add(investor5);

        logger.info(" \uD83D\uDD06 \uD83D\uDD06 \uD83D\uDD06 \uD83D\uDD06 registerAccounts complete ...  \uD83D\uDD06 \uD83D\uDD06 added 3 accounts");
        List<AccountInfoDTO> list = TheUtil.getAccounts(proxy);
        logger.info(" \uD83C\uDF4E  \uD83C\uDF4E List of Accounts on Node  \uD83C\uDF4E  \uD83C\uDF4E "
                .concat(GSON.toJson(list)));
        demoSummary.setNumberOfAccounts(list.size());
        registerInvoices();

    }

    static Random random = new Random(System.currentTimeMillis());
    private static void registerInvoices() throws Exception {
        logger.info("\n\n\uD83D\uDD06 \uD83D\uDD06 \uD83D\uDD06 \uD83D\uDD06 registerInvoices started ...  \uD83D\uDD06 \uD83D\uDD06 will add 3 accounts");

        for (AccountInfoDTO supplier: suppliers ) {
            for (AccountInfoDTO customer: customers) {
                InvoiceDTO m = new InvoiceDTO();
                m.setInvoiceNumber("INV_" + System.currentTimeMillis());
                m.setSupplierId(supplier.getIdentifier());
                m.setCustomerId(customer.getIdentifier());
                int num = random.nextInt(100);
                if (num == 0) num = 92;
                m.setAmount(num * 1000.05);
                m.setValueAddedTax(15.0);
                m.setTotalAmount(m.getAmount() * 1.15);
                m.setDescription("Demo Invoice at ".concat(new Date().toString()));
                m.setDateRegistered(new Date());

                InvoiceDTO invoice = TheUtil.startRegisterInvoiceFlow(proxy,m);
                for (AccountInfoDTO investor: investors) {
                    registerInvoiceOffer(invoice,supplier,investor);
                }
            }
        }

        List<InvoiceDTO> list = TheUtil.getInvoiceStates(proxy);
        logger.info(" \uD83C\uDF4A  \uD83C\uDF4A "+list.size()+" InvoiceStates added ...  \uD83C\uDF4A ".concat(GSON.toJson(list)));
        demoSummary.setNumberOfInvoices(list.size());
        List<InvoiceOfferDTO> list2 = TheUtil.getInvoiceOfferStates(proxy);
        demoSummary.setNumberOfInvoiceOffers(list2.size());
        logger.info(" \uD83C\uDF4A  \uD83C\uDF4A "+list2.size()+" InvoiceOfferStates added ...  \uD83C\uDF4A ".concat(GSON.toJson(list2)));

    }
    private static void registerInvoiceOffer(InvoiceDTO invoice, AccountInfoDTO supplier, AccountInfoDTO investor) throws Exception {
        logger.info("\n\uD83D\uDD06 \uD83D\uDD06 \uD83D\uDD06 \uD83D\uDD06 registerInvoiceOffer started ...  \uD83D\uDD06 \uD83D\uDD06 will add 3 accounts");

        InvoiceOfferDTO m = new InvoiceOfferDTO();
        m.setInvoiceId(invoice.getInvoiceId());
        m.setInvestorId(investor.getIdentifier());
        m.setOfferDate(new Date());
        m.setDiscount(random.nextInt(25) * 1.0);
        m.setOfferAmount(invoice.getTotalAmount() * ((100.0 - m.getDiscount()) / 100));
        m.setSupplierId(supplier.getIdentifier());

        TheUtil.startInvoiceOfferFlow(proxy,m);


    }
}

