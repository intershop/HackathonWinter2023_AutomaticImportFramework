/**
 * 
 */
package com.zamro.component.internal.price;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

import javax.inject.Inject;

import com.intershop.beehive.core.capi.common.SystemException;
import com.intershop.beehive.core.capi.currency.Currency;
import com.intershop.beehive.core.capi.domain.Domain;
import com.intershop.beehive.core.capi.domain.DomainMgr;
import com.intershop.beehive.core.capi.user.User;
import com.intershop.beehive.core.internal.util.UUIDGeneratorImpl;
import com.intershop.beehive.foundation.quantity.CurrencyException;
import com.intershop.beehive.foundation.quantity.Money;
import com.intershop.beehive.foundation.quantity.MoneyCalculator;
import com.intershop.beehive.xcs.capi.price.ProductPriceEntry;
import com.intershop.beehive.xcs.capi.price.ProductPriceMapping;
import com.intershop.beehive.xcs.capi.price.ProductPriceMgr;
import com.intershop.beehive.xcs.capi.price.ProductPriceRangeEntry;
import com.intershop.beehive.xcs.capi.product.Product;
import com.intershop.beehive.xcs.capi.product.ProductMgr;
import com.intershop.beehive.xcs.common.ProductRef;
import com.intershop.component.customer.capi.CustomerBO;
import com.intershop.component.customer.segment.capi.provider.CustomerSegmentBOProvider;
import com.intershop.component.pricing.capi.PriceType;
import com.intershop.component.pricing.capi.pricedisplay.PriceDisplayPreferences;
import com.intershop.component.pricing.capi.scale.PriceScaleEntry;
import com.intershop.component.product.capi.ProductBO;
import com.intershop.component.product.pricing.capi.CustomerBOPricingPreferencesExtension;
import com.intershop.component.product.pricing.capi.PriceRangeRecord;
import com.intershop.component.product.pricing.capi.PriceRecordBO;
import com.intershop.component.product.pricing.capi.ProductPriceContext;
import com.intershop.component.product.pricing.capi.UserBOPricingPreferencesExtension;
import com.intershop.component.product.pricing.internal.CustomerSegementCacheIdHelper;
import com.intershop.component.product.pricing.orm.internal.ProductBOPricingExtensionImpl;
import com.intershop.component.repository.capi.BusinessObjectRepositoryContext;
import com.intershop.component.service.capi.service.ServiceConfigurationBORepository;
import com.intershop.component.taxation.capi.SingleItemTaxRecord;
import com.intershop.component.taxation.capi.impl.DefaultTaxationContextImpl;
import com.intershop.component.taxation.capi.product.ProductTaxationService;
import com.intershop.component.taxation.capi.product.impl.ProductTaxationSubjectImpl;
import com.intershop.component.user.capi.UserBO;
import com.intershop.component.user.capi.UserBORepository;

/**
 * @author Jitender Singh Negi
 *
 */
public class ZMRProductBOPricingExtensionImpl extends ProductBOPricingExtensionImpl
{

    private static final User ANONYMOUS_USER = new User((new UUIDGeneratorImpl()).createUUIDString());
    private final MoneyCalculator calculator = MoneyCalculator.createLowPrecisionCalculator();
    @Inject
    private DomainMgr domainMgr;
    @Inject
    private ProductMgr productMgr;
    @Inject
    private ProductPriceMgr productPriceMgr;
    @Inject
    private CustomerSegmentBOProvider customerSegmentBOProvider;
    private ProductTaxationService taxationService;
    
    private BusinessObjectRepositoryContext boRepositoryContext;
    private final ZMRProductBOPricingExtensionImpl.PriceCalculator grossPriceCalculator = new ZMRProductBOPricingExtensionImpl.PriceCalculator() {
        @Override
        public Money calculateValue(Money storedValue, Money vatValue) throws CurrencyException, SystemException {
            return ZMRProductBOPricingExtensionImpl.this.calculator.add(storedValue, vatValue);
        }
    };
    private final ZMRProductBOPricingExtensionImpl.PriceCalculator netPriceCalculator = new ZMRProductBOPricingExtensionImpl.PriceCalculator() {
        @Override
        public Money calculateValue(Money storedValue, Money vatValue) throws CurrencyException, SystemException {
            return ZMRProductBOPricingExtensionImpl.this.calculator.subtract(storedValue, vatValue);
        }
    };
    
    public ZMRProductBOPricingExtensionImpl(String extensionID, ProductBO extendedObject) {
        super(extensionID, extendedObject);
        this.boRepositoryContext = (BusinessObjectRepositoryContext) this.getContext()
                .getVariable("CurrentBusinessObjectRepositoryContext");
    }
    
    @Override
    public PriceRecordBO getPrice(String productPriceType, Currency currency, ProductPriceContext productPriceContext) {
        Objects.requireNonNull(productPriceContext, "The product price context can\'t be null");
        
        PriceDisplayPreferences priceDisplayPreferences = this.getPriceDisplayPreferences(productPriceContext.getCustomerBO());
        PriceType priceDisplayType = priceDisplayPreferences.getPriceDisplayType();
        PriceRecordBO result = this.getProductPrice(priceDisplayType, productPriceType, currency, productPriceContext);
        
        return result;
    }
    
    @Override
    public PriceRecordBO getNetPrice(String productPriceType, Currency currency, ProductPriceContext productPriceContext) {
        return this.getProductPrice(PriceType.NET, productPriceType, currency, productPriceContext);
    }
    
    @Override
    public PriceRecordBO getGrossPrice(String productPriceType, Currency currency, ProductPriceContext productPriceContext) {
        return this.getProductPrice(PriceType.GROSS, productPriceType, currency, productPriceContext);
    }
    
    private PriceDisplayPreferences getPriceDisplayPreferences(CustomerBO customerBO) {
        if(customerBO == null) {
            User pricingPreferencesExtension2 = ANONYMOUS_USER;
            BusinessObjectRepositoryContext repositoryContext = (BusinessObjectRepositoryContext)this.getContext().getVariable("CurrentBusinessObjectRepositoryContext");
            UserBORepository userBORepository = (UserBORepository)repositoryContext.getRepository("UserBORepository");
            UserBO userBO = userBORepository.getUserBOByID(pricingPreferencesExtension2.getID());
            UserBOPricingPreferencesExtension pricingPreferencesExtension1 = (UserBOPricingPreferencesExtension)userBO.getExtension("PricingPreferences");
                return pricingPreferencesExtension1.getPriceDisplayPreferences();
        } else {
            CustomerBOPricingPreferencesExtension pricingPreferencesExtension = (CustomerBOPricingPreferencesExtension)customerBO.getExtension("PricingPreferences");
            return pricingPreferencesExtension.getPriceDisplayPreferences();
}
    }

    private PriceRecordBO getProductPrice(PriceType priceDisplayType, String productPriceType, Currency currency,
                    ProductPriceContext productPriceContext) {
                CustomerBO customerBO = productPriceContext.getCustomerBO();
                User user = customerBO == null ? ANONYMOUS_USER : new User(customerBO.getRepositoryID());
                ProductBO productBO = this.getExtendedObject();
                Domain domain = domainMgr.getDomainByUUID(productBO.getRepository().getRepositoryID());
                ProductRef productRef = new ProductRef(productBO.getSKU(), domain.getDomainName());
                ProductBO contextProductBO = productPriceContext.getContextProductBO();
                ProductRef contextProductRef = contextProductBO == null
                        ? null
                        : new ProductRef(contextProductBO.getSKU(), domain.getDomainName());
                ProductPriceEntry priceEntry = this.getPriceEntry(productRef, contextProductRef, productPriceType, currency,
                        user, customerBO);
                if (!priceEntry.getPrice().isAvailable()) {
                    Product taxRecord = this.productMgr.getProductByProductRef(productRef);
                    if (taxRecord.isDerivedProduct()) {
                        Product result = taxRecord.getBaseProduct();
                        priceEntry = this.getPriceEntry(result.getProductRef(), contextProductRef, productPriceType, currency,
                                user, customerBO);
                    }
                }

                SingleItemTaxRecord taxRecord1 = this.getTaxRecord(this.getExtendedObject(), priceEntry.getPrice(),
                        productPriceContext);
                ZMRPriceRecordBOImpl result1;
                if (priceDisplayType.equals(this.getStoredPriceType())) {
                    PriceRangeRecord priceRangeRecord = null;
                    if (priceEntry instanceof ProductPriceRangeEntry) {
                        ProductPriceRangeEntry priceRangeEntry = (ProductPriceRangeEntry) priceEntry;
                        priceRangeRecord = new PriceRangeRecord(priceRangeEntry.getMinimum().getPrice(),
                                priceRangeEntry.getMaximum().getPrice());
                    }

                    result1 = new ZMRPriceRecordBOImpl(productBO.getID(), this.getContext(),
                            priceDisplayType.equals(PriceType.NET), priceEntry.getPrice(), taxRecord1.getEffectiveTaxRate(),
                            priceRangeRecord);
                    Iterator priceRangeEntry1 = this.getScaleEntries(priceEntry).iterator();

                    while (priceRangeEntry1.hasNext()) {
                        PriceScaleEntry priceScaleEntry = (PriceScaleEntry) priceRangeEntry1.next();
                        result1.addScalePrice(priceScaleEntry.getQuantityLevel(),
                                this.getScalePriceValue(this.getScaleBasePrice(productBO, priceEntry), priceScaleEntry));
                    }
                } else {
                    result1 = this.calculatePrice(productBO, priceDisplayType, taxRecord1, priceEntry, productPriceContext);
                }

                return result1;
            }
    
    private ProductPriceEntry getPriceEntry(ProductRef productRef, ProductRef contextProductRef,
                    String productPriceType, Currency currency, User user, CustomerBO customerBO) {
                ProductPriceMapping priceMapping = new ProductPriceMapping();
                priceMapping.createEntry(productRef);
                Collection customerSegments = this.customerSegmentBOProvider.getCustomerSegmentBOs(customerBO);
                CustomerSegementCacheIdHelper.addCustomerSegmentsToPriceMapping(customerSegments, priceMapping);
                if (contextProductRef != null) {
                    priceMapping.putPriceInfo(productRef, "ContextProduct", contextProductRef);
                }

                priceMapping = this.productPriceMgr.getPrices(user, priceMapping, currency.getMnemonic(), productPriceType);
                ProductPriceEntry priceEntry = priceMapping.getEntry(productRef);
                return priceEntry;
            }
    
    private ZMRPriceRecordBOImpl calculatePrice(ProductBO productBO, PriceType priceDisplayType,
                    SingleItemTaxRecord taxRecord, ProductPriceEntry priceEntry, ProductPriceContext productPriceContext) {
                    ZMRProductBOPricingExtensionImpl.PriceCalculator calculator = priceDisplayType.equals(PriceType.NET)
                        ? this.netPriceCalculator
                        : this.grossPriceCalculator;
                Money value = null;
                if (taxRecord != null) {
                    Money priceRangeRecord = taxRecord.getCalculatedTax();
                    if (priceRangeRecord != null) {
                        value = this.calculateValue(priceEntry.getPrice(), priceRangeRecord, calculator);
                    }
                }

                if (value == null) {
                    value = priceEntry.getPrice();
                }

                PriceRangeRecord priceRangeRecord1 = null;
                Money scaleEntryValue;
                Money scaleEntryCalculatedValue;
                if (priceEntry instanceof ProductPriceRangeEntry) {
                    ProductPriceRangeEntry result = (ProductPriceRangeEntry) priceEntry;
                    Money minPrice = result.getMinimum().getPrice();
                    SingleItemTaxRecord priceScaleEntry = this.getTaxRecord(this.getExtendedObject(), minPrice,
                            productPriceContext);
                    scaleEntryValue = this.calculateValue(minPrice, priceScaleEntry.getCalculatedTax(), calculator);
                    Money scaleEntryTaxRecord = result.getMaximum().getPrice();
                    SingleItemTaxRecord scaleEntryVatValue = this.getTaxRecord(this.getExtendedObject(),
                            scaleEntryTaxRecord, productPriceContext);
                    scaleEntryCalculatedValue = this.calculateValue(scaleEntryTaxRecord, scaleEntryVatValue.getCalculatedTax(),
                            calculator);
                    priceRangeRecord1 = new PriceRangeRecord(scaleEntryValue, scaleEntryCalculatedValue);
                }

                ZMRPriceRecordBOImpl result1 = new ZMRPriceRecordBOImpl(productBO.getID(), this.getContext(),
                        priceDisplayType.equals(PriceType.NET), value, taxRecord.getEffectiveTaxRate(), priceRangeRecord1);
                Iterator minPrice1 = this.getScaleEntries(priceEntry).iterator();

                while (minPrice1.hasNext()) {
                    PriceScaleEntry priceScaleEntry1 = (PriceScaleEntry) minPrice1.next();
                    scaleEntryValue = this.getScalePriceValue(this.getScaleBasePrice(productBO, priceEntry), priceScaleEntry1);
                    SingleItemTaxRecord scaleEntryTaxRecord1 = this.getTaxRecord(this.getExtendedObject(),
                            scaleEntryValue, productPriceContext);
                    Money scaleEntryVatValue1 = scaleEntryTaxRecord1.getCalculatedTax();
                    scaleEntryCalculatedValue = this.calculateValue(scaleEntryValue, scaleEntryVatValue1, calculator);
                    result1.addScalePrice(priceScaleEntry1.getQuantityLevel(), scaleEntryCalculatedValue);
                }

                return result1;
            }
    private interface PriceCalculator {
        Money calculateValue(Money arg0, Money arg1) throws CurrencyException;
    }
    
    private SingleItemTaxRecord getTaxRecord(ProductBO productBO, Money price,
                    ProductPriceContext productPriceContext) {
                DefaultTaxationContextImpl context = new DefaultTaxationContextImpl();
                context.setInvoiceToAddress(productPriceContext.getInvoiceToAddressBO());
                context.setShipToAddress(productPriceContext.getShipToAddressBO());
                context.setShipFromAddress(productPriceContext.getShipFromAddressBO());
                ProductTaxationSubjectImpl taxationSubject = new ProductTaxationSubjectImpl(productBO,
                        productPriceContext.getContextProductBO());
                SingleItemTaxRecord result = this.getTaxationService().getTaxRecord(taxationSubject,
                        price, context);
                return result;
            }
    
    private Money calculateValue(Money storedValue, Money vatValue,
                    ZMRProductBOPricingExtensionImpl.PriceCalculator calculator) {
                try {
                    Money e = calculator.calculateValue(storedValue, vatValue);
                    return e;
                } catch (CurrencyException arg4) {
                    throw new IllegalArgumentException("Currency mismatch.", arg4);
                }
            }
    
    private ProductTaxationService getTaxationService() {
        if (this.taxationService == null) {
            ServiceConfigurationBORepository repository = (ServiceConfigurationBORepository) this.boRepositoryContext
                    .getRepository("ServiceConfigurationBORepository");
            Collection taxationServices = repository.getServiceAdapters(ProductTaxationService.class);
            if (taxationServices.isEmpty()) {
                throw new IllegalStateException(
                        "No taxation service is configured. There should be exactly one taxation service.");
            }

            if (taxationServices.size() > 1) {
                throw new IllegalStateException(
                        "Too many taxation services are configured. There should be exactly one taxation service.");
            }

            this.taxationService = (ProductTaxationService) taxationServices.iterator().next();
        }

        return this.taxationService;
    }
    private PriceType getStoredPriceType() {
        String priceTypeName = this.getDomainConfiguration().getString("PriceType");
        PriceType storedPriceType = priceTypeName == null ? PriceType.NET : PriceType.getPriceType(priceTypeName);
        return storedPriceType;
    }
    private Collection<PriceScaleEntry> getScaleEntries(ProductPriceEntry priceEntry) {
        Map priceListInfoMap = (Map) priceEntry.getPriceInfo("PriceListPriceInfo");
        if (priceListInfoMap == null) {
            return new ArrayList();
        } else {
            Map scalesMap = (Map) priceListInfoMap.get("PriceScales");
            return scalesMap == null ? new ArrayList() : scalesMap.values();
        }
    }
    
    private Money getScalePriceValue(Money salePrice, PriceScaleEntry entry) {
        switch (entry.getTypeCode()) {
            case 1 :
            case 3 :
            case 4 :
                return entry.getSingleBasePrice();
            case 2 :
                double percentage = entry.getPercentage();
                return this.calculator.multiply(salePrice, 1.0D - percentage / 100.0D);
            default :
                return salePrice;
        }
    }
    
    private Money getScaleBasePrice(ProductBO productBO, ProductPriceEntry priceEntry) {
        Money basePrice = productBO.getListPrice(priceEntry.getPrice().getCurrencyMnemonic());
        if (!basePrice.isAvailable()) {
            basePrice = priceEntry.getPrice();
        }

        return basePrice;
    }

}
