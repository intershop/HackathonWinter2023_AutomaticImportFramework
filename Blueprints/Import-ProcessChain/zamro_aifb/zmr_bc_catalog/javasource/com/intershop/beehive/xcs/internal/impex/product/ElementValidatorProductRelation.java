package com.intershop.beehive.xcs.internal.impex.product;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.intershop.beehive.core.capi.app.AppContextUtil;
import com.intershop.beehive.core.capi.common.Factory;
import com.intershop.beehive.core.capi.common.FinderException;
import com.intershop.beehive.core.capi.common.SystemException;
import com.intershop.beehive.core.capi.domain.ApplicationTypeImpl;
import com.intershop.beehive.core.capi.domain.AttributeDefinitionConstants;
import com.intershop.beehive.core.capi.domain.AttributeValue;
import com.intershop.beehive.core.capi.domain.Domain;
import com.intershop.beehive.core.capi.domain.ExtensibleObject;
import com.intershop.beehive.core.capi.impex.Controller;
import com.intershop.beehive.core.capi.impex.Element;
import com.intershop.beehive.core.capi.impex.ElementValidator;
import com.intershop.beehive.core.capi.impex.ImpexLogger;
import com.intershop.beehive.core.capi.impex.ImportException;
import com.intershop.beehive.core.capi.impex.ImportMode;
import com.intershop.beehive.core.capi.impex.PreferenceConstants;
import com.intershop.beehive.core.capi.impex.XMLLocalizedElement;
import com.intershop.beehive.core.capi.log.Logger;
import com.intershop.beehive.core.capi.naming.NamingMgr;
import com.intershop.beehive.core.internal.impex.ElementValidatorCustomAttributes;
import com.intershop.beehive.orm.capi.common.ORMHelper;
import com.intershop.beehive.xcs.capi.catalog.CatalogCategory;
import com.intershop.beehive.xcs.capi.impex.product.extension.ElementValidatorProductRelationExtension;
import com.intershop.beehive.xcs.capi.product.Product;
import com.intershop.beehive.xcs.capi.product.ProductLink;
import com.intershop.beehive.xcs.capi.productvariation.ProductVariation;
import com.intershop.beehive.xcs.capi.productvariation.ProductVariationValue;
import com.intershop.beehive.xcs.capi.productvariation.VariableVariationAttribute;
import com.intershop.beehive.xcs.internal.impex.common.ImportPermissionMgr;
import com.intershop.beehive.xcs.internal.product.BundleAssignmentPO;
import com.intershop.beehive.xcs.internal.product.BundleAssignmentPOFactory;
import com.intershop.beehive.xcs.internal.product.ProductLinkPO;
import com.intershop.beehive.xcs.internal.product.ProductLinkPOFactory;
import com.intershop.beehive.xcs.internal.product.ProductLinkPositionPO;
import com.intershop.beehive.xcs.internal.product.ProductLinkPositionPOFactory;
import com.intershop.beehive.xcs.internal.productvariation.ProductVariationAssignment;
import com.intershop.beehive.xcs.internal.productvariation.ProductVariationAssignmentPO;
import com.intershop.beehive.xcs.internal.productvariation.ProductVariationAssignmentPOFactory;
import com.intershop.beehive.xcs.internal.productvariation.ProductVariationAssignmentPOKey;
import com.intershop.beehive.xcs.internal.productvariation.ProductVariationPO;
import com.intershop.beehive.xcs.internal.productvariation.ProductVariationPOFactory;
import com.intershop.beehive.xcs.internal.productvariation.ProductVariationValuePO;
import com.intershop.beehive.xcs.internal.productvariation.ProductVariationValuePOAttributeValuePO;
import com.intershop.beehive.xcs.internal.productvariation.ProductVariationValuePOAttributeValuePOFactory;
import com.intershop.component.foundation.capi.typecode.TypeCodeDefinition;
import com.intershop.component.foundation.capi.typecode.TypeCodeRegistry;
import com.intershop.platform.extension.capi.ExtensionPoint;

/**
 *  Validates product links, bundles and variations.
 */
public class ElementValidatorProductRelation extends ElementValidator
{
    /**
     *  Default quantity for a bundled.
     */

    protected static final String DEFAULT_BUNDLED_QTY = "1.0";

    /**
     *  The import product manager.
     */

    private ImportProductMgr importProductMgr;

    /**
     *  The import permission manager.
     */

    protected ImportPermissionMgr importPermissionMgr;

    /**
     *  The product variation assignment factory.
     */

    private ProductVariationAssignmentPOFactory productVariationAssignmentHome;

    /**
     *  The current domain.
     */

    private Domain currentDomain;

    /**
     *  The validator for custom attributes 
     */
    protected ElementValidatorCustomAttributes elementValidatorCustomAttributes = new ElementValidatorCustomAttributes();
    
    /**
     *  Used to find product link instances.
     */

    private ProductLinkPOFactory plHome;

    /**
     *  Used to find product link position instances.
     */

    private ProductLinkPositionPOFactory plpHome;

    /**
     *  Used to find bundled assignment instances.
     */

    private BundleAssignmentPOFactory baHome;

    /**
     *  Variation factory.
     */

    private ProductVariationPOFactory variationHome;
    
    private TypeCodeRegistry typeCodeRegistry;

    /**
     *  Variation value av factory.
     */

    private ProductVariationValuePOAttributeValuePOFactory variationValueAVHome;

    /**
     *  Storage for SKUs used in variation product references (needed for
     *  uniqueness check, see bug #1473). Note: synchronize access to this
     *  attribute.
     */

    private Set<String> variationProductSKUs;

    /**
     * Ignore new on update?
     */
    
    private boolean ignoreNewOnUpdate;
    /**
     * local instances of ElementValidatorProductRelationExtension (Java-Extension methods)
     */
    protected List<ElementValidatorProductRelationExtension> validatorProductRelationExtensions;
    
    /**
     * default constructor
     */
    public ElementValidatorProductRelation()
    {
        // local List of java extension implementations; null if none found
        this.validatorProductRelationExtensions = initExtension();
    }
    /**
     *  This is the preparer for the validator.
     *
     *  @param  aController     The Impex controller object.
     *  @return True if successful.
     */

    @Override
    public boolean prepare(Controller aController)
    {
        if (!super.prepare(aController))
        {
            return false;
        }
        
        NamingMgr registry = NamingMgr.getInstance();
        elementValidatorCustomAttributes.prepare(aController);

        currentDomain = getDomainByName(controller.getDomainName());

        importProductMgr = new ImportProductMgr(currentDomain);

        importPermissionMgr = new ImportPermissionMgr(controller.getImpexAuthority(), currentDomain);
        
        // get the TypeCodeRegistry from the registry
        typeCodeRegistry = registry.getManager(TypeCodeRegistry.class);        

        plHome = (ProductLinkPOFactory) registry.lookupFactory(ProductLinkPO.class);
        plpHome = (ProductLinkPositionPOFactory) registry.lookupFactory(ProductLinkPositionPO.class);
        baHome = (BundleAssignmentPOFactory) registry.lookupFactory(BundleAssignmentPO.class);
        variationValueAVHome = (ProductVariationValuePOAttributeValuePOFactory) registry.lookupFactory(ProductVariationValuePOAttributeValuePO.class);
        productVariationAssignmentHome =
            (ProductVariationAssignmentPOFactory) NamingMgr.getInstance().lookupFactory(ProductVariationAssignmentPO.class);
        variationHome = (ProductVariationPOFactory) NamingMgr.getInstance().lookupFactory(ProductVariationPO.class);

        variationProductSKUs = new HashSet<>(100000);

        ignoreNewOnUpdate = controller.getPreferences().getPreferenceAsBoolean(PreferenceConstants.PREF_UPDATE_IGNORE_NEW, Boolean.FALSE).booleanValue();

        // prepare Validator Product Relation Extension
        if(!prepareExtension(aController))
            return false;
        
        return true;
    }

    /**
     * is a additional import mode - retrieved from a property<br>
     * 'updates' existing data but 'ignores' new data <br>
     * it's default value is 'false'
     * 
     * @return the ignoreNewOnUpdate
     */
    public boolean isIgnoreNewOnUpdate()
    {
        return ignoreNewOnUpdate;
    }
    
    /**
     *  This is the element validator.
     *
     *  @param  validateElement     The element which have to be validated.
     *  @return True if element is valid.
     */
    @Override
    public boolean isValid (Element validateElement)
    {
        ElementProductRelation productRelation = (ElementProductRelation) validateElement;

        int importMode = productRelation.getImportMode();
        
        boolean validReturnValue=true;
        
        try
        {
            getImportElementRecorder().recordElement(productRelation);
            
            switch (importMode)
            {
                case ImportMode.INITIAL:
                    validReturnValue = isValidForInitialMode(productRelation);
                    if(!this.isValidExtension(productRelation))
                    {
                        validReturnValue=false;
                    }
                    return validReturnValue;

                case ImportMode.IGNORE:
                    validReturnValue = isValidForIgnoreMode(productRelation);
                    if(!this.isValidExtension(productRelation))
                    {
                        validReturnValue=false;
                    }
                    return validReturnValue;

                case ImportMode.REPLACE:
                    validReturnValue = isValidForReplaceMode(productRelation);
                    if(!this.isValidExtension(productRelation))
                    {
                        validReturnValue=false;
                    }
                    return validReturnValue;

                case ImportMode.UPDATE:
                    validReturnValue = isValidForUpdateMode(productRelation);
                    if(!this.isValidExtension(productRelation))
                    {
                        validReturnValue=false;
                    }
                    return validReturnValue;

                case ImportMode.DELETE:
                {
                    // Ignore (relations should be already deleted).
                    // TODO check for Extension objects
                    return true;
                }

                case ImportMode.OMIT:
                    return true;

                default:
                    logger.logMessage(ImpexLogger.LOG_ERROR, "xcs.impex.ElementValidatorProductRelation_E_IMPORTMODE", String.valueOf(importMode), getImportElementRecorder());
                    break;
            }
            
        }
        catch (Exception e)
        {
            logger.logMessage(ImpexLogger.LOG_ERROR, "xcs.impex.ElementValidatorProductRelation_E_VALIDATE", e.getMessage(), logger.getStackTrace(e),getImportElementRecorder());
        }
        finally
        {
            getImportElementRecorder().removeCurrentRecordedElement();
        }

        return false;
    }

    /**
     *  Validates the product relation in initial mode.
     *
     *  @param  productRelation     The product relation element.
     *  @return True if product relation is valid.

     */

    protected boolean isValidForInitialMode(ElementProductRelation productRelation)
        throws SystemException
    {
        ProductRelationSourcePoint source = productRelation.getSourcePoint();
        
        boolean isSourcePointResolved = resolveSourcePoint(source);
        if (!isSourcePointResolved)
        {
            logUnknownSource(source);
            return false;
        }
        
        if (hasInvalidTypeCombinations(productRelation, isSourcePointResolved)) {
            return false;
        }

        //
        //  Links.
        //

        if(!validateProductLinks(productRelation))
        {
            return false;
        }
        if (!isRelationProductLinksAttributesValid(productRelation)){
            return false;
        }
        //
        //  Bundles.
        //

        validateBundles(productRelation);

        //
        //  Variations.
        //

        if (! validateVariationModel(productRelation)){
            return false;
        }
        if (!isRelationVariableVariationAttributesAttributesValid(productRelation)){
            return false;
        }
        
        return true;
    }

    /**
     *  Validates the product relation in ignore mode.
     *
     *  @param  productRelation     The product relation element.
     *  @return True if product relation is valid.

     */

    protected boolean isValidForIgnoreMode(ElementProductRelation productRelation)
        throws SystemException
    {
        ProductRelationSourcePoint source = productRelation.getSourcePoint();

        boolean isSourcePointResolved = resolveSourcePoint(source);
        if (!isSourcePointResolved)
        {
            logUnknownSource(source);
            return false;
        }
        
        if (hasInvalidTypeCombinations(productRelation, isSourcePointResolved)) {
            return false;
        }

        //
        //  Links.
        //

        if(!validateProductLinks(productRelation))
        {
            return false;
        }
        if (!isRelationProductLinksAttributesValid(productRelation)){
            return false;
        }

        //
        //  Bundles.
        //

        validateBundles(productRelation);

        //
        //  Variations.
        //

        if (! validateVariationModel(productRelation)){
            return false;
        }
        if (!isRelationVariableVariationAttributesAttributesValid(productRelation)){
            return false;
        }
        
        // filter existing product links
        filterExistingProductLinks(productRelation);
        // filter existing bundle assignments
        filterExistingBundles(productRelation);
        // filter existing variations
        filterExistingProductVariations(productRelation);
        
        return true;
    }

    /**
     *  Validates the product relation in replace mode.
     *
     *  @param  productRelation     The product relation element.
     *  @return True if product relation is valid.
     */

    protected boolean isValidForReplaceMode(ElementProductRelation productRelation)
        throws SystemException
    {
        ProductRelationSourcePoint source = productRelation.getSourcePoint();
        
        boolean isSourcePointResolved = resolveSourcePoint(source);
        if (!isSourcePointResolved)
        {
            logUnknownSource(source);
            return false;
        }
        
        if (hasInvalidTypeCombinations(productRelation, isSourcePointResolved)) {
            return false;
        }
        
        //
        //  Links.
        //

        if(!validateProductLinks(productRelation))
        {
            return false;
        }
        if (!isRelationProductLinksAttributesValid(productRelation)){
            return false;
        }

        //
        //  Bundles.
        //

        validateBundles(productRelation);

        //
        //  Variations.
        //

        if (! validateVariationModelForReplace(productRelation)){
            return false;
        }
        
        if (!isRelationVariableVariationAttributesAttributesValid(productRelation)){
            return false;
        }
        
        
        return true;

    }

    /**
     *  Validates the product relation in update mode.
     *
     *  @param  productRelation     The product relation element.
     *  @return True if product relation is valid.
     */

    protected boolean isValidForUpdateMode(ElementProductRelation productRelation)
        throws SystemException
    {
        ProductRelationSourcePoint source = productRelation.getSourcePoint();

        boolean isSourcePointResolved = resolveSourcePoint(source);
        if (!isSourcePointResolved)
        {
            if (ignoreNewOnUpdate)
            {
                productRelation.setBulkFlag(false);
                return true;
            }
            
            logUnknownSource(source);
            return false;
        }
        
        if (hasInvalidTypeCombinations(productRelation, isSourcePointResolved)) {
            return false;
        }
        
        //
        //  Links.
        //
        if(!validateProductLinks(productRelation))
        {
            return false;
        }
        updateProductLinks(productRelation);
        if (!isRelationProductLinksAttributesValid(productRelation)){
            return false;
        }



        //
        //  Bundles.
        //
        validateBundles(productRelation);
        updateBundledProducts(productRelation);


        //
        //  Variations.
        //
        if (! validateVariationModelForUpdate(productRelation)){
            return false;
        }
        if (!isRelationVariableVariationAttributesAttributesValid(productRelation)){
            return false;
        }
        return true;

    }

    /**
     *  Validates product links. This method does not check for existing links.
     *  Illegal links will be removed from the link list and logged.
     *
     *  @param  source  The source (used for referencing in log messages.)
     *  @param  links   A product link list.
     */

    protected boolean validateProductLinks(ElementProductRelation productRelation)
        throws SystemException
    {
        ProductRelationSourcePoint source = productRelation.getSourcePoint();
        List<ElementProductLink> links  = productRelation.getProductLinks();
        Set<String> keys = new HashSet<>();

        for (int i = 0, numLinks = links.size(); i < numLinks; i++)
        {
            ElementProductLink link = links.get(i);
            ProductRelationEndPoint target = link.getTargetPoint();

            // set domainID of current importing Domain
            link.setDomainID(currentDomain.getUUID());
            resolveLinkType(source.getSku() != null ? source.getSku() : source.getRefSku(), link);
            String key = target.getRefSku() + "@" + 
                target.getRefDomainName() + "@" + link.getLinkTypeCodeName();
            
            if((source.getSku() != null && source.getSku().equals(target.getRefSku())) || 
                            (source.getRefSku() != null && source.getRefSku().equals(target.getRefSku())))
            {
                logger.logMessage(ImpexLogger.LOG_ERROR,
                    "xcs.impex.ElementValidatorProductRelation_E_PRODUCT_LINKS_TO_SELF",
                    new Object[] {target.getRefSku(), target.getRefDomainName(), 
                        link.getLinkTypeCode(),getImportElementRecorder()});
                return false;
            }
            
            if (keys.contains(key))
            {
                logger.logMessage(ImpexLogger.LOG_WARN,
                    "xcs.impex.ElementValidatorProductRelation_DuplicateProductLink",
                    new Object[] {source.getSku(), target.getRefDomainName(), 
                        target.getRefSku(), link.getLinkTypeCode()});
                links.remove(i);
                numLinks--;
                i--;
                continue;
            }

            if (resolveProductLink(source, link))
            {
                complementPersistentObjectAttributes(link, link.getProductLinkPO());
                replicateProductAttributes(link);
                keys.add(key);
            }
            else
            {
                links.remove(i);
                numLinks--;
                i--;
            }
        }
        return true;
    }

    protected void updateProductLinks(ElementProductRelation productRelation)
        throws SystemException
    {
        ProductRelationSourcePoint source = productRelation.getSourcePoint();
        List<ElementProductLink> links  = productRelation.getProductLinks();

        for (int i = 0, numLinks = links.size(); i < numLinks; i++)
        {
            ElementProductLink link = links.get(i);

            link.setRemoveFromDatabase(true);

            resolveLinkType(source.getSku() != null ? source.getSku() : source.getRefSku(), link);

            updateProductLink(link);
        }
    }


    /**
     *  Updates the import link object from a corresponding product link (the
     *  source). Sets only the oca if source is null.
     *
     *  @param  link        The link to resolve.
     *  @param  source      The already existent product link.
     */

    protected void updateProductLink(ElementProductLink link)
    {
        ProductLinkPO productLinkPO = link.getProductLinkPO();

        if (productLinkPO == null)
        {
            link.setOCA(0);
            return;
        }

        if (link.getCategoryID() == null)
        {
            link.setCategoryID(productLinkPO.getLinkedCatalogCategoryID());
        }
        if (link.getLinkType() == null && link.getLinkTypeCodeName() == null)
        {
            // use only the (new) string typeCode of the link, defined by a TypeCodeDefinition:Name
            // do NOT use the (old) integer type code
            link.setLinkTypeCodeName(productLinkPO.getTypeCode());
        }
        if (!productLinkPO.getOcaNull())
        {
            link.setOCA(productLinkPO.getOca() + 1);
        }
        if (link.getQuantity() == null)
        {
            link.setQuantity(productLinkPO.getQuantity().toPlainString());
        }
        else
        {
            link.setOCA(0);
        }
    }

    /**
     *  Validates product bundles. This method does not check for existing bundles.
     *  Illegal bundles will be removed from the bundle list and logged.
     *
     *  @param  source   A product relation source point (used to reference the source in log messages.)
     *  @param  bundle   A product bundle list.
     */

    protected void validateBundles(ElementProductRelation productRelation)
        throws SystemException
    {
        ProductRelationSourcePoint source = productRelation.getSourcePoint();
        List<ElementBundledProduct> bundle = productRelation.getBundledProducts();

        for (int i = 0, numBundled = bundle.size(); i < numBundled; i++)
        {
            ElementBundledProduct bundled = bundle.get(i);

            if (resolveBundledTarget(source, bundled.getBundledProduct()))
            {
                checkBundledQuantity(bundled);
                checkBundledPosition(bundled);
                complementBundledAttributes(bundled, null);

                setBundledProductDefaults(bundled);
            }
            else
            {
                bundle.remove(i);
                numBundled--;
                i--;
            }
        }
    }



    /**
     *  Sets not yet set product bundled attributes. 
     *
     *  @param  bundled   bundled products to set default values if not yet set.
     */

    protected void setBundledProductDefaults(ElementBundledProduct bundled)
        throws SystemException
    {

        if (bundled.getOnline() == null)
        {
            bundled.setOnline(true);
        }
    }
    

    /**
     *  Validates product bundles. This method checks for existing bundles and
     *  updates bundles accordingly.
     *  Illegal bundles will be removed from the bundle list and logged.
     *
     *  @param  source          A product relation source point (used to reference the source in log messages.)
     *  @param  bundle          A product bundle list.
     *  @param  existingBundles An enumeration of existing product bundles of
     *                          the product.
     */

    protected void updateBundledProducts(ElementProductRelation productRelation)
        throws SystemException
    {
        ProductRelationSourcePoint source = productRelation.getSourcePoint();
        List bundle = productRelation.getBundledProducts();

        ElementProductRelationBundleAssignments bundleAssignments = new ElementProductRelationBundleAssignments();

        try
        {
            Enumeration existingBundles = baHome.findBySQLWhere(
                "bundleid=?", new String[]{source.getID()});
            bundleAssignments.init(existingBundles);
        }
        catch (FinderException e)
        {
            logger.logMessage(ImpexLogger.LOG_ERROR, "xcs.impex.ElementValidatorProductRelation_E_BUNDLEASSIGNMENTS_CNG", e.getMessage(),getImportElementRecorder());
            return;
        }


        for (int i = 0, numBundled = bundle.size(); i < numBundled; i++)
        {
            ElementBundledProduct bundled = (ElementBundledProduct) bundle.get(i);

            if (resolveBundledTarget(source, bundled.getBundledProduct()))
            {
                checkBundledQuantity(bundled);
                checkBundledPosition(bundled);

                BundleAssignmentPO exAssignment = bundleAssignments.getBundleAssignment(bundled);

                if (exAssignment != null)
                {
                    bundled.setRemoveFromDatabase(true);
                }

                complementBundledAttributes(bundled, exAssignment);
            }
            else
            {
                bundle.remove(i);
                numBundled--;
                i--;
            }
        }
    }

    /**
     *  Resolves variation products. The corresponding variation
     *  products must exist and must be of the right type.
     *
     *  @param  model   The variation model.
     *  @param  master  The variation master.
     *  @return True if ok.
     */

    protected boolean resolveProductVariations(ProductVariationModel model,
                                               ProductRelationSourcePoint master)
        throws SystemException
    {
        List<ElementProductVariation> variations = model.getProductVariations();

        for (ElementProductVariation pv :variations)
        {
            if (!resolveProductVariation(pv, master))
            {
                return false;
            }
        }
        
        return true;
    }

    /**
     *  Checks product variations for uniqueness. This should be done after all
     *  variations are resolved and complemented.
     *  A variation product must not be used for more than one variation.
     *  Method logs errors.
     *  Since we must check database in update mode, a flag
     *  <code>checkDatabase</code> is passed to the method.
     *
     *  @param  variationModel  The variation model.
     *  @param  checkDatabase   Check database too?
     *  @return True, if variations are unique.
     */

    protected synchronized boolean checkUniquenessProductVariations(ProductVariationModel variationModel,
                                                                    boolean checkDatabase)
        throws SystemException
    {
        List<ElementProductVariation> variations = variationModel.getProductVariations();
        String       masterID   = variationModel.getMasterProduct().getUUID();

        for (int i = 0, l = variations.size(); i < l; i++)
        {
            ElementProductVariation variation = variations.get(i);

            String productSKU = variation.getVariationProductSku();

            if (variationProductSKUs.contains(productSKU))
            {
                logger.logMessage(ImpexLogger.LOG_ERROR, "xcs.impex.Variation_NotUniqueVariationProduct", productSKU, variationModel.getMasterProduct().getSKU(),getImportElementRecorder());
                return false;
            }

            if (checkDatabase)
            {
                try
                {
                    Enumeration variationBeans = variationHome.findBySQLWhere(
                        "productid=?", new String[]{variation.getVariationProductID()});

                    while (variationBeans.hasMoreElements())
                    {
                        ProductVariation variationBean = (ProductVariation) variationBeans.nextElement();

                        if (!masterID.equals(variationBean.getProductMasterID()))
                        {
                            //  A variation for another master exists with the variation product.

                            logger.logMessage(ImpexLogger.LOG_ERROR, "xcs.impex.Variation_NotUniqueVariationProduct", productSKU, variationModel.getMasterProduct().getSKU(),getImportElementRecorder());
                            return false;
                        }
                    }
                }
                catch (FinderException e)
                {
                    Logger.debug(this, e.getMessage(), e);
                }
            }
        }

        //  OK, store variation product SKUs.

        for (int i = 0, l = variations.size(); i < l; i++)
        {
            variationProductSKUs.add(variations.get(i).getVariationProductSku());
        }
        
        return true;
    }
    
    /**
     *  Resolves a variation product. Looks up the product by sku and domain
     *  name (if given). Complements the product variation by setting
     *  the corresponding variation product and the master id.
     *
     *  @param  productVariation   The product variation element.
     *  @param  master             The variation master.
     *  @return True if ok, false, if the variation product could not be found.
     */

    protected boolean resolveProductVariation(ElementProductVariation productVariation,
                                              ProductRelationSourcePoint master)
        throws SystemException
    {
        String sku = productVariation.getVariationProductSku();
        Double productVariationPosition = productVariation.getProductVariationPosition();

        if (sku == null)
        {
            logger.logMessage(ImpexLogger.LOG_ERROR,
                             "xcs.impex.ElementValidatorProductRelation_E_MASTERSKU_NS",
                             master.getSku(),getImportElementRecorder());
            return false;
        }

        Product variationProduct = null;

        String  domainName = productVariation.getDomainName();

        if (domainName != null)
        {
            Domain domain = getDomainByName(domainName);

            if (domain == null)
            {
                logger.logMessage(ImpexLogger.LOG_ERROR, "xcs.impex.ElementValidatorIllegalDomainName",
                                  "product variation", sku, domainName,getImportElementRecorder());
                return false;
            }

            variationProduct = importProductMgr.getProductBySKU(sku, domain);
        }
        else
        {
            variationProduct = importProductMgr.getProductBySKU(sku);
        }

        if (variationProduct == null)
        {
            logger.logMessage(ImpexLogger.LOG_ERROR,
                              "xcs.impex.ElementValidatorProductRelation_E_VARIATIONSKU_NF",
                              sku,getImportElementRecorder());
            return false;
        }

        if (!isValidVariationProductType(variationProduct))
        {
            return false;
        }

        //only if current user has the permission on this target product
        if (importPermissionMgr.userHasViewPermissions(variationProduct))
        {
            productVariation.setVariationProduct(variationProduct);
            productVariation.setMasterID(master.getID());
            productVariation.setProductVariationPosition(productVariationPosition);
        }
        else
        {
            logger.logMessage(ImpexLogger.LOG_ERROR,
                              "xcs.impex.ElementValidatorProductRelation_E_VARIATION_DENIED",
                              sku, domainName,getImportElementRecorder());
            return false;
        }

        return true;
    }

    /**
     *  Checks the type code for a variation product. Always returns true
     *  for this validator.
     *
     *  @param  variationProduct    The variation product to check the type code for.
     *  @return True, if product has the right type for a variation product.
     */

    protected boolean isValidVariationProductType(Product variationProduct)
        throws SystemException
    {
        return true;
    }

    /**
     *  Sets the default variation id for the model.
     *  Precondition: All product variations must be resolved and complete.
     *
     *  @param  variationModel  The variation model.

     */

    protected void setDefaultVariation(ProductVariationModel variationModel)
    {
        List variations = variationModel.getProductVariations();

        boolean defaultSet = false;

        for (int i = 0, l = variations.size(); i < l && !defaultSet; i++)
        {
            ElementProductVariation pv = (ElementProductVariation) variations.get(i);

            if (Boolean.TRUE.equals(pv.getDefault()))
            {
                variationModel.setDefaultVariationID(pv.getUUID());
                defaultSet = true;
            }
        }

        if (!defaultSet)
        {
            variationModel.setDefaultVariationID(((ElementProductVariation) variations.get(0)).getUUID());
        }
    }

    /**
     *  Resolves variable variation attributes. Each variation product must have
     *  the variation attributes, attributes must be of same type. Stores
     *  the attribute value iterator at the product variation element for later
     *  usage.
     */

    protected boolean resolveVariableVariationAttributes(
                List<ElementVariableVariationAttribute> variationAttributes,
                List<ElementProductVariation> variationProducts,
                ProductRelationSourcePoint master)
        throws SystemException
    {
        //
        //  Prepare.
        //

        Map<String, ElementVariableVariationAttribute> attributesByName = new HashMap<>();

        List<String> attributeNames = new ArrayList<>(variationAttributes.size());

        Map<String, AttributeInfo> attributeInfos = new HashMap<>();

        for (int i = 0, l = variationAttributes.size(); i < l; i++)
        {
            ElementVariableVariationAttribute variationAttribute = variationAttributes.get(i);

            String name = variationAttribute.getName();

            if (name == null || name.length() == 0)
            {
                logger.logMessage(ImpexLogger.LOG_ERROR, "xcs.impex.ElementValidatorProductRelation_E_VA_NAME_NS", master.getSku(),getImportElementRecorder());
                return false;
            }

            attributeNames.add(name);

            attributesByName.put(name, variationAttribute);

            attributeInfos.put(name, new AttributeInfo());

            checkVVAPosition(variationAttribute);
        }

        //
        //  Check all products.
        //

        Set<String> productAttributes = new HashSet<>();

        for (int idxProduct = 0, l = variationProducts.size(); idxProduct < l; idxProduct++)
        {
            ElementProductVariation pv = variationProducts.get(idxProduct);

            productAttributes.clear();

            Iterator<AttributeValue> attributes = pv.getVariationProduct().createAttributeValuesIterator();
            List<AttributeValue> attributeValues = new ArrayList<>(20); 
            while (attributes.hasNext())
            {
                AttributeValue av = attributes.next();
                
                if(!attributeNames.contains(av.getName())) continue;

                attributeValues.add(av);
                productAttributes.add(av.getName());
                
                AttributeInfo attributeInfo = attributeInfos.get(av.getName());

                if (!attributeInfo.initialized)
                {
                    attributeInfo.type        = av.getType();
                    attributeInfo.isLocalized = av.isLocalized();
                    attributeInfo.initialized = true;

                    ElementVariableVariationAttribute variationAttribute = attributesByName.get(av.getName());

                    variationAttribute.setType(attributeInfo.type);
                    variationAttribute.setLocalized(attributeInfo.isLocalized);
                    variationAttribute.setMasterID(master.getID());
                }
                else
                {
                    if (av.getType() != attributeInfo.type)
                    {
                        logger.logMessage(ImpexLogger.LOG_WARN, "xcs.impex.ElementValidatorProductRelation_E_VA_TYPE_ILL", av.getName());
                    }
                    if (av.isLocalized() != attributeInfo.isLocalized)
                    {
                        logger.logMessage(ImpexLogger.LOG_WARN, "xcs.impex.ElementValidatorProductRelation_E_VA_LOC_ILL", av.getName());
                    }
                }
            }

            if (productAttributes.size() != variationAttributes.size())
            {
                logger.logMessage(ImpexLogger.LOG_WARN, "core.impex.Exception", "Product with sku '" + pv.getVariationProduct().getSKU() + "' does not provide all attributes for variation.");
//                attributes = null;
//                return false;
            }

            pv.setAttributeValueIterator(new AttributeValueIterator(attributeValues.toArray(new AttributeValue[0])));
        }

        
        return true;
    }

    /**
     *  Helper to store attribute value characteristics.
     */

    private class AttributeInfo
    {
        public int type = AttributeDefinitionConstants.ATTRIBUTE_TYPE_NONE;
        public boolean isLocalized = false;
        public boolean initialized = false;
    }

    /**
     *  Creates all variation values and assignments.
     *
     *  @param  model   The variation model.
     */

    protected void createVariationValuesAndAssignments(ProductVariationModel model)
        throws SystemException
    {
        List<ElementProductVariation> productVariations   = model.getProductVariations();
        List<ElementVariableVariationAttribute> variationAttributes = model.getVariableVariationAttributes();

        for (int i = 0, l = productVariations.size(); i < l; i++)
        {
            ElementProductVariation productVariationElement = productVariations.get(i);

            AttributeValueIterator masteredBeanAVIterator = productVariationElement.getAttributeValueIterator();

            for (int ii = 0, ll = variationAttributes.size(); ii < ll; ii++)
            {
                ElementVariableVariationAttribute variableVariationAttribute = variationAttributes.get(ii);

                //
                //  Look for a product variation value element, matching the products attributes.
                //

                AttributeValueWrapper masteredAttributeValue = getAttributeValueForAttributeName(masteredBeanAVIterator, variableVariationAttribute.getName());

                ElementProductVariationValue productVariationValueElement = getElementProductVariationValue(model, masteredAttributeValue);

                if (productVariationValueElement != null)
                {
                    //  Found. Create only an assignment.

                    ElementProductVariationAssignment productVariationAssignmentElement =
                        createProductVariationAssignment(productVariationValueElement.getUUID(), productVariationElement.getUUID());

                    model.addProductVariationAssignment(productVariationAssignmentElement);

                    continue;
                }

                //
                //  Create a new variation value.
                //

                productVariationValueElement = new ElementProductVariationValue();

                productVariationValueElement.setDomainID(model.getMasterProduct().getDomainID());
                complementPersistentObjectAttributes(productVariationValueElement, null);

                List attributeValues = masteredAttributeValue.attributeValues;

                for (int iii = 0, lll = attributeValues.size(); iii < lll; iii++)
                {
                    productVariationValueElement.addAttributeValue((AttributeValue) attributeValues.get(iii));
                }

                productVariationValueElement.setVariableVariationAttributeID(variableVariationAttribute.getUUID());
                model.addProductVariationValue(productVariationValueElement);

                ElementProductVariationAssignment productVariationAssignmentElement = createProductVariationAssignment(productVariationValueElement.getUUID(), productVariationElement.getUUID());

                model.addProductVariationAssignment(productVariationAssignmentElement);
            }
        }
    }

    /**
     *  Complements product variations (used in update mode).
     *  <ul>
     *      <li>Adds existing, but not in source listed
     *          variations to the models variations list.
     *      <li>Complements all listed variations either from existing
     *          variations or by creating new (persistent object -) attributes.
     *  </ul>
     *  Preconditions: All variation products read from the source must
     *  be resolved before.
     *
     *  @param  model   The variation model.
     */

    protected void complementProductVariations(ProductVariationModel model)
        throws SystemException
    {
        Product master = model.getMasterProduct();

        Iterator<ProductVariation> existingProductVariations = master.createProductVariationsIterator();
        try
        {
            while (existingProductVariations.hasNext())
            {
                ProductVariation existingProductVariation = existingProductVariations.next();

                ElementProductVariation elemPV = model.getProductVariationByVariationProductID(existingProductVariation.getProductID());
                if (elemPV != null)
                {
                    complementPersistentObjectAttributes(elemPV, existingProductVariation);
                }
                else
                {
                    elemPV = new ElementProductVariation();

                    elemPV.setMasterID(master.getUUID());
                    elemPV.setVariationProduct(existingProductVariation.getMasteredProduct());
                    elemPV.setDefault(existingProductVariation.equals(
                                    existingProductVariation.getProductMaster().getDefaultProductVariation()));

                    complementPersistentObjectAttributes(elemPV, existingProductVariation);

                    model.addProductVariation(elemPV);
                }
            }
        }
        finally
        {
            ORMHelper.closeIterator(existingProductVariations);
        }


        //
        //  Complement all product variations not processed till now.
        //
        List<ElementProductVariation> productVariations = model.getProductVariations();
        String masterDomainID = master.getDomainID();

        for (int i = 0, l = productVariations.size(); i < l; i++)
        {
            ElementProductVariation pv =
                        productVariations.get(i);

            if (pv.getUUID() == null)
            {
                pv.setDomainID(masterDomainID);
                complementPersistentObjectAttributes(pv, null);
            }
        }
    }

    /**
     *  Complements variable variation attributes. Adds existing but not listed
     *  attributes to the models attribute list, complements listed attributes
     *  from existing attributes or complements with newly created
     *  (persistent object -) attributes.
     *
     *  @param  model   The variation model.
     */

    protected void complementVariationAttributes(ProductVariationModel model)
        throws SystemException
    {
        Product master = model.getMasterProduct();

        Iterator<VariableVariationAttribute> exAttributes = master.createVariableVariationAttributesIterator();
        try
        {
            List<ElementVariableVariationAttribute> attributes = model.getVariableVariationAttributes();
    
            while (exAttributes.hasNext())
            {
                VariableVariationAttribute exAttribute = exAttributes.next();
    
                boolean attributeProcessed = false;
    
                for (int i = 0, l = attributes.size(); i < l && !attributeProcessed; i++)
                {
                    ElementVariableVariationAttribute va = attributes.get(i);
    
                    if (va.getName().equals(exAttribute.getName()))
                    {
                        complementPersistentObjectAttributes(va, exAttribute);
                        complementEOAttributes(va, exAttribute, DBTableVariableVariationAttribute.getInstance());
                        va.setPOObject(exAttribute);
                        attributeProcessed = true;
                    }
                }
    
                if (!attributeProcessed)
                {
                    // Add to the variation attributes list.
    
                    ElementVariableVariationAttribute va = new ElementVariableVariationAttribute();
    
                    va.setMasterID(master.getUUID());
                    va.setName(exAttribute.getName());
                    va.setLocalized(exAttribute.isLocalized());
                    va.setType(exAttribute.getValueType());
    
                    complementPersistentObjectAttributes(va, exAttribute);
                    complementEOAttributes(va, exAttribute, DBTableVariableVariationAttribute.getInstance());
    
                    model.addVariableVariationAttribute(va);
                }
            }
    
            // Complement all not processed.
    
            for (int i = 0, l = attributes.size(); i < l; i++)
            {
                ElementVariableVariationAttribute va = attributes.get(i);
    
                if (va.getUUID() == null)
                {
                    complementPersistentObjectAttributes(va, null);
                }
            }
        }
        finally
        {
            ORMHelper.closeIterator(exAttributes);
        }
    }

    /**
     *  Complements product variation values and assignments from existing
     *  or creates new for not existing values.
     *
     *  @param  model   The variation model.
     */

    protected void complementVariationValuesAndAssignments(ProductVariationModel model)
        throws SystemException
    {
        List<ElementProductVariation> productVariations   = model.getProductVariations();
        List<ElementVariableVariationAttribute> variationAttributes = model.getVariableVariationAttributes();

        for (int i = 0, l = productVariations.size(); i < l; i++)
        {
            ElementProductVariation productVariationElement = productVariations.get(i);
            Product masteredBean = productVariationElement.getVariationProduct();

            AttributeValueIterator masteredBeanAVIterator = createAttributeValueIterator(masteredBean);

            for (int ii = 0, ll = variationAttributes.size(); ii < ll; ii++)
            {
                ElementVariableVariationAttribute variableVariationAttribute = variationAttributes.get(ii);

                //
                //  Look for a product variation value element, matching the products attributes.
                //

                AttributeValueWrapper masteredAttributeValue = getAttributeValueForAttributeName(masteredBeanAVIterator, variableVariationAttribute.getName());

                ElementProductVariationValue productVariationValueElement = getElementProductVariationValue(model, masteredAttributeValue);

                if (productVariationValueElement != null)
                {
                    //  Found. Create only an assignment (from existing or new).

                    ElementProductVariationAssignment productVariationAssignmentElement = null;

                    ProductVariationAssignment assignment = getProductVariationAssignment(productVariationValueElement.getUUID(), productVariationElement.getUUID());

                    if (assignment != null)
                    {
                        productVariationAssignmentElement = createProductVariationAssignment(assignment);
                    }
                    else
                    {
                        productVariationAssignmentElement = createProductVariationAssignment(productVariationValueElement.getUUID(), productVariationElement.getUUID());
                    }

                    model.addProductVariationAssignment(productVariationAssignmentElement);

                    continue;
                }

                //
                //  Find a product variation value matching the products attributes. If found create
                //  an element from existing, else from scratch.
                //

                ProductVariationValuePO variationValueBean = null;

                VariableVariationAttribute variableVariationAttributeBean = (VariableVariationAttribute) variableVariationAttribute.getPOObject();

                if (variableVariationAttributeBean != null)
                {
                    variationValueBean = (ProductVariationValuePO)getProductVariationValue(masteredBeanAVIterator, variableVariationAttributeBean);
                }

                productVariationValueElement = new ElementProductVariationValue();
                ElementProductVariationAssignment productVariationAssignmentElement = null;

                complementPersistentObjectAttributes(productVariationValueElement, variationValueBean);

                if (variationValueBean != null)
                {
                    try
                    {
                        Iterator<AttributeValue> attributeValues = ImportProductMgr.createAttributeValueIterator(variationValueAVHome, variationValueBean.getUUID());

                        while (attributeValues.hasNext())
                        {
                            AttributeValue attributeValue = attributeValues.next();

                            if (!attributeValue.getName().equals(variableVariationAttribute.getName()))
                            {
                                productVariationValueElement.addAttributeValue(attributeValue);
                            }
                        }

                        attributeValues = null;
                    }
                    catch (FinderException e)
                    {
                        Logger.debug(this, e.getMessage(), e);
                    }

                    ProductVariationAssignment assignment = getProductVariationAssignment(variationValueBean.getUUID(), productVariationElement.getUUID());

                    if (assignment != null)
                    {
                        productVariationAssignmentElement = createProductVariationAssignment(assignment);
                    }
                }
                else
                {
                    productVariationValueElement.setDomainID(model.getMasterProduct().getDomainID());
                }

                List<AttributeValue> attributeValues = masteredAttributeValue.attributeValues;

                for (int iii = 0, lll = attributeValues.size(); iii < lll; iii++)
                {
                    productVariationValueElement.addAttributeValue(attributeValues.get(iii));
                }

                productVariationValueElement.setVariableVariationAttributeID(variableVariationAttribute.getUUID());
                model.addProductVariationValue(productVariationValueElement);

                if (productVariationAssignmentElement == null)
                {
                    productVariationAssignmentElement = createProductVariationAssignment(productVariationValueElement.getUUID(), productVariationElement.getUUID());
                }

                model.addProductVariationAssignment(productVariationAssignmentElement);
            }

            masteredBeanAVIterator.discard();
            masteredBeanAVIterator = null;
        }
    }

    private AttributeValueIterator createAttributeValueIterator(Product product)
    {
        Iterator<AttributeValue> attributes = product.createAttributeValuesIterator();
        List<AttributeValue> attributeValues = new ArrayList<>(20);
        
        try
        {
            while (attributes.hasNext())
            {
                attributeValues.add(attributes.next());
            }
        }
        finally
        {
            ORMHelper.closeIterator(attributes);
        }
        
        return new AttributeValueIterator(attributeValues.toArray(new AttributeValue[0]));
    }

    /**
     *  Returns an product variation value element matching the passed attribute
     *  value (wrapper).
     *
     *  @param  model           The variation model.
     *  @param  attributeValue  The attribute value for the variation value to find.
     *  @return The product variation value element, null if no element with the
     *          given attribute exists.
     */

    private ElementProductVariationValue getElementProductVariationValue(ProductVariationModel model, AttributeValueWrapper attributeValue)
        throws SystemException
    {
        List<ElementProductVariationValue> productVariationValueElements = model.getProductVariationValues();

        for (int i = 0, l = productVariationValueElements.size(); i < l; i++)
        {
            AttributeValueWrapper variationValueAttributeValue = null;

            ElementProductVariationValue productVariationValueElement = productVariationValueElements.get(i);

            List<AttributeValue> variationValueAttributeValues = productVariationValueElement.getAttributeValues();

            for (int ii = 0, ll = variationValueAttributeValues.size(); ii < ll; ii++)
            {
                AttributeValue variationValueAttributeValueBean = variationValueAttributeValues.get(ii);

                if (variationValueAttributeValueBean.getName().equals(attributeValue.attributeName))
                {
                    if (variationValueAttributeValue == null)
                    {
                        variationValueAttributeValue = new AttributeValueWrapper(attributeValue.attributeName);
                    }

                    variationValueAttributeValue.attributeValues.add(variationValueAttributeValueBean);
                }
            }

            if (variationValueAttributeValue != null && variationValueAttributeValue.equals(attributeValue))
            {
                return productVariationValueElement;
            }
        }
        
        return null;
    }

    /**
     *  Creates a variation assignment for the passed variation and variation
     *  value id.
     *
     *  @param  productVariationValueID The product variation value id.
     *  @param  productVariationID      The product variation id.
     *  @return The created variation assignment.
     */

    protected ElementProductVariationAssignment createProductVariationAssignment(String productVariationValueID,
                                                                                 String productVariationID)
    {
        ElementProductVariationAssignment variationAssignment = new ElementProductVariationAssignment();

        variationAssignment.setProductVariationID(productVariationID);
        variationAssignment.setProductVariationValueID(productVariationValueID);
        variationAssignment.setOca(0);

        return variationAssignment;
    }

    /**
     *  Creates an elemenzt product variation assignment from an assignment bean.
     *
     *  @param  variationAssignment The assignment bean.
     *  @return The assignment element.
     */

    protected ElementProductVariationAssignment createProductVariationAssignment(ProductVariationAssignment variationAssignment)
        throws SystemException
    {
        ElementProductVariationAssignment variationAssignmentElement = new ElementProductVariationAssignment();

        variationAssignmentElement.setProductVariationID(variationAssignment.getProductVariationID());
        variationAssignmentElement.setProductVariationValueID(variationAssignment.getProductVariationValueID());

        if (variationAssignment.getOcaNull())
        {
            variationAssignmentElement.setOca(0);
        }
        else
        {
            variationAssignmentElement.setOca(variationAssignment.getOca() + 1);
        }

        return variationAssignmentElement;
    }

    /**
     *  Gets a product variation assignment by the specified primary key
     *  attributes. Returns null, if no assignment for the key exists.
     *
     *  @param  productVariationValueID The product variation value key.
     *  @param  productVariationID      The product variation key.
     *  @return The product variation assignment or null if no assignment exists.
     */

    protected ProductVariationAssignment getProductVariationAssignment(String productVariationValueID,
                                                                       String productVariationID)
    {
        return productVariationAssignmentHome.
            getObjectByPrimaryKey(new ProductVariationAssignmentPOKey(productVariationValueID, productVariationID));
    }

    /**
     *  Returns a product variation value for the mastered product and variation
     *  attribute.
     *
     *  @param  masteredProduct     The mastered product to get the value for.
     *  @param  variationAttribute  The required variation attribute.
     *  @return The product variation value or null if no exists.
     */

    private ProductVariationValue getProductVariationValue(AttributeValueIterator masteredProductAVIterator, VariableVariationAttribute variationAttribute)
        throws SystemException
    {
        AttributeValueWrapper attributeValueMasteredProduct = getAttributeValueForAttributeName(masteredProductAVIterator, variationAttribute.getName());

        if (attributeValueMasteredProduct == null)
        {
            return null;
        }

        Iterator<ProductVariationValue> productVariationValues = variationAttribute.createProductVariationValuesIterator();
        
        try
        {
            while (productVariationValues.hasNext())
            {
                ProductVariationValue productVariationValue = productVariationValues.next();
    
                AttributeValueWrapper attributeValueVariationValue = getAttributeValueForAttributeName(variationValueAVHome, productVariationValue.getUUID(), variationAttribute.getName());
    
                if (attributeValueVariationValue != null &&  attributeValueVariationValue.equals(attributeValueMasteredProduct))
                {
                    return productVariationValue;
                }
            }
        }
        finally
        {
            ORMHelper.closeIterator(productVariationValues);
        }

        return null;
    }

    /**
     *  Validates the variation model in <b>all</b> modes but update and replace mode. All
     *  modes are handled identically since the variation
     *  model is completely removed from the database, i.e. preconditions are
     *  always the same.
     *  The relations source point must be validated by the caller.
     *
     *  @param  relation    The product relation object.
     *  @return True if model is valid. If not valid a matching error message
     *          is printed and false is returned.
     */

    protected boolean validateVariationModel(ElementProductRelation relation)
        throws SystemException
    {
        System.out.println(getClass().getName() + "validateVariationModel(ElementProductRelation)");
        
        return createVariationModel(relation);
    }

    /**
     * Validates the variation model in replace mode.
     * 
     * @param relation The product relation object
     * @return true if the model is valid.
     * @throws SystemException
     */
    protected boolean validateVariationModelForReplace(ElementProductRelation relation)
                    throws SystemException
    {
        String dbg = "validateVariationModelForReplacE | " + relation.getSourcePoint().getSku() + " | " +   // TODO fdpo
        relation.getVariationModel().getProductVariations().size() + " | ";                                 // TODO fdpo
        long t0 = System.currentTimeMillis();                                                               // TODO fdpo
        
        if (relation.getVariationModel() == null)
        {
            System.out.println("#");
            
            //Variation model not created during parsing 
            //because no variations are described in the source.
            //If the replaced product has variations they must be deleted
            //To enable deleting create variation model with empty variation list.

            //resolve imported product
            ProductRelationSourcePoint master = relation.getSourcePoint();
            Product product                   = master.getRelatedProduct();
            Iterator masteredProductsIterator = product.createMasteredProductsIterator();

            try
            {
                //check if the product has variations        
                if (masteredProductsIterator.hasNext())
                {
                    //if yes create VariationModel for given relation and leave the variation list empty
                    ElementProductRelationVariation variationData = relation.getVariationElement();
                    
                    variationData.setImportMode(ImportMode.REPLACE);            
                    
                    relation.setVariationModel(variationData.getVariationModel());
                    
                    relation.getVariationModel().setMasterProduct(product);
                    relation.getVariationModel().setRemoveFromDatabase(true);            
                    
                    relation.setTypeCodeDefinedBySource(true);

                    return true;
                }
            }
            finally
            {
                ORMHelper.closeIterator(masteredProductsIterator);
            }
        }

        // --
        long t1 = System.currentTimeMillis();                                                               // TODO fdpo
        dbg += t1 - t0 + " | ";                                                                             // TODO fdpo

        //if variation model is created during parsing
        //or the replaced product has no variations
        //call the default validation.
        boolean valid = validateVariationModel(relation);
        
        // --
        dbg += System.currentTimeMillis() - t1;                                                             // TODO fdpo
        System.out.println(dbg);                                                                            // TODO fdpo

        return valid;
    }
    
    /**
     *  Validates the variation model in update mode.
     *
     *  @param  relation    The product relation object.
     *  @return True if model is valid. If not valid a matching error message
     *          is printed and false is returned.
     */

    protected boolean validateVariationModelForUpdate(ElementProductRelation relation)
        throws SystemException
    {
        ProductVariationModel variationModel = relation.getVariationModel();

        if (variationModel != null)
        {
            ProductRelationSourcePoint master = relation.getSourcePoint();
            Product product                   = master.getRelatedProduct();
                variationModel.setRemoveFromDatabase(true);
            variationModel.setMasterProduct(product);

            if (!resolveProductVariations(variationModel, master))
            {
                return false;
            }
            
            //checks if the variations/master are shared and logs some messages.
            checkVariationSharing(variationModel);

            complementProductVariations(variationModel);

            if (!checkUniquenessProductVariations(variationModel, true))
            {
                return false;
            }

            setDefaultVariation(variationModel);

            //  Products ok, evaluate attributes.

            List<ElementVariableVariationAttribute> attributes = variationModel.getVariableVariationAttributes();

            if (!resolveVariableVariationAttributes(attributes, variationModel.getProductVariations(), master))
            {
                return false;
            }

            complementVariationAttributes(variationModel);
            complementVariationValuesAndAssignments(variationModel);
            
            @SuppressWarnings("unchecked")
            Iterator<ElementVariableVariationAttribute> masterVariationAttrs = variationModel
                                                                              .getVariableVariationAttributes().iterator();
            
            if (masterVariationAttrs != null)
            {
                @SuppressWarnings("unchecked")
                List<ElementProductVariation> elementsProductVariations = variationModel.getProductVariations();                
                removeVariationAttrsWithInconsistentType(masterVariationAttrs, elementsProductVariations);
            }
        }

        return true;
    }    
    
    private List<Product> getVariationProducts(List<ElementProductVariation> elementsProductVariations)
    {
        List<Product> variationProducts = new LinkedList<>();
        for(ElementProductVariation elementProductVariation : elementsProductVariations)
        {
            Product variationProduct = elementProductVariation.getVariationProduct();
            variationProducts.add(variationProduct);
        }
        
        return variationProducts;
    }
    
    private void removeVariationAttrsWithInconsistentType(Iterator<ElementVariableVariationAttribute> variationAttrs
                 , List<ElementProductVariation> elementsProductVariations)
    {
        List<Product> variationProducts = getVariationProducts(elementsProductVariations);
        
        while(variationAttrs.hasNext())
        {
            ElementVariableVariationAttribute elementVariableVariationAttribute = variationAttrs.next();
            String variationAttrName = elementVariableVariationAttribute.getName();
            int variationAttrType = elementVariableVariationAttribute.getType();
            if (!areVariationAttributesConsistent(variationProducts, variationAttrName, variationAttrType))
            {
                variationAttrs.remove();
            }
        }
    }        
    
    private boolean areVariationAttributesConsistent(List<Product> variations, String variationAttributeName
                    , int expectedVariationAttributeType)
    {
        for(Product variation : variations)
        {
            Object variationtAttribute = variation.getAttribute(variationAttributeName);
            if(variationtAttribute != null)
            {
                int variationtAttributeType = variation.getAttributeType(variationAttributeName);                

                if(expectedVariationAttributeType != variationtAttributeType)
                {
                    return false;
                }
            }
        }
        
        return true;
    }         

    /**
     *  Creates all elements needed to create a variation model from scratch,
     *  if no variation model already exists. This may affect all import modes.
     *  Method doesn't check for an existing model, this must be done by the caller.
     *
     *  @param  relation    The element holding the variation model.
     *  @return True if successful created.
     */

    private boolean createVariationModel(ElementProductRelation relation)
        throws SystemException
    {
        ProductVariationModel variationModel = relation.getVariationModel();
        
        if (variationModel != null)
        {
            ProductRelationSourcePoint master = relation.getSourcePoint();

            Product masterBean = master.getRelatedProduct();

            variationModel.setMasterProduct(masterBean);

            String masterDomainID = masterBean.getDomainID();

            if (!resolveProductVariations(variationModel, master))
            {
                return false;
            }
            
            //checks if the variations/master are shared and logs some messages.
            checkVariationSharing(variationModel);

            List<ElementProductVariation> productVariations = variationModel.getProductVariations();

            for (int i = 0, l = productVariations.size(); i < l; i++)
            {
                ElementProductVariation pv = productVariations.get(i);

                pv.setDomainID(masterDomainID);
                complementPersistentObjectAttributes(pv, null);
            }

            if (!checkUniquenessProductVariations(variationModel, false))
            {
                return false;
            }
            
            setDefaultVariation(variationModel);

            //  Products ok, evaluate attributes.

            List<ElementVariableVariationAttribute> attributes = variationModel.getVariableVariationAttributes();

            if (attributes.size() > 0)
            {
                if (!resolveVariableVariationAttributes(attributes, productVariations, master))
                {
                    return false;
                }

                for (int i = 0, l = attributes.size(); i < l; i++)
                {
                    ElementVariableVariationAttribute va =
                        attributes.get(i);

                    va.setDomainID(masterDomainID);
                    complementPersistentObjectAttributes(va, null);
                }

                createVariationValuesAndAssignments(variationModel);
            }
            
        }
        
        return true;
    }
    
    
    protected void checkVariationSharing(ProductVariationModel variationModel)
    {
        //REQ: It's not possible to modify a shared variation master/variation 
        //product structure in the channel. Variation products can't be added/ 
        //can't be removed from the master in the channel.
        
        if (variationModel.getProductVariations().size() == 0)
        {
            //there are no variations involved which means that we will not modify
            //the variation structure. This applies even for REPLACE mode because
            //the existing variations will be added to the model.
            return;
        }
        
        Product masterProduct = variationModel.getMasterProduct();
        if (masterProduct.isDerivedProduct())
        {
            logger.logMessage(ImpexLogger.LOG_WARN,
                            "xcs.impex.ElementValidatorProductRelation_W_PRODUCT_VARIATION_SHARED",
                            masterProduct.getSKU(), 
                            masterProduct.getDomain().getDomainName());
            
            variationModel.getProductVariations().clear();
            return;
        }
        
        Iterator<ElementProductVariation> elementPVIter = variationModel.getProductVariations().iterator();
        while (elementPVIter.hasNext())
        {
            ElementProductVariation elemPV = elementPVIter.next();

            Product variation = elemPV.getVariationProduct();
            if (variation.isDerivedProduct())
            {
                logger.logMessage(ImpexLogger.LOG_WARN,
                                "xcs.impex.ElementValidatorProductRelation_W_PRODUCT_VARIATION_SHARED",
                                variation.getSKU(), 
                                variation.getDomain().getDomainName());
                    
                elementPVIter.remove();
            }
        }
        
        return;
    }
    
    /**
     *  Resolves a relation source point. If source could be resolved, the
     *  source id and the related product will be set appropriately. Otherwise
     *  logs a message and returns false.
     *
     *  @param  sourcePoint     The relations source point to resolve.
     *  @return True if the source point could be resolved (points to an
     *          existing product).
     *  @throws  ImportException If product could not be referenced.
     */

    protected boolean resolveSourcePoint(ProductRelationSourcePoint sourcePoint)
        throws SystemException
    {
        Product product = null;
        String  uuid    = sourcePoint.getID();
        String  sku     = sourcePoint.getSku();
        String  refSku  = sourcePoint.getRefSku();

        if (uuid == null && sku == null && refSku == null)
        {
            logger.logMessage(ImpexLogger.LOG_ERROR, "xcs.impex.ElementValidatorProductRelation_E_PRODUCT_REF",getImportElementRecorder());
            return false;
        }

        
        
        if (sku != null)
        {
            product = importProductMgr.getProductBySKU(sku, currentDomain);
        }
        
        if (product == null && uuid != null)
        {
            product = importProductMgr.getProductByUUID(uuid, currentDomain);
        }

        if (product == null && sku != null)
        {
            product = importProductMgr.getProductBySKU(sku);
        }

        if (product == null && refSku != null)
        {
            product = importProductMgr.getProductBySKU(refSku);
        }        
        
        if (product != null)
        {
            sourcePoint.setID(product.getUUID());
            sourcePoint.setRelatedProduct(product);
            return true;
        }
        
        return false;
    }

    /**
     *  Resolves a link target:
     *  <ul>
     *      <li>Maps product sku to product id.
     *      <li>Maps category name to category id.
     *      <li>Sets the related product for the target.
     *  </ul>
     *
     *  @param  source  The source (for referencing in log messages.)
     *  @param  link    The link to resolve.
     *  @return True if link could be resolved.
     *  REWORK Use CategoryDomain for category lookup. (line 1660)
     */

    protected boolean resolveProductLink(ProductRelationSourcePoint source, ElementProductLink link)
        throws SystemException
    {
        if (!resolveLinkTargetProduct(source, link.getTargetPoint()))
        {
            return false;
        }

        //
        //  Resolve category if supplied.
        //

        resolveLinkTargetCategory(source, link);
        
        return true;
    }

    /**
     *  Resolves the target product for a product link. Complements the
     *  target specification with product attributes (if product found).
     *
     *  @param  source  The source (for referencing in log messages.)
     *  @param  target  The target specification for the product link.
     *  @return True if the target could be resolved and user has permissions,
     *          false otherwise.
     */

    private boolean resolveLinkTargetProduct(ProductRelationSourcePoint source, ProductRelationEndPoint target)
        throws SystemException
    {
        Product product       = null;
        String  uuid          = target.getID();
        String  refSku        = target.getRefSku();
        String  refDomainName = target.getRefDomainName()==null ?
                controller.getDomainName() : target.getRefDomainName();
        Domain domain = getDomainByName(refDomainName);
        if (domain == null)
        {
            logger.logMessage(ImpexLogger.LOG_ERROR,
                              "xcs.impex.ElementValidatorIllegalDomainName",
                              "product link", refSku, refDomainName,getImportElementRecorder());
            return false;
        }

        if (refSku != null)
        {
            product = importProductMgr.getProductBySKU(refSku, domain);
        }

        if (product == null && uuid != null)
        {
            product = importProductMgr.getProductByUUID(uuid, domain);
        }        
        
        if (product == null)
        {
            logger.logMessage(ImpexLogger.LOG_ERROR,
                              "xcs.impex.ElementValidatorProductRelation_E_LINKTGT_ILL",
                              uuid, refSku, source.getSku(),getImportElementRecorder());
            return false;
        }

        //only if current user has the permission on this target product
        if (importPermissionMgr.userHasViewPermissions(product))
        {
            target.setID(product.getUUID());
            target.setRelatedProduct(product);
        }
        else
        {
            logger.logMessage(ImpexLogger.LOG_WARN,
                              "xcs.impex.ElementValidatorProductRelation_W_LINK_DENIED",
                              product.getName(), source.getSku());
            return false;
        }
        
        return true;
    }

    /**
     *  Resolves the target category for a product link. Complements the
     *  link with category attributes (if category found).
     *
     *  @param  source     The source (for referencing in log messages.)
     *  @param  link       The product link.
     */

    private void resolveLinkTargetCategory(ProductRelationSourcePoint source, ElementProductLink link)
        throws SystemException
    {
        String categoryid   = link.getCategoryID();
        String categoryName = link.getCategoryName();

        if (categoryid != null || categoryName != null)
        {
            CatalogCategory category = null;

            if (categoryid != null)
            {
                category = importProductMgr.getCategoryByUUID(categoryid);
            }

            if (category == null && categoryName != null)
            {
                String categoryDomain = link.getCategoryDomainName();

                if (categoryDomain != null)
                {
                    Domain domain = getDomainByName(categoryDomain);

                    if (domain == null)
                    {
                        logger.logMessage(ImpexLogger.LOG_ERROR, "xcs.impex.ElementValidatorIllegalDomainName",
                                          "product link", categoryName, categoryDomain,getImportElementRecorder());
                        return;
                    }

                    category = importProductMgr.getCategoryByName(categoryName, domain);
                }
                else
                {
                    category = importProductMgr.getCategoryByName(categoryName);
                }
            }

            if (category != null)
            {
                if (importPermissionMgr.userHasViewPermissions(category) ||
                    importPermissionMgr.userHasEditPermissions(category))
                {
                    link.setCategoryID(category.getUUID());
                }
                else
                {
                    link.setCategoryID(null);
                    logger.logMessage(ImpexLogger.LOG_WARN,
                                      "xcs.impex.ElementValidatorProduct_W_CATEGORY_DENIED",
                                      category.getName(), source.getSku());
                }
            }
            else
            {
                link.setCategoryID(null);
                logger.logMessage(ImpexLogger.LOG_WARN,
                                  "xcs.impex.ElementValidatorProductRelation_W_LINKCATEGORY_ILL", source.getSku());
            }
        }
        else
        {
            link.setCategoryID(null);
        }
    }

    /**
     *  Resolves a bundled target:
     *  <ul>
     *      <li>Maps product sku to product id.
     *      <li>Sets the related product for the target.
     *  </ul>
     *
     *  @param  source  The source (used for referencing in log messages.)
     *  @param  target  The target to resolve.
     *  @return True if target could be resolved.
     */

    protected boolean resolveBundledTarget(ProductRelationSourcePoint source, ProductRelationEndPoint target)
        throws SystemException
    {
        String uuid = target.getID();
        Product bundledProduct = null;
        String refDomainName = target.getRefDomainName()==null ?
                controller.getDomainName() : target.getRefDomainName();

        Domain domain = getDomainByName(refDomainName);
        if (domain == null)
        {
            logger.logMessage(ImpexLogger.LOG_ERROR, "xcs.impex.ElementValidatorIllegalDomainName",
                              "bundled product", target.getRefSku(), refDomainName,getImportElementRecorder());
            return false;
        }

        if (target.getRefSku() != null)
        {
            bundledProduct = importProductMgr.getProductBySKU(target.getRefSku(), domain);
        }
        
        if (bundledProduct == null && uuid != null)
        {
            bundledProduct = importProductMgr.getProductByUUID(uuid, domain);
        }

        if (bundledProduct == null)
        {
            logger.logMessage(ImpexLogger.LOG_ERROR, "xcs.impex.ElementValidatorProductRelation_E_BUNDLETGT_ILL", source.getSku(), uuid, target.getRefSku(),getImportElementRecorder());
            return false;
        }

        //only if current user has the permission on this target product
        if (importPermissionMgr.userHasViewPermissions(bundledProduct))
        {
            target.setID(bundledProduct.getUUID());
            target.setRelatedProduct(bundledProduct);
        }
        else
        {
            logger.logMessage(ImpexLogger.LOG_WARN,
                              "xcs.impex.ElementValidatorProductRelation_W_BUNDLE_DENIED",
                              bundledProduct.getName(), source.getSku());
            return false;
        }
        
        return true;
    }
                          
    /**                        
     *  A type is required for a ProductLink.
     *  If no type is provided by the import source, use a default type.
     *
     *  @param  source  The source (for referencing in log messages.)
     *  @param  link    The link to resolve.
     *  REWORK make the default value configurable
     */

    protected void resolveLinkType(String sourceSKU, ElementProductLink link)
        throws SystemException
    {
        String typeName = link.getLinkTypeCodeName();  // get (new) string typeCode of the link, defined by a TypeCodeDefinition:Name
        TypeCodeDefinition typeCodeDefinition = null;

        if (typeName == null)
        {
            // fallback to the integer typeCode of the link (deprecated) stored as string
            String typeCodeString = link.getLinkType();
            if (typeCodeString != null) {
                // get an Integer from the String
                try
                {
                    int typeCode = Integer.parseInt(typeCodeString);
                    
                    // check if a TypeCodeDefinition exists
                    typeCodeDefinition = typeCodeRegistry.getTypeCodeDefinition(typeCode, ProductLink.TypeCodeDefinitionGroup);
                    if (typeCodeDefinition != null) {
                        typeName = typeCodeDefinition.getName();
                    }
                    else {
                        logger.logMessage(ImpexLogger.LOG_ERROR,
                                        "xcs.impex.ElementValidatorProductRelation_E_LINKTYPE_ILL",
                                        "'" + typeCodeString + "'", sourceSKU,getImportElementRecorder());
                    }
                }
                catch (NumberFormatException e)
                {
                    logger.logMessage(ImpexLogger.LOG_ERROR,
                                        "xcs.impex.ElementValidatorProductRelation_E_LINKTYPE_ILL",
                                        "'" + typeCodeString + "'", sourceSKU, getImportElementRecorder());
                }
            }
            else {
                logger.logMessage(ImpexLogger.LOG_ERROR,
                                "xcs.impex.ElementValidatorProductRelation_E_LINKTYPE_ILL",
                                "null", sourceSKU, getImportElementRecorder());
            }
        }
        else
        {
            // check if a TypeCodeDefinition exists
            typeCodeDefinition = typeCodeRegistry.getTypeCodeDefinition(typeName, ProductLink.TypeCodeDefinitionGroup);
            if (typeCodeDefinition == null) {
                logger.logMessage(ImpexLogger.LOG_ERROR,
                                "xcs.impex.ElementValidatorProductRelation_E_LINKTYPE_ILL",
                                "'" + typeName + "'", sourceSKU, getImportElementRecorder());
                typeName = null;
            }
        }

        // use the (new) string typeCode of the link, defined by a TypeCodeDefinition:Name
        // the integer type code is stored in the database
        link.setLinkTypeCodeName(typeName);
        if (typeCodeDefinition != null) {
            link.setLinkTypeCode(typeCodeDefinition.getTypeCode());
        }
    }

    /**
     *  Complements bundled attributes from a source.
     *  If source is null, sets only the oca.
     *
     *  @param  bundled     The bundled to complete.
     *  @param  source      The attribute source for the bundled.
     */

    protected void complementBundledAttributes(ElementBundledProduct bundled,
                                               BundleAssignmentPO source)
        throws SystemException
    {
        if (source == null)
        {
            bundled.setOca(0);
            return;
        }

        if (bundled.getQuantity() == null && !source.getQuantityNull())
        {
            bundled.setQuantity(source.getQuantity().getValue().toString());
        }
        if (bundled.getQuantityUnit() == null && !source.getQuantityNull())
        {
            bundled.setQuantityUnit(source.getQuantity().getUnit());
        }
        if (bundled.getPosition() == null && !source.getPositionNull())
        {
            bundled.setPosition(Double.toString(source.getPosition()));
        }
        if (bundled.getOnline() == null)
        {
            bundled.setOnline(source.isOnline());
        }
        if (!source.getOcaNull())
        {
            bundled.setOca(source.getOca() + 1);
        }
        else
        {
            bundled.setOca(0);
        }
    }

    /**
     *  Checks the bundled quantity (must be a double).
     *  Sets a default, if quantity is not valid.
     *
     *  @param  bundled     The bundled that contains the quantity.
     */

    private void checkBundledQuantity(ElementBundledProduct bundled)
    {
        String quantity = bundled.getQuantity();

        try
        {
            Double.parseDouble(quantity);
        }
        catch (Exception e)
        {
            logger.logMessage(ImpexLogger.LOG_WARN, "xcs.impex.IllegalQuantityForBundle", quantity);
            bundled.setQuantity(DEFAULT_BUNDLED_QTY);
        }
    }

    /**
     *  Checks the bundled position (must be a double or null).
     *  Sets a default (null), if position is not valid.
     *
     *  @param  bundled     The bundled that contains the position.
     */

    private void checkBundledPosition(ElementBundledProduct bundled)
    {
        String position = bundled.getPosition();

        if (position==null) 
        {
            return;
        }

        try
        {
            Double.parseDouble(position);
        }
        catch (Exception e)
        {
            logger.logMessage(ImpexLogger.LOG_WARN, "xcs.impex.IllegalPositionForBundle", position);
            bundled.setPosition(null);
        }
    }

    /**
     *  Checks the position of VariableVariationAttribute (must be a double or null).
     *  Sets a default (null), if position is not valid.
     *
     *  @param  evva     The ElementVariableVariationAttribute that contains the position.
     */

    private void checkVVAPosition(ElementVariableVariationAttribute evva)
    {
        String position = evva.getPosition();

        if (position==null) 
        {
            return;
        }

        try
        {
            Double.parseDouble(position);
        }
        catch (Exception e)
        {
            logger.logMessage(ImpexLogger.LOG_WARN, "xcs.impex.IllegalPositionForVariableVariationAttribute", position);
            evva.setPosition(null);
        }
    }


    /**
     *  Called to cleanup.
     *
     *  @return True if finished ok.
     */

    @Override
    public boolean finish()
    {
        productVariationAssignmentHome = null;
        currentDomain = null;
        plHome = null;
        baHome = null;

        importProductMgr    = null;
        importPermissionMgr = null;

        variationProductSKUs = null;
        
        // call finish for all extensions
        if(!finishExtension())
            return false;

        return true;
    }

    /**
     *  Returns an attribute value of an extensible object,
     *  matching the passed attribute name.
     *
     *  @param  object          The object to get the specified attribute for.
     *  @param  attributeName   The name of the attribute.
     *  @return An attribute value for the passed attribute name, null
     *          if the object doesn't have the attribute.
     */

    protected AttributeValueWrapper getAttributeValueForAttributeName(Factory factory, String ownerID,
                                                                      String attributeName)
        throws SystemException
    {
        AttributeValueWrapper attributeValue = null;

        Iterator<AttributeValue> attributeValues = null;

        try
        {
            attributeValues = ImportProductMgr.createAttributeValueIterator(factory, ownerID);
        }
        catch (FinderException e)
        {
            return null;
        }

        while (attributeValues.hasNext())
        {
            AttributeValue av = attributeValues.next();

            if (av.getName().equals(attributeName))
            {
                if (attributeValue == null)
                {
                    attributeValue = new AttributeValueWrapper(attributeName);
                }

                attributeValue.attributeValues.add(av);
            }
        }

        attributeValues = null;
        
        return attributeValue;
    }

    protected AttributeValueWrapper getAttributeValueForAttributeName(AttributeValueIterator attributeValues,
                                                                      String attributeName)
        throws SystemException
    {
        AttributeValueWrapper attributeValue = new AttributeValueWrapper(attributeName);

        attributeValues.reset();
        while (attributeValues.hasNext())
        {
            AttributeValue av = attributeValues.nextAttributeValue();

            if (av.getName().equals(attributeName))
            {
                attributeValue.attributeValues.add(av);
            }
        }

        return attributeValue;
    }
    
    /**
     * Logs unknown source point.
     */
    
    private void logUnknownSource(ProductRelationSourcePoint source)
    {
        logger.logMessage(ImpexLogger.LOG_ERROR,
                          "xcs.impex.ElementValidatorProductRelation_E_SRCPOINT_ILL",
                          source.getID(), source.getSku(), source.getRefSku(), getImportElementRecorder());
    }

    /**
     *  An attribute value wrapper stores all attribute values of an EO
     *  for a certain attribute name.
     */

    protected class AttributeValueWrapper
    {
        /**
         *  The name of the attribute.
         */

        public String attributeName;

        /**
         *  A list of all attribute values of a product for the specified
         *  attribute name.
         */

        public List<AttributeValue> attributeValues;

        /**
         *  Creates with the given attribute name.
         */

        public AttributeValueWrapper(String attributeName)
        {
            this.attributeName = attributeName;
            attributeValues    = new ArrayList<>(20);
        }

        /**
         *  ...
         */

        public boolean equals(AttributeValueWrapper attributeValue)
            throws SystemException
        {
            if (attributeValue == null || attributeName == null ||
                attributeValues == null || attributeValue.attributeValues == null)
            {
                return false;
            }

            if (!attributeName.equals(attributeValue.attributeName))
            {
                return false;
            }

            if (attributeValues.size() != attributeValue.attributeValues.size())
            {
                return false;
            }

            boolean attributesEquals = true;

            for (int i = 0, l = attributeValues.size(); i < l && attributesEquals; i++)
            {
                AttributeValue av = attributeValues.get(i);

                attributesEquals = false;

                for (int k = 0, ll = attributeValue.attributeValues.size(); k < ll && !attributesEquals; k++)
                {
                    AttributeValue av1 = attributeValue.attributeValues.get(k);

                    if (av.equals(av1))
                    {
                        attributesEquals = true;
                    }
                }
            }

            return attributesEquals;
        }
        
        @Override
        public int hashCode()
        {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((attributeName == null) ? 0 : attributeName.hashCode());
            // cannot include the attributeValues field, because AttributeValues don't override hashCode()              
            return result;
        }
    }
    
    /**
     *  Replicates certain product attributes.
     *  <ul>
     *      <li>unit</li>
     *      <li>description</li>
     *  </ul>
     *  are added as eoa's. 
     */

    protected void replicateProductAttributes(ElementProductLink productLink)
    {
        XMLLocalizedElement unit           = productLink.getUnit();
        XMLLocalizedElement description    = productLink.getDescription();

        if (unit != null)
        {
            productLink.setCustomAttribute(unit, "unit");
        }

        if (description != null)
        {
            productLink.setCustomAttribute(description, "description");
        }
    }

    private boolean hasInvalidTypeCombinations (ElementProductRelation relation, boolean isSourcePointResolved) 
    {
        if (relation.hasRelations())
        {
            boolean isOrGetsBundle = false;
            boolean isOrGetsVariation = false;
            Product sourceProduct; 
            if (isSourcePointResolved)
            {
                sourceProduct = relation.getSourcePoint().getRelatedProduct();
                isOrGetsBundle = sourceProduct.isProductBundle(); 
                isOrGetsVariation = sourceProduct.isProductMaster(); 
            }
            
            if (relation.getVariationModel() != null) {
                isOrGetsVariation = true;
            }
            if (relation.getBundledProducts() != null && relation.getBundledProducts().size() > 0) 
            {
                isOrGetsBundle = true;
            }
            
            if (isOrGetsVariation && isOrGetsBundle) {
                logger.logMessage(ImpexLogger.LOG_ERROR, "core.impex.Exception", "Product with sku '" + relation.getSourcePoint().getSku() + "' cannot be both bundle and variation master",getImportElementRecorder());
                return true;
            }
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    private boolean isRelationProductLinksAttributesValid (ElementProductRelation productRelation) 
    {
        List <ElementProductLink> links  = productRelation.getProductLinks();
        ProductRelationSourcePoint source = productRelation.getSourcePoint();

        for (int i = 0, numLinks = links.size(); i < numLinks; i++)
        {
            ElementProductLink link = links.get(i);
            if (! isProductLinkAttributesValid(link,source)){
                return false;
            }
        }
        return true;
    }

    private boolean isProductLinkAttributesValid (ElementProductLink link,ProductRelationSourcePoint source) 
    {
        ProductLink exLink= null;
        exLink = link.getProductLinkPO();
        
        Iterator<Entry<String, XMLLocalizedElement>> attributesIterator = link.getCustomAttributes().entrySet().iterator(); 

        // be aware that this validation is necessary even if the product still does not exist
        if (! elementValidatorCustomAttributes.isValid(attributesIterator, exLink))
        {
            return false;
        }
        
        return true;
    }

    @SuppressWarnings("unchecked")
    private boolean isRelationVariableVariationAttributesAttributesValid (ElementProductRelation productRelation) 
    {
        ProductVariationModel model = productRelation.getVariationModel();
        if (model != null){
            List<ElementVariableVariationAttribute> vvattributes = model.getVariableVariationAttributes();
            
            for (int i = 0, numLinks = vvattributes.size(); i < numLinks; i++)
            {
                ElementVariableVariationAttribute vva = vvattributes.get(i);
                if (! isVariableVariationAttributeAttributesValid(vva)){
                    return false;
                }
            }
        }
        return true;
    }

    private boolean isVariableVariationAttributeAttributesValid (ElementVariableVariationAttribute vva) 
    {
        ExtensibleObject exVVA= null;
        exVVA = (ExtensibleObject)vva.getPOObject();
        
        Iterator<Entry<String, XMLLocalizedElement>> attributesIterator = vva.getCustomAttributes().entrySet().iterator(); 

        // be aware that this validation is necessary even if the product still does not exist
        if (! elementValidatorCustomAttributes.isValid(attributesIterator, exVVA))
        {
            return false;
        }
        return true;
    }
    
    // validator extension code
    /**
     * returns first ElementValidatorProductRelationExtension implementation found
     * @return ElementValidatorProductRelationExtension
     */
    protected List<ElementValidatorProductRelationExtension> initExtension()
    {
        ApplicationTypeImpl app = (ApplicationTypeImpl)(AppContextUtil.getCurrentAppContext().getApp());
        return app.getJavaExtensions(ElementValidatorProductRelationExtension.class, "prepareExtension");
    }
    /**
     *  Initializes the parser.
     *
     *  @param   aController    The import controller.
     *  @param   elementValidator to handle custom-attributes
     *  @return  True if successful.
     */
    @ExtensionPoint(type = ElementValidatorProductRelationExtension.class, id = "prepareExtension")
    public boolean prepareExtension(Controller controller)
    {
        // default value in case no java extension implementation exists
        boolean retValue = true;
        // call java extension methods if java extension implementation was found
        if(validatorProductRelationExtensions!=null && !validatorProductRelationExtensions.isEmpty())
        {
            for(ElementValidatorProductRelationExtension validatorExtension : validatorProductRelationExtensions)
            {
                if(!validatorExtension.prepareExtension(controller))
                {
                    retValue = false;
                    break;
                }
            }
        }
        return retValue;
    }
    /**
     *  Called to cleanup.
     *
     *  @return True if finished ok.
     */
    @ExtensionPoint(type = ElementValidatorProductRelationExtension.class, id = "finishExtension")
    public boolean finishExtension()
    {
        // default value in case no java extension implementation exists
        boolean retValue = true;
        // call java extension methods if java extension implementation was found
        if(validatorProductRelationExtensions!=null && !validatorProductRelationExtensions.isEmpty())
        {
            for(ElementValidatorProductRelationExtension validatorExtension : validatorProductRelationExtensions)
            {
                if(!validatorExtension.finishExtension())
                {
                    retValue = false;
                    break;
                }
            }
        }
        return retValue;
    }
    /**
     *  Validates the shipping surcharge element.
     *
     *  @param  product  The ElementProductRelationShippingSurcharge element.
     *  @return True if surcharge is valid.
     */
    @ExtensionPoint(type = ElementValidatorProductRelationExtension.class, id = "isValidExtension")
    public boolean isValidExtension(ElementProductRelation elementProductRelation) throws SystemException
    {
        // default value in case no java extension implementation exists
        boolean retValue = true;
        // call java extension methods if java extension implementation was found
        if(validatorProductRelationExtensions!=null && !validatorProductRelationExtensions.isEmpty())
        {
            for(ElementValidatorProductRelationExtension validatorExtension : validatorProductRelationExtensions)
            {
                if(!validatorExtension.isValidExtension(elementProductRelation))
                {
                    retValue = false;
                    break;
                }
            }
        }
        return retValue;
    }
    
    /**
     *  Filters existing product links from the product relation
     *
     *  @param productRelation the product relation
     *  
     */
    private void filterExistingProductLinks(ElementProductRelation productRelation)
    {
        ProductRelationSourcePoint source = productRelation.getSourcePoint();
        List<ElementProductLink> productLinks = productRelation.getProductLinks();
        if (productLinks != null)
        {
            Iterator<ElementProductLink> productLinksIter = productLinks.iterator();
            while (productLinksIter.hasNext())
            {
                ElementProductLink productLink = productLinksIter.next();
                ProductRelationEndPoint target = productLink.getTargetPoint();
                @SuppressWarnings("unchecked")
                Iterator<ProductLinkPO> existingLinksIter = plHome.getObjectsBySQLWhere("sourceid=? and targetid=? and domainid=?",
                                                            new Object[]{ source.getID(), target.getID(), productLink.getDomainID() }).iterator();
                try
                {
                    if (existingLinksIter.hasNext())
                    {
                        productLinksIter.remove();
                    }
                }
                finally
                {
                    ORMHelper.closeIterator(productLinksIter);
                }
            }
        }
    }
    
    /**
     *  Filters existing bundle assignments from the product relation.
     *  
     *  @param productRelation the product relation
     */

    private void filterExistingBundles(ElementProductRelation productRelation)
    {
        ProductRelationSourcePoint source = productRelation.getSourcePoint();
        @SuppressWarnings("unchecked")
        List<ElementBundledProduct> bundledProducts = productRelation.getBundledProducts();
        if (bundledProducts != null)
        {
            Iterator<ElementBundledProduct> bundledProductsIter = bundledProducts.iterator();
            while (bundledProductsIter.hasNext())
            {
                ElementBundledProduct bundledProduct = bundledProductsIter.next();
                ProductRelationEndPoint target = bundledProduct.getBundledProduct();
                
                @SuppressWarnings("unchecked")
                Iterator<BundleAssignmentPO> existingBundleAssignmentsIter = baHome.getObjectsBySQLWhere("bundleid=? and productid=?",
                                                                                    new Object[]{ source.getID(), target.getID() }).iterator();
                try
                {
                    if (existingBundleAssignmentsIter.hasNext())
                    {
                        bundledProductsIter.remove();
                    }
                }
                finally
                {
                    ORMHelper.closeIterator(bundledProductsIter);
                }
            }
        }
    }
    
    /**
     * Filters existing variations from the product relation
     * 
     * @param productRelation the product relation
     */
    private void filterExistingProductVariations(ElementProductRelation productRelation)
    {
        ProductVariationModel variationModel = productRelation.getVariationModel();
        if (variationModel != null)
        {
            ProductRelationSourcePoint master = productRelation.getSourcePoint();
            List<ElementProductVariation> elementProductVariations = variationModel.getProductVariations();
            if (elementProductVariations != null)
            {
                Iterator<ElementProductVariation> elementProductVariationsIter = elementProductVariations.iterator();
                while (elementProductVariationsIter.hasNext())
                {
                    ElementProductVariation elementProductVariation = elementProductVariationsIter.next();
                    Product productVariation = importProductMgr.getProductBySKU(elementProductVariation.getVariationProductSku(), elementProductVariation.getDomainName());
                    if (productVariation != null)
                    {
                        @SuppressWarnings("unchecked")
                        Iterator<ProductVariationPO> existingProductVariationsIter = variationHome.getObjectsBySQLWhere("productmasterid=? AND productid=? and domainid=?",
                                                                                        new Object[] { master.getID(), productVariation.getUUID(), productVariation.getDomainID() }).iterator();
                        try
                        {
                            if (existingProductVariationsIter.hasNext())
                            {
                                elementProductVariationsIter.remove();
                            }
                        }
                        finally
                        {
                            ORMHelper.closeIterator(existingProductVariationsIter);
                        }
                    }
                }
            }
        }
    }
}