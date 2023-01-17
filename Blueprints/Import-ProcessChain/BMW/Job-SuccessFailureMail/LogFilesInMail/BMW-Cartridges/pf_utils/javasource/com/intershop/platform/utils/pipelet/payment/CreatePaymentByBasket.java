package com.intershop.platform.utils.pipelet.payment;

import java.util.Collection;
import java.util.Iterator;

import com.intershop.beehive.bts.capi.orderprocess.basket.Basket;
import com.intershop.beehive.bts.capi.payment.PaymentInstrumentInfo;
import com.intershop.beehive.bts.capi.payment.PaymentService;
import com.intershop.beehive.bts.capi.payment.PaymentTransaction;
import com.intershop.beehive.bts.capi.payment.PaymentTransactionConstants;
import com.intershop.beehive.bts.internal.payment.PaymentInstrumentInfoPO;
import com.intershop.beehive.bts.internal.payment.PaymentTransactionPO;
import com.intershop.beehive.bts.internal.payment.PaymentTransactionPOFactory;
import com.intershop.beehive.bts.pipelet.orderprocess.payment.ProcessPaymentBase;
import com.intershop.beehive.core.capi.naming.NamingMgr;
import com.intershop.beehive.core.capi.pipeline.PipeletExecutionException;
import com.intershop.beehive.core.capi.pipeline.PipelineDictionary;
import com.intershop.beehive.foundation.quantity.Money;
import com.intershop.beehive.orm.capi.common.ORMHelper;

public class CreatePaymentByBasket extends ProcessPaymentBase
{
    /**
     * Constant used to access the pipeline dictionary with key 'Basket'
     * <p/>
     * the basket object
     */
    private static final String DN_BASKET = "Basket";
    /**
     * Constant used to access the pipeline dictionary with key 'Description'
     * <p/>
     * the Description of the payment transaction
     */
    public static final String DN_DESCRIPTION = "Description";
    /**
     * Constant used to access the pipeline dictionary with key
     * 'ServiceTransactionID'
     *
     * the ServiceTransactionID of the payment transaction
     */
    public static final String DN_SERVICE_TRANSACTION_ID = "ServiceTransactionID";
    /**
     * Constant used to access the pipeline dictionary with key
     * 'PaymentTransaction'
     *
     * the PaymentTransaction
     */
    public static final String DN_PAYMENT_TRANSACTION = "PaymentTransaction";

    public static final String DN_PAYMENT_INSTRUMENT_INFO = "PaymentInstrumentInfo";
    
    public static final String DN_AMOUNT = "Amount";

    @Override
    public int execute(PipelineDictionary dict) throws PipeletExecutionException
    {
        Basket currentbasket = dict.getRequired(DN_BASKET);

        String description = dict.getOptional(DN_DESCRIPTION);

        String serviceTransactionID = dict.getOptional(DN_SERVICE_TRANSACTION_ID);
        
        String amount = dict.getOptional(DN_AMOUNT);
        
        PaymentInstrumentInfo paymentInstrumentInfo = dict.getOptional(DN_PAYMENT_INSTRUMENT_INFO);

        // While we have more placed orders to process
        PaymentTransactionPOFactory ptFactory = (PaymentTransactionPOFactory)NamingMgr.getInstance().lookupFactory(
                        PaymentTransactionPO.class);
        if (currentbasket != null)
        {
            PaymentTransaction pt = null;
            
            Money trxAmount = null;
            if (amount != null && amount.trim().length() > 0) {
                trxAmount = new Money(currentbasket.getGrandTotalGrossPricePC().getCurrencyMnemonic(), amount);
            }

            if (paymentInstrumentInfo != null)
            {
                pt = createPaymentTransaction(paymentInstrumentInfo, currentbasket, serviceTransactionID, description, ptFactory, trxAmount);
                dict.put(DN_PAYMENT_TRANSACTION, pt);
            }
            else
            {
                // Get selected PaymentMethod from Order
                Iterator it = currentbasket.createPaymentInstrumentInfoIterator();
                while(it.hasNext())
                {
                    pt = createPaymentTransaction((PaymentInstrumentInfo)it.next(), currentbasket, serviceTransactionID, description, ptFactory, trxAmount);
                    dict.put(DN_PAYMENT_TRANSACTION, pt);
                }
                ORMHelper.closeIterator(it);
            }
        }
        return PIPELET_NEXT;
    }

    private PaymentTransaction createPaymentTransaction(PaymentInstrumentInfo pii, Basket currentBasket,
                    String serviceTransactionID, String description, PaymentTransactionPOFactory ptFactory, Money trxAmount)
    {
        PaymentInstrumentInfoPO info = (PaymentInstrumentInfoPO)pii;

        Money amount = info.getCalculatedPaymentAmount();

        if (!amount.isAvailable())
        {
            amount = currentBasket.getGrandTotalGrossPricePC();
        }

        PaymentService ps = info.getPaymentService();
        amount = convertMoney(amount, ps);

        // trxamount is always used if given
        if (trxAmount != null) {
            amount = convertMoney(trxAmount, ps);
        }

        if (null == amount)
        {
            return null;
        }

        PaymentTransaction transaction = null;

        // if PaymentTransaction already exists, use it again!
        Collection<PaymentTransaction> pts = ptFactory.getObjectsBySQLWhere(
                        "orderid = ? and amountvalue = ? and status = ?", new Object[] { currentBasket.getUUID(),
                                        amount.getValue(), PaymentTransactionConstants.PT_CREATED });

        if (pts != null)
        {
            for (PaymentTransaction pt : pts)
            {
                transaction = pt;
            }
            ORMHelper.closeCollection(pts);
        }

        if (transaction == null)
        {
            transaction = ptFactory.create(info.getDomain(), info);
        }

        if (description != null)
        {
            transaction.setDescription(description);
        }

        if (serviceTransactionID != null)
        {
            transaction.setServiceTransactionID(serviceTransactionID);
        }

        transaction.setOrderID(currentBasket.getUUID());

        transaction.setStatus(PaymentTransactionConstants.PT_CREATED);

        // set the amount
        // if we have no payment service, this amount is not
        // converted -
        // but this doesn't matter in this case...
        transaction.setAmount(amount);

        Money zero_amount = Money.getZeroMoney(amount.getCurrencyMnemonic());
        transaction.setAmountCaptured(zero_amount);
        transaction.setAmountPaid(zero_amount);

        transaction.setPaymentPayer(currentBasket.getUser());
        transaction.setPaymentPayeeID(currentBasket.getDomainID());

        // make sure the PaymentTransaction is stored, to query it
        // later
        ((PaymentTransactionPO)transaction).store();
        // add current Basket
        // ((PaymentBasket)transaction.getPaymentService()).setCurrentBasket(currentbasket);
        
        return transaction;
    }
}
