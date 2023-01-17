/**
 * 
 */
package com.zamro.component.internal.price;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.intershop.beehive.businessobject.capi.BusinessObjectContext;
import com.intershop.beehive.foundation.quantity.Money;
import com.intershop.beehive.foundation.quantity.Quantity;
import com.intershop.component.product.pricing.capi.PriceRangeRecord;
import com.intershop.component.product.pricing.capi.PriceRecordBO;
import com.intershop.component.product.pricing.capi.ScaledPrice;
import com.intershop.component.product.pricing.orm.internal.PriceRecordBOImpl;

/**
 * @author jitender singh negi
 *
 */
public class ZMRPriceRecordBOImpl extends PriceRecordBOImpl
{

    private List<ScaledPrice> scaledPrices;

    public ZMRPriceRecordBOImpl(String id, BusinessObjectContext context, boolean isNet, Money price, BigDecimal taxRate,
                    PriceRangeRecord priceRange)
    {
        super(id,  context,  isNet,  price,  taxRate,priceRange);
    }
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public PriceRecordBO addScalePrice(Quantity quantity, Money price) {
        ScaledPrice newScalePrice = new ScaledPrice(new Quantity(quantity.getValue(),""), price);
        if (this.scaledPrices == null) {
            this.scaledPrices = new LinkedList();
        } else {
            Iterator arg3 = this.scaledPrices.iterator();

            while (arg3.hasNext()) {
                ScaledPrice scalePrice = (ScaledPrice) arg3.next();
                //Commented out to resolve ZMR-3429
                //if (!scalePrice.getQuantity().isOfSameUnit(quantity)) {
                    //throw new IllegalArgumentException("The quantity is of invalid unit. Should be: "
                            //+ scalePrice.getQuantity().getUnit() + " but is:" + quantity.getUnit());
                //}

                if (scalePrice.getQuantity().getValue().equals(quantity.getValue())) {
                    throw new IllegalArgumentException(
                            "The list already contains this quantity: " + quantity.getValue());
                }
            }
        }
        this.scaledPrices.add(newScalePrice);
        Collections.sort(this.scaledPrices);
        return this;
    }
    @Override
    public boolean hasScaledPrices() {
        return this.scaledPrices != null && !this.scaledPrices.isEmpty();
    }
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public List<ScaledPrice> getScaledPrices() {
        List result;
        if (this.scaledPrices == null) {
            result = Collections.emptyList();
        } else {
            result = Collections.unmodifiableList(this.scaledPrices);
        }

        return result;
    }
}
