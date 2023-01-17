package com.intershop.platform.utils.pipelet.payment;

import java.util.Iterator;

import com.intershop.beehive.bts.capi.orderprocess.fulfillment.Order;
import com.intershop.beehive.bts.capi.payment.PaymentInstrumentInfo;
import com.intershop.beehive.bts.capi.payment.PaymentTransaction;
import com.intershop.beehive.bts.internal.payment.PaymentInstrumentInfoPO;
import com.intershop.beehive.core.capi.pipeline.Pipelet;
import com.intershop.beehive.core.capi.pipeline.PipeletExecutionException;
import com.intershop.beehive.core.capi.pipeline.PipelineDictionary;
import com.intershop.beehive.orm.capi.common.ORMHelper;

/**
 * This pipelets adds the given PaymentTransaction to the Order.
 * 
 * @author aseitz
 * @since 3.1.0
 *
 */
public class AddPaymentTransactionToOrder extends Pipelet
{
    
    public static String DN_ORDER = "Order";
    
    public static String DN_PAYMENT_TRANSACTION = "PaymentTransaction";

    @Override
    public int execute(PipelineDictionary dict) throws PipeletExecutionException
    {
        Order order = (Order)dict.getOptional(DN_ORDER);
        
        PaymentTransaction transaction = (PaymentTransaction)dict.getOptional(DN_PAYMENT_TRANSACTION);
        
        if (order != null && transaction != null) {
            transaction.setOrder(order);
            // Also set lineitemCtnr for the paymentinstrument info, otherwise 
            // the pii will be deleted by the job to remove history baskets!!
            PaymentInstrumentInfo pii = transaction.getPaymentInstrumentInfo(); 
            if (pii != null) {
                // first delete other PIIs, which were created during order creation
                Iterator<PaymentInstrumentInfo> piis = order.createPaymentInstrumentInfoIterator();
                while (piis.hasNext())
                {
                    PaymentInstrumentInfo oldPii = piis.next();
                    ((PaymentInstrumentInfoPO)oldPii).remove();
                }
                ORMHelper.closeIterator(piis);
                pii.setLineItemCtnr(order);
            }
        }
        return PIPELET_NEXT;
    }

}