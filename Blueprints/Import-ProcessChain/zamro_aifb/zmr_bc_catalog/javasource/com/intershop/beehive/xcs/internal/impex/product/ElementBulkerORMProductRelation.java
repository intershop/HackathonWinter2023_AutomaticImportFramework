package com.intershop.beehive.xcs.internal.impex.product;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.intershop.beehive.core.capi.app.AppContextUtil;
import com.intershop.beehive.core.capi.common.SystemException;
import com.intershop.beehive.core.capi.domain.ApplicationTypeImpl;
import com.intershop.beehive.core.capi.impex.Controller;
import com.intershop.beehive.core.capi.impex.Element;
import com.intershop.beehive.core.capi.impex.ImpexLogger;
import com.intershop.beehive.core.capi.impex.ImportException;
import com.intershop.beehive.core.capi.impex.ImportMode;
import com.intershop.beehive.core.capi.naming.NamingMgr;
import com.intershop.beehive.core.internal.impex.orm.ElementBulkerORM;
import com.intershop.beehive.foundation.quantity.Quantity;
import com.intershop.beehive.orm.capi.common.ORMHelper;
import com.intershop.beehive.xcs.capi.impex.product.extension.ElementBulkerORMProductRelationExtension;
import com.intershop.beehive.xcs.capi.product.Product;
import com.intershop.beehive.xcs.capi.product.ProductConstants;
import com.intershop.beehive.xcs.capi.product.ProductMgr;
import com.intershop.beehive.xcs.capi.productset.ProductSet;
import com.intershop.beehive.xcs.capi.productset.ProductSetMgr;
import com.intershop.beehive.xcs.capi.productvariation.ProductVariationException;
import com.intershop.beehive.xcs.internal.product.BundleAssignmentPO;
import com.intershop.beehive.xcs.internal.product.BundleAssignmentPOFactory;
import com.intershop.beehive.xcs.internal.product.BundleAssignmentPOKey;
import com.intershop.beehive.xcs.internal.product.ProductLinkPO;
import com.intershop.beehive.xcs.internal.product.ProductLinkPOFactory;
import com.intershop.beehive.xcs.internal.product.ProductLinkPositionPO;
import com.intershop.beehive.xcs.internal.product.ProductLinkPositionPOFactory;
import com.intershop.beehive.xcs.internal.product.ProductLinkPositionPOKey;
import com.intershop.beehive.xcs.internal.product.ProductPO;
import com.intershop.beehive.xcs.internal.product.ProductPOFactory;
import com.intershop.beehive.xcs.internal.product.ProductPOKey;
import com.intershop.beehive.xcs.internal.product.TypeCodeDefinitionLRUCache;
import com.intershop.beehive.xcs.internal.productset.ProductSetPO;
import com.intershop.beehive.xcs.internal.productset.ProductSetPOFactory;
import com.intershop.beehive.xcs.internal.productvariation.ProductVariationPO;
import com.intershop.beehive.xcs.internal.productvariation.ProductVariationPOFactory;
import com.intershop.beehive.xcs.internal.productvariation.VariableVariationAttributePO;
import com.intershop.beehive.xcs.internal.productvariation.VariableVariationAttributePOFactory;
import com.intershop.platform.extension.capi.ExtensionPoint;
/**
 * Bulks product links/bundles/variations.
 */
public class ElementBulkerORMProductRelation extends ElementBulkerORM
{
    private ProductPOFactory productPOFactory;
    
    /**
     * ProductVariationPOFactory instance
     */
    private ProductVariationPOFactory variationHome;
    
    /**
     * BundleAssignmentPOFactory instance
     */
    private BundleAssignmentPOFactory bundleHome;
    
    private BundleAssignmentPOFactory bundleAssignmentPOFactory;
    
    private ProductSetPOFactory productSetPOFactory;
    
    private ProductLinkPOFactory productLinkFactory;
    
    private ProductLinkPositionPOFactory prodLinkPositionFactory;
    
    private ProductVariationPOFactory productVariationPOFactory;
            
    private ProductMgr productMgr;
    
    private VariableVariationAttributePOFactory variableVariationAttributePOFactory;
    
    private ProductSetMgr psMgr;
    
    /**
     * Cache for the already processed product masters
     */
    private Set<String> productMasters = Collections.synchronizedSet(new HashSet<String>());
    
    /**
     * Cache for the already processed product bundles and retail sets
     */
    private Set<String> productBundles = Collections.synchronizedSet(new HashSet<String>());

    /**
     * the local instances of the ElementBulkerORMProductRelationExtension implementation
     */
    private List<ElementBulkerORMProductRelationExtension> bulkerProductRelationExtensions;
    
    /**
     * Used to acquire products and make sure only one thread process a variation object at one time.
     */
    private ProductSyncHelper productSync = new ProductSyncHelper();
    
    /**
     * Number of bulker threads. 
     */
    private int bulkerThreadCount = 1;
    
    private static final ThreadLocal<Long> previousBulkTimestamp = new ThreadLocal<Long>() {
        
        @Override
        protected Long initialValue()
        {
            return 0L;
        }
    };
    
    /**
     * default constructor
     */
    public ElementBulkerORMProductRelation()
    {
        // get extension implementation instance assigned to local variable
        this.bulkerProductRelationExtensions = initExtension();
    }
    /**
     * Clean up.
     * 
     * @return True if successful.
     */
    @Override
    public boolean finish()
    {
        TypeCodeDefinitionLRUCache.getInstance().clear();
        
        if (bulkerThreadCount > 1) 
        { 
            productSync.cleanUp();
        }
        
        // finishes the extension implementation
        if(!finishExtension())
            return false;
        return true;
    }

    /**
     * Prepare bulk import.
     * 
     * @return <code>True</code> if successful.
     * @throws ImportException 
     */
    @Override
    public boolean prepare(Controller controller) throws NullPointerException
    {
        variationHome = (ProductVariationPOFactory) NamingMgr.getInstance().lookupFactory(ProductVariationPO.class);
        bundleHome = (BundleAssignmentPOFactory)NamingMgr.getInstance().lookupFactory(BundleAssignmentPO.class);
        bundleAssignmentPOFactory = (BundleAssignmentPOFactory) NamingMgr.getInstance().lookupFactory(BundleAssignmentPO.class);
        productSetPOFactory = (ProductSetPOFactory) NamingMgr.getInstance().lookupFactory(ProductSetPO.class);
        productLinkFactory = (ProductLinkPOFactory)NamingMgr.getInstance().lookupFactory(ProductLinkPO.class);
        prodLinkPositionFactory = (ProductLinkPositionPOFactory)NamingMgr.getInstance().lookupFactory(ProductLinkPositionPO.class);
        productVariationPOFactory = (ProductVariationPOFactory) NamingMgr.getInstance().lookupFactory(ProductVariationPO.class);
        productMgr = NamingMgr.getManager(ProductMgr.class);
        variableVariationAttributePOFactory = (VariableVariationAttributePOFactory) NamingMgr.getInstance().lookupFactory(VariableVariationAttributePO.class);
        psMgr = NamingMgr.getManager(ProductSetMgr.class);
        productPOFactory = (ProductPOFactory) NamingMgr.getInstance().lookupFactory(ProductPO.class);
        
        // limit to single thread only
        bulkerThreadCount = controller.retrieveConfigurationIntValue("Bulker.NumberThreads", controller.getPipeletID(), -1);

        // prepares the extension implementation
        if(!prepareExtension(controller))
            return false;
        
        return super.prepare(controller);
    }
    
    /**
     * The bulker method.
     * 
     * @param element
     *            The element which have to be bulked.
     * @return True if successful.
     */
    @Override
    public boolean bulk(Element element)
    {
        
        ElementProductRelation productRelation = (ElementProductRelation) element;
        
        long currentBulkTimestamp = System.currentTimeMillis();
        long diff = currentBulkTimestamp - previousBulkTimestamp.get();
        previousBulkTimestamp.set(currentBulkTimestamp);
        
        String dbg =  Thread.currentThread().getName() + " :: " + diff + " :: " + productRelation.getSourcePoint().getSku() + " [ ";
        
        // for (ElementProductVariation variation : productRelation.getVariationModel().getProductVariations()) {
        //    dbg += variation.getVariationProductSku() + " ";
        //}
        dbg += productRelation.getVariationModel().getProductVariations().size();
        
        dbg += "]";
        // System.out.println(dbg);
        
        if (!element.getBulkFlag()
                || element.getImportMode() == ImportMode.OMIT
                || element.getImportMode() == ImportMode.DELETE)
        {
            return true; // ignore element
        }

        List<String> acquiredProducts = new ArrayList<>(1);
        
        try
        {
            if (bulkerThreadCount > 1)
            {
                ProductVariationModel variationModel = productRelation.getVariationModel();
                
                if (variationModel != null && !variationModel.getProductVariations().isEmpty())
                {
                    acquiredProducts = variationModel.getProductVariations().stream()
                                    .map(variationProduct -> variationProduct.getVariationProductSku())
                                    .collect(Collectors.toList());
                }
                else
                {
                    List<ElementBundledProduct> bundledProducts = productRelation.getBundledProducts();

                    if(bundledProducts != null && !bundledProducts.isEmpty())
                    {
                        acquiredProducts = bundledProducts.stream()
                                        .map(bundledProduct -> bundledProduct.getBundledProduct().getRelatedProduct().getSKU())
                                        .collect(Collectors.toList());
                    }
                }

                acquiredProducts.add(productRelation.getSourcePoint().getSku());
                
                productSync.acquireProducts(acquiredProducts);
            }
            
            getImportElementRecorder().recordElement(productRelation);
            
            beginTransaction();
            incElementCounter();
            bulkProductLinks(productRelation);
            bulkBundleAssignments(productRelation);
            bulkProductVariationModel(productRelation);
            bulkProductRelationExtension(productRelation);
            
            if (bulkerThreadCount > 1)
            {
                productSync.releaseProducts(acquiredProducts);
                acquiredProducts.clear();
            }
            
            commitTransaction();
        }
        catch (Exception e)
        {
            logger.logMessage(ImpexLogger.LOG_ERROR,
                    "xcs.impex.ElementBulkerSQLLDRProductRelation_E_BULK", e
                            .getMessage(), logger.getStackTrace(e),getImportElementRecorder());
            
            rollbackTransaction();
            
            return false;
        }
        finally
        {
            getImportElementRecorder().removeCurrentRecordedElement();
            
            if (bulkerThreadCount > 1 && !acquiredProducts.isEmpty())
            {
                productSync.releaseProducts(acquiredProducts);    
            }            
        }
        
        return true;
    }

    /**
     * Bulks BundleAssignments. Updates also the type code for product bundle
     * and the bundled products in the type code map.
     * 
     * @param productRelations
     *            The element holding product relations.
     */
    protected void bulkBundleAssignments(ElementProductRelation productRelation)
            throws SystemException
    {
        if (productRelation.getImportMode() == ImportMode.REPLACE)
        {
            if (productRelation.getBundledProducts().isEmpty())
            {
                removeBundledProductAssignments(productRelation);
            }
            else
            {
                Product bundle = productRelation.getSourcePoint().getRelatedProduct();
                // add the UUID and the domainID of the bundle in the cache of
                // already processed bundles and retail sets
                productBundles.add(bundle.getUUID() + "_" + bundle.getDomainID());
            }

            @SuppressWarnings("unchecked")
            Collection<BundleAssignmentPO> bundleAssignments = 
                            bundleAssignmentPOFactory.getObjectsBySQLWhere("bundleid=?", new Object[] { productRelation.getSourcePoint().getID()});
            try
            {
                for (BundleAssignmentPO po : bundleAssignments)
                {
                    boolean isImported = false;
                    for (Object i : productRelation.getBundledProducts())
                    {
                        ElementBundledProduct bundled = (ElementBundledProduct) i;

                        getImportElementRecorder().recordElement(bundled);
                        
                        if (bundled.getBundledProduct().getID().equals(
                                po.getProductID()))
                        {
                            isImported = true;
                            break;
                        }
                        
                        getImportElementRecorder().removeCurrentRecordedElement();
                    }
                    
                    if (!isImported)
                    {
                        // Check if it is part of other retail set
                        Product bundledProduct = po.getProduct();
                        if (!isProductPartOfOtherRetailSets(po.getProductBundle(), bundledProduct))
                        {
                            unsetTypeCode(bundledProduct, ProductConstants.PART_OF_RETAIL_SET);
                        }
                        
                        // Check if it is part of other bundles
                        if (!isProductPartOfOtherBundles(po.getProductBundle(), bundledProduct))
                        {
                            unsetTypeCode(bundledProduct, ProductConstants.BUNDLED);
                        }

                        po.remove();
                    }
                }                
            }
            finally
            {
                ORMHelper.closeCollection(bundleAssignments);
            }
        }

        if(productRelation.getBundledProducts().isEmpty())
        {
            return;
        }
            
        Collection<?> productSets = productSetPOFactory.getObjectsBySQLWhere( "domainid = ? AND "
                        + "(uuid IN (SELECT productsetuuid FROM productsetassignment WHERE productuuid = ?)) AND "
                        + "EXISTS (SELECT 1 FROM productsetdomainassignment WHERE productsetuuid=uuid)",
                        new String[] { productRelation.getSourcePoint().getRelatedProduct().getDomainID(), productRelation.getSourcePoint().getID() });
        try
        {
            for (Object o : productRelation.getBundledProducts())
            {
                ElementBundledProduct bundled = (ElementBundledProduct) o;

                getImportElementRecorder().recordElement(bundled);
                
                BundleAssignmentPO po = bundleAssignmentPOFactory
                        .getObjectByPrimaryKey(new BundleAssignmentPOKey(bundled
                                .getBundledProduct().getID(), productRelation
                                .getSourcePoint().getID()));
                if (po == null)
                {
                    po = bundleAssignmentPOFactory.create(bundled.getBundledProduct().getID(),
                            productRelation.getSourcePoint().getID(),
                            productRelation.getSourcePoint().getRelatedProduct().getDomainID());

                    for (Object ps : productSets)
                    {
                        ProductSetPO productSet = (ProductSetPO)ps;
                        if (!productSet.isInProducts(po.getProduct()))
                        {
                            productSet.addToProducts(po.getProduct());
                        }
                    }
                }
                if (bundled.getQuantity() != null) 
                {
                    po.setQuantity(new Quantity(
                                    bundled.getQuantity(), bundled.getQuantityUnit()));
                }
                    
                if (bundled.getPosition() != null) 
                {
                    po.setPosition(Double
                                    .parseDouble(bundled.getPosition()));
                }
                    
                if (bundled.getOnline() != null) 
                {
                    po.setOnline(bundled.getOnline().booleanValue());
                }

                if (!productRelation.isTypeCodeDefinedBySourceForBundledProducts())
                {
                    int bundleTypeCode = productRelation.getSourcePoint()
                            .getRelatedProduct().getTypeCode();
                    if ((bundleTypeCode & ProductConstants.RETAIL_SET)
                                    == ProductConstants.RETAIL_SET)
                    {
                        // Product in Retail Set
                        setTypeCode(
                                bundled.getBundledProduct().getRelatedProduct(),
                                ProductConstants.PART_OF_RETAIL_SET);
                    }
                    else
                    {
                        // Bundled Product
                        setTypeCode(
                                bundled.getBundledProduct().getRelatedProduct(),
                                ProductConstants.BUNDLED);
                    }
                }
                
                getImportElementRecorder().removeCurrentRecordedElement();
                
            }
            if (!productRelation.isTypeCodeDefinedBySource())
            {
                setTypeCode(productRelation.getSourcePoint().getRelatedProduct(),
                        ProductConstants.BUNDLE);
            }            
        }
        finally
        {
            ORMHelper.closeCollection(productSets);
        }
    }

    /**
     * Bulks product links.
     * 
     * @param productRelations
     *            The element holding product relations.
     */
    protected void bulkProductLinks(ElementProductRelation productRelation)
    {
        if (productRelation.getImportMode() == ImportMode.REPLACE)
        {
            @SuppressWarnings("unchecked")
            Collection<ProductLinkPO> links = productLinkFactory.getObjectsBySQLWhere("sourceid=? and domainid=?", new Object[] {
                            productRelation.getSourcePoint().getID(),
                            productRelation.getSourcePoint().getRelatedProduct().getDomainID() });
            try
            {
                for(ProductLinkPO po : links)
                {
                    boolean isImported = false;
                    for (Object i : productRelation.getProductLinks())
                    {
                        ElementProductLink link = (ElementProductLink) i;
                        
                        getImportElementRecorder().recordElement(link);
                        
                        String sourceid, targetid;
                        if (link.isIncomingLink())
                        {
                            sourceid = link.getTargetPoint().getID();
                            targetid = productRelation.getSourcePoint().getID();
                        }
                        else
                        {
                            sourceid = productRelation.getSourcePoint().getID();
                            targetid = link.getTargetPoint().getID();
                        }

                        if (po.getSourceID().equals(sourceid)
                                && po.getTargetID().equals(targetid)
                                && po.getTypeCode().equals(link.getLinkTypeCodeName()))
                        {
                            isImported = true;
                            break;
                        }
                        
                        getImportElementRecorder().removeCurrentRecordedElement();
                    }
                    
                    if (!isImported)
                    {
                        // We need to remove the product link position too.
                        // It is questionable why the productLinkPO.setPositionNull
                        // does not do that... We will keep this behavior
                        // for backward compatibility.
                        ProductLinkPositionPO plp = prodLinkPositionFactory
                                        .getObjectByPrimaryKey(new ProductLinkPositionPOKey(po.getUUID(), po.getDomainID()));
                        if (plp != null)
                        {
                            plp.remove();
                        }
                        po.remove();
                    }
                }                
            }
            finally
            {
                ORMHelper.closeCollection(links);
            }
        }

        for (ElementProductLink link : productRelation.getProductLinks())
        {
            // avoid importing links with invalid linkTypeCode
            // LinkTypeCodeName is set to 'null' by the validator 
            //   in case no TypeCodeDefinition could be found
            if(link.getLinkTypeCodeName()==null) continue;
            
            getImportElementRecorder().recordElement(link);
            
            String sourceid, targetid;

            if (link.isIncomingLink())
            {
                sourceid = link.getTargetPoint().getID();
                targetid = productRelation.getSourcePoint().getID();
            }
            else
            {
                sourceid = productRelation.getSourcePoint().getID();
                targetid = link.getTargetPoint().getID();
            }

            ProductLinkPO po = link.getProductLinkPO();
            
            if (po == null)
            {
                po = productLinkFactory.create(link.getUUID(), link.getDomainID(), sourceid, targetid,
                            link.getLinkTypeCode());
            }

            po.setLinkedCatalogCategoryID(link.getCategoryID());
            if (link.getQuantity() != null)
            {
                po.setQuantity(new BigDecimal(link.getQuantity()));
            }
            if (link.getPosition() != null) 
            {
                po.setPosition(Double.parseDouble(link.getPosition()));
            }

            bulkCustomAttributes(link, po);
            
            getImportElementRecorder().removeCurrentRecordedElement();
        }
    }

    /**
     * Bulks the variation model.
     * 
     * @param bulkBuffer
     *            The bulk buffer.
     * @param productRelations
     *            The element holding product relations.
     */
    protected void bulkProductVariationModel(
            ElementProductRelation productRelation)
            throws ProductVariationException
    {
        if (productRelation.getVariationModel() == null)
        {
            if (productRelation.getImportMode() == ImportMode.REPLACE)
            {
                setMasteredProductTypeCode(productRelation);
            }
            return;
        }
        
        if (!productRelation.getVariationModel().getMasterProduct().isDerivedProduct())
        {

            bulkProductVariations(productRelation);
            bulkProductVariationVariableAttributes(productRelation);
            // bulkProductVariationValues(variationModel);
            // bulkProductVariationAssignments(variationModel);
    
            if (!productRelation.isTypeCodeDefinedBySource())
            {
                setTypeCode(productRelation.getVariationModel().getMasterProduct(),
                        ProductConstants.MASTER);
            }
        }
    }

    /**
     * Bulks product variations.
     * 
     * @param relateion
     *            The product relation element.
     */
    protected void bulkProductVariations(ElementProductRelation relation)
            throws ProductVariationException
    {
        ProductVariationModel pvm = relation.getVariationModel();
        
        getImportElementRecorder().recordElement(pvm);
   
        if (relation.getImportMode() == ImportMode.REPLACE)
        {
            // add the UUID and the domainID of the master product in the cache of
            // already processed master products
            productMasters.add(pvm.getMasterProduct().getUUID() + "_" + pvm.getMasterProduct().getDomainID());
            
            @SuppressWarnings("unchecked")
            Collection<ProductVariationPO> productVariations = 
                            productVariationPOFactory.getObjectsBySQLWhere("productmasterid=?", new Object[] { pvm.getMasterProduct().getUUID() });

            try
            {
                // remove existing product variations which are no longer part of 
                // master in variation model from database
                for (ProductVariationPO po : productVariations)
                {
                    boolean isImported = false;
                    for (Object i : pvm.getProductVariations())
                    {
                        ElementProductVariation pv = (ElementProductVariation) i;

                        if (po.getProductID().equals(
                                pv.getVariationProduct().getUUID()))
                        {
                            isImported = true;
                            break;
                        }
                    }
                    
                    if (!isImported)
                    {               
                        if (po.getUUID().equals(pvm.getMasterProduct().getDefaultProductVariationID()))
                        {
                            pvm.getMasterProduct().setDefaultProductVariationIDNull(true);
                        }
                        
                        unsetTypeCode(getProductByUUID(po.getProductID()), ProductConstants.MASTERED);
                        po.remove();
                    }
                }                
            }
            finally
            {
                ORMHelper.closeCollection(productVariations);
            }
            
            // remove product variations from database where master in database
            // differs from master in variation model
            for (Object o : relation.getVariationModel().getProductVariations())
            {
                ElementProductVariation pv = (ElementProductVariation) o;
                Iterator<?> i = productVariationPOFactory.getObjectsBySQLWhere("productmasterid<>? AND productid=? AND rownum=1",
                                new Object[] { pvm.getMasterProduct().getUUID(), pv.getVariationProductID() })
                                .iterator();
                try
                {
                    if (i.hasNext())
                    {
                        ProductVariationPO existingPV = (ProductVariationPO)i.next();
                        // if default variation -> set default to null
                        if (existingPV.getUUID().equals(existingPV.getProductMaster().getDefaultProductVariationID()))
                        {
                            existingPV.getProductMaster().setDefaultProductVariationIDNull(true);
                        }
                        existingPV.remove();
                    }
                }
                finally
                {
                    ORMHelper.closeIterator(i);
                }
            }
        }

        for (ElementProductVariation pv : relation.getVariationModel().getProductVariations())
        {
            getImportElementRecorder().recordElement(pv);
            
            Product pvVariationProduct = pv.getVariationProduct();
            
            Iterator<?> i = productVariationPOFactory.getObjectsBySQLWhere(
                    "productmasterid=? AND productid=? AND rownum=1",
                    new Object[] {
                            pvm.getMasterProduct().getUUID(),
                            pvVariationProduct.getUUID()
                            }).iterator();
            ProductVariationPO po;
            ProductPO productMasterPO = getProductByUUID(pv.getMasterID());
            
            try
            {
                if (i.hasNext())
                {
                    po = (ProductVariationPO) i.next();
                }
                else
                {
                    po = productVariationPOFactory.create(pv.getUUID(), pv.getDomainID(),
                                    (ProductPO) pvVariationProduct.getBaseProduct(),
                                    productMasterPO);
                }
            }
            finally
            {
                ORMHelper.closeIterator(i);
            }

            if (pv.getDefault()!=null && pv.getDefault().booleanValue())
            {
                relation.getVariationModel().getMasterProduct().setDefaultProductVariationID(po.getUUID());
            }
            Product variationProduct = productMgr.resolveProductFromID(po.getProductID(), po.getDomain());
            pv.setVariationProduct(variationProduct);
            
            if(pv.getProductVariationPosition() != null)
            {
                variationProduct.setProductVariationPosition(pv.getProductVariationPosition().doubleValue());
            }

            // TODO Why? if (!relation.isTypeCodeDefinedBySource())
            setTypeCode(pvVariationProduct, ProductConstants.MASTERED);
            
            // update product sets for new variation products
            
            Collection<ProductSet> sets = psMgr.getAssignedProductSets(productMasterPO);
            for (ProductSet set: sets)
            {
                if (!set.isInProducts(pvVariationProduct))
                {
                    set.addToProducts(pvVariationProduct);
                }
            }
            
            getImportElementRecorder().removeCurrentRecordedElement();
        }
        
        getImportElementRecorder().removeCurrentRecordedElement();
    }
    
   

    /**
     * Bulks product variation variable attributes.
     * 
     * @param relation
     *            The element product relation.
     */
    protected void bulkProductVariationVariableAttributes(
            ElementProductRelation relation)
    {
        ProductVariationModel pvm = relation.getVariationModel();
        if (relation.getImportMode() == ImportMode.REPLACE)
        {
            @SuppressWarnings("unchecked")
            Collection<VariableVariationAttributePO> variationVariableAttributes = 
                            variableVariationAttributePOFactory.getObjectsBySQLWhere("productmasterid=?", new Object[] { pvm.getMasterProduct().getUUID() });
            try
            {
                for (VariableVariationAttributePO po : variationVariableAttributes)
                {
                    boolean isImported = false;
                    for (Object i : pvm.getVariableVariationAttributes())
                    {
                        ElementVariableVariationAttribute pv = (ElementVariableVariationAttribute) i;

                        if (po.getName().equals(pv.getName()))
                        {
                            isImported = true;
                            break;
                        }
                    }

                    if (!isImported) po.remove();
                }                
            }
            finally
            {
                ORMHelper.closeCollection(variationVariableAttributes);
            }
        }

        for (Object o : pvm.getVariableVariationAttributes())
        {
            ElementVariableVariationAttribute va = (ElementVariableVariationAttribute) o;

            Iterator<?> i = variableVariationAttributePOFactory.getObjectsBySQLWhere("productmasterid=? AND name=?",
                    new Object[] { pvm.getMasterProduct().getUUID(), va.getName() })
                    .iterator();
            
            VariableVariationAttributePO po = null;
            ProductPO productMasterPO = null;
            
            try
            {
                if (va.getMasterID()== null )   /* workaround for #2465 */  
                {
                    va.setMasterID(pvm.getMasterProduct().getUUID());
                    va.setLocalized(false);
                    va.setType(0);
//                                continue;
                }

                productMasterPO = getProductByUUID(va.getMasterID());
                
                if (i.hasNext())
                {
                    po = (VariableVariationAttributePO) i.next();
                }
                else
                {
                    po = variableVariationAttributePOFactory.create(va.getUUID(), va.getDomainID(), va.getName(),
                                    productMasterPO);
                }
            }
            finally
            {
                ORMHelper.closeIterator(i);
            }
           
            po.setLocalized(va.getLocalizedFlag());
            po.setValueType(va.getType());
            po.setProductMaster(productMasterPO);
            if (va.getPosition() != null)
            {
                po.setPosition(Double.parseDouble(va.getPosition()));
            }
            if (va.getPresentationOption() != null)
            {
                po.setPresentationOption(va.getPresentationOption());
            }
            if (va.getPresentationProductAttributeName() != null)
            {
                po.setPresentationProductAttributeName(va.getPresentationProductAttributeName());
            }
            bulkCustomAttributes(va, po);
        }
    }

    private ProductPO getProductByUUID(String uuid)
    {
        ProductPO p = productPOFactory.getObjectByPrimaryKey(new ProductPOKey(uuid));
        if (p == null)
        {
            throw new RuntimeException("Error finding product with uuid '"
                    + uuid + "'!");
        }
        return p;
    }

    /**
     * Sets the type code for the given product in the type code map. This
     * method actually does not modify the products type code. To update the
     * type code, get the type code from the manager and perform an appropriate
     * action to modify the product.
     * 
     * @param product
     *            The product to set the type code for.
     * @param typeCode
     *            The type code, that must be set.
     */

    private void setTypeCode(Product product, int typeCode)
            throws SystemException
    {
        // Take care of original product type code only the first time
        // the type code is being modified.
        int newTypeCode = typeCode;
        if (!product.getTypeCodeNull())
        {
            newTypeCode = product.getTypeCode() | typeCode;
        }

        //
        // Delete item type code for masters and bundles.
        //
        switch (typeCode)
        {
        case ProductConstants.BUNDLE:
        case ProductConstants.MASTER:
            newTypeCode &= ~ProductConstants.ITEM;
            break;
        }
        
        product.setTypeCode(newTypeCode);
    }
    
    /**
     * Unsets the typecode bits according to the given parameter bitmap.
     *  
     * @param product
     *           The product to unset the type code for.
     * @param typeCode
     *           The type code, that must be unset.
     */
    private void unsetTypeCode(Product product, int typeCode)
    {
        int newTypeCode = typeCode;
        if (!product.getTypeCodeNull())
        {
            newTypeCode = product.getTypeCode() | typeCode;
            newTypeCode ^= typeCode;
        }
        product.setTypeCode(newTypeCode);
    }
    
    /**
     * Check "bundledProduct" if it is not part of any product bundles ( that contains  "productBundle")
     * 
     * @param productBundle
     * @param bundledProduct
     */
    private boolean isProductPartOfOtherBundles(Product productBundle, Product bundledProduct)
    {
        @SuppressWarnings("unchecked")
        Iterator<Product> productBundles = productBundle.createProductBundleIterator();

        try
        {
            while (productBundles.hasNext())
            {
                Product oneProductBundle = productBundles.next();
                // Ensuring that it is a product bundle ( not a retail set )
                if (!oneProductBundle.getUUID().equals(productBundle.getUUID()) && oneProductBundle.isProductBundle())
                {
                    return true;
                }
            }
        }
        finally
        {
            ORMHelper.closeIterator(productBundles);
        }
        
        return false;
    }

    /**
     * Check "bundledProduct" if it is not part of any retail set
     * 
     * @param productBundle
     * @param bundledProduct
     */
    private boolean isProductPartOfOtherRetailSets(Product productBundle, Product bundledProduct)
    {
        @SuppressWarnings("unchecked")
        Iterator<Product> productBundles = productBundle.createProductBundleIterator();

        try
        {
            while (productBundles.hasNext())
            {
                Product oneProductBundle = productBundles.next();
                // Ensuring that it is a retail set ( not a product bundle )
                if (!oneProductBundle.getUUID().equals(productBundle.getUUID()) && oneProductBundle.isRetailSet())
                {
                    return true;
                }
            }
        }
        finally
        {
            ORMHelper.closeIterator(productBundles);
        }
        
        return false;
    }
    
    // bulker extension code
    /**
     * returns ElementValidatorProductRelationExtension implementations found
     * @return ElementValidatorProductRelationExtensions or 'null' in case none was found
     */
    protected List<ElementBulkerORMProductRelationExtension> initExtension()
    {
        ApplicationTypeImpl app = (ApplicationTypeImpl)(AppContextUtil.getCurrentAppContext().getApp());
        return app.getJavaExtensions(ElementBulkerORMProductRelationExtension.class, "prepareExtension");
    }

    /**
     * prepares extension
     * @param controller - The Controller
     * @param bulkerProductRelation - The ElementBulkerORMProductRelation instance
     * @return true if successful; false otherwise
     */
    @ExtensionPoint(type = ElementBulkerORMProductRelationExtension.class, id = "prepareExtension")
    public boolean prepareExtension(Controller controller)
    {
        // default value
        boolean status=true;
        // call java extension method if java extension implementation was found
        if(bulkerProductRelationExtensions!=null && !bulkerProductRelationExtensions.isEmpty())
        {
            for (ElementBulkerORMProductRelationExtension bulkerExtension:bulkerProductRelationExtensions)
            {
                if(!bulkerExtension.prepareExtension(controller))
                {
                    status=false;
                    break;
                }
            }
        }
        return status;
    }
    /**
     * Bulks ProductRelationExtension Data.
     * 
     * @param productRelations
     *            The element holding product relations.
     * @throws SystemException if bulker in ProductRelationExtension encountered an error
     */
    @ExtensionPoint(type = ElementBulkerORMProductRelationExtension.class, id = "bulkProductRelationExtension")
    public void bulkProductRelationExtension(ElementProductRelation elementProductRelation)
                    throws SystemException
    {
        // call java extension method if java extension implementation was found
        if(bulkerProductRelationExtensions!=null && !bulkerProductRelationExtensions.isEmpty())
        {
            for (ElementBulkerORMProductRelationExtension bulkerExtension:bulkerProductRelationExtensions)
            {
                // throws a systemException if something goes wrong
                bulkerExtension.bulkProductRelationExtension(elementProductRelation);
            }
        }
    }
    /**
     * does some clean up for extension
     * @return true if successful; false otherwise
     */
    @ExtensionPoint(type = ElementBulkerORMProductRelationExtension.class, id = "finishExtension")
    public boolean finishExtension()
    {
        // default value
        boolean status=true;
        // call java extension method if java extension implementation was found
        if(bulkerProductRelationExtensions!=null && !bulkerProductRelationExtensions.isEmpty())
        {
            for (ElementBulkerORMProductRelationExtension bulkerExtension:bulkerProductRelationExtensions)
            {
                if(!bulkerExtension.finishExtension())
                {
                    status=false;
                    break;
                }
            }
        }
        return status;
    }
    
    /**
     * Removes mastered product variation relations
     * 
     * @param elementProductRelation
     *            the ElementProductRelation
     */
    private void setMasteredProductTypeCode(ElementProductRelation elementProductRelation)
    {
        ProductRelationSourcePoint sourcePoint = elementProductRelation.getSourcePoint();
        @SuppressWarnings("unchecked")
        Iterator<ProductVariationPO> productVariations = variationHome.getObjectsBySQLWhere("productid=? AND domainid=? AND rownum=1",
                        new Object[] { sourcePoint.getID(), sourcePoint.getRelatedProduct().getDomainID() }).iterator();
        try
        {
            if (productVariations.hasNext())
            {
                ProductVariationPO productVariation = productVariations.next();
                if(productVariation.isRemoved())
                {
                    logger.logMessage(ImpexLogger.LOG_ERROR,
                                    "xcs.impex.ElementBulkerORMProductRelation.ProductVariationAlreadyRemoved",
                                    sourcePoint.getSku(), sourcePoint.getRefSku(), sourcePoint.getID(),
                                    sourcePoint.getRelatedProduct().getDomainID());
                    return;
                }
                else
                {
                    Product master = productVariation.getProductMaster();
                    if (!productMasters.contains(master.getUUID() + "_" + master.getDomainID()))
                    {
                        setTypeCode(productVariation.getMasteredProduct(), ProductConstants.MASTERED);
                    }
                }
            }
        }
        finally
        {
           ORMHelper.closeIterator(productVariations);
        }
    }
    
    /**
     * Removes bundled products assignments
     * 
     * @param elementProductRelation
     *            the ElementProductRelation
     */
    private void removeBundledProductAssignments(ElementProductRelation elementProductRelation)
    {
        ProductRelationSourcePoint sourcePoint = elementProductRelation.getSourcePoint();
        
        @SuppressWarnings("unchecked")
        Iterator<BundleAssignmentPO> bundleAssignments = bundleHome.getObjectsBySQLWhere("productid=? AND domainid=?",
                        new Object[] { sourcePoint.getID(), sourcePoint.getRelatedProduct().getDomainID()  }).iterator();
        
        Product bundledProduct = null;
        boolean processedBundleAssignments = false;
        
        try
        {
            while(bundleAssignments.hasNext())
            {
                BundleAssignmentPO bundleAssingment = bundleAssignments.next();
                Product bundle = bundleAssingment.getProductBundle();
                if (bundledProduct == null)
                {
                    bundledProduct = bundleAssingment.getProduct();
                }
                if (!productBundles.contains(bundle.getUUID() + "_" + bundle.getDomainID()))
                {
                    bundleAssingment.remove();
                }
                else
                {
                    if (!processedBundleAssignments)
                    {
                        processedBundleAssignments = true;
                    }
                }
            }            
        }
        finally
        {
            ORMHelper.closeIterator(bundleAssignments);
        }
        
        if (bundledProduct != null && !processedBundleAssignments && bundledProduct.isPartOfRetailSet())
        {
            unsetTypeCode(bundledProduct, ProductConstants.PART_OF_RETAIL_SET);
        }
    }
}
