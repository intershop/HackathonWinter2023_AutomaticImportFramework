package com.intershop.platform.impex.automation.internal.impex;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import com.intershop.beehive.core.internal.impex.CollectXMLStatisticsContentHandler;

/**
 * An extension of {@link CollectXMLStatisticsContentHandler} for enriching
 * statistics with the price list id. Only one price list expected to exist. The
 * price list id is needed for further actions during import process.
 * 
 * @author l.stephan@intershop.com
 * @version 1.0, 2016-04-20
 * @since 3.2.2
 * 
 *        Basic functionality.
 */
public class PriceListXMLStatisticsHandler extends CollectXMLStatisticsContentHandler
{
    private String id = null;
    private static final String TAG_PRODUCT_PRICE_LIST = "product-price-list";
    private static final String PRICELISTID = "PriceListID";

    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException
    {
        super.startElement(uri, localName, qName, attributes);

        // get value of 'id' attribute of 'product-price-list' element
        if (localName.equals(TAG_PRODUCT_PRICE_LIST))
        {
            id = attributes.getValue("id");
            statistics.put(PRICELISTID, id);
        }
    }
}
