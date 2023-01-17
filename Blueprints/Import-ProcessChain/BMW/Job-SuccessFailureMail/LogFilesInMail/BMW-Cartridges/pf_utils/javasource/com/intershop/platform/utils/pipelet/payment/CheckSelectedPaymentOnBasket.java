package com.intershop.platform.utils.pipelet.payment;

import java.util.Iterator;

import com.intershop.beehive.bts.capi.orderprocess.basket.Basket;
import com.intershop.beehive.bts.capi.payment.PaymentInstrumentInfo;
import com.intershop.beehive.core.capi.pipeline.Pipelet;
import com.intershop.beehive.core.capi.pipeline.PipeletExecutionException;
import com.intershop.beehive.core.capi.pipeline.PipelineDictionary;

/**
 * Checks if the basket contains any payment method with the given
 * PaymentServiceID. If so, it returns true, otherwise false.
 * 
 * @author aseitz
 *
 */
public class CheckSelectedPaymentOnBasket extends Pipelet
{
    protected String DN_PAYMENT_SERVICE_ID = "PaymentServiceId";
    protected String DN_BASKET = "Basket";

    @Override
    public int execute(PipelineDictionary dict) throws PipeletExecutionException
    {

        Basket basket = dict.getRequired(DN_BASKET);
        String serviceId = dict.getRequired(DN_PAYMENT_SERVICE_ID);

        Iterator it = basket.createPaymentInstrumentInfoIterator();
        while(it.hasNext())
        {
            PaymentInstrumentInfo pii = (PaymentInstrumentInfo)it.next();
            if (pii.getPaymentService().getID().equals(serviceId))
            {
                return PIPELET_NEXT;
            }
        }
        return PIPELET_ERROR;
    }
}
