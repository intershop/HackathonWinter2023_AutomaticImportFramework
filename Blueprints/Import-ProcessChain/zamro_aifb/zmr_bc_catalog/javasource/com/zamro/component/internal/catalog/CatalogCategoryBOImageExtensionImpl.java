package com.zamro.component.internal.catalog;

import com.intershop.beehive.businessobject.capi.BusinessObjectRepositoryFactory;
import com.intershop.beehive.core.capi.domain.AbstractPersistentObjectBOExtension;
import com.intershop.beehive.core.capi.domain.Domain;
import com.intershop.component.catalog.capi.CatalogCategoryBO;
import com.intershop.component.image.capi.ImageBO;
import com.intershop.component.image.capi.ImageBORepository;
import com.intershop.component.image.capi.ImageTypeBO;
import com.intershop.component.image.capi.ImageTypeBORepository;
import com.intershop.component.image.capi.ImageViewBO;
import com.intershop.component.image.capi.ImageViewBORepository;
import com.intershop.component.repository.capi.RepositoryBO;
import com.intershop.component.repository.capi.RepositoryBORepository;
import com.zamro.component.capi.catalog.CatalogCategoryBOImageExtension;
import com.zamro.component.internal.image.ZMRImageBOImpl;

/**
 * This extension provides functions for rendering menu structures.
 * 
 * @author Juergen Nuetzel
 * 
 */
public class CatalogCategoryBOImageExtensionImpl extends AbstractPersistentObjectBOExtension<CatalogCategoryBO>
                implements CatalogCategoryBOImageExtension<CatalogCategoryBO>
{
    private static final String IMAGE_BASE_NAME = "image-base-name";
    private static final String IMAGE_VIEW_ID = "front";
    private static final String CATEGORY_IMAGE_PATH = "category-images";
    
    /**
     * Create the instance.
     * 
     * @param catalogCategoryBO
     *            The catalog category business object.
     */
    public CatalogCategoryBOImageExtensionImpl(CatalogCategoryBO catalogCategoryBO)
    {
        super(EXTENSION_ID, catalogCategoryBO);
    }

    /**
     * Returns fully qualified href for the categoryImage.
     * 
     * @param imageType
     *            The needed image type.
     * @return the detail image url.
     */
    public ImageBO getImageBO(String imageType)
    {
        CatalogCategoryBO catalogCategoryBO = getExtendedObject();
        Domain imageDomain = catalogCategoryBO.getCatalogBO().getOwningDomain();
        
        BusinessObjectRepositoryFactory<RepositoryBORepository> repositoryFactory = null;
        repositoryFactory = getContext().getEngine().getRepositoryFactory(RepositoryBORepository.class);
        RepositoryBORepository repositoryBORepository = repositoryFactory.createRepository(getContext());
        
        
        // get the business object repositories
        final RepositoryBO repository = repositoryBORepository.getRepositoryBOByID(imageDomain.getUUID());
        final ImageBORepository imageBORepository = repository.getExtension(ImageBORepository.class);
        final RepositoryBO definitionRepository = imageBORepository.getImageDefinitionRepositoryBO();

        final ImageViewBORepository imageViewBORepository = definitionRepository
                        .getExtension(ImageViewBORepository.class);
        final ImageTypeBORepository imageTypeBORepository = definitionRepository
                        .getExtension(ImageTypeBORepository.class);        
        
        ImageTypeBO imageTypeBO = imageTypeBORepository.getImageTypeBOByID(imageType);
        ImageViewBO imageViewBO = imageViewBORepository.getImageViewBOByID(IMAGE_VIEW_ID);
                       
        String imageBaseName = catalogCategoryBO.getString(IMAGE_BASE_NAME);
        
        if (imageBaseName == null)
        {
            imageBaseName = CATEGORY_IMAGE_PATH + '/' + catalogCategoryBO.getCatalogBO().getName() + "_" + catalogCategoryBO.getName() + ".png"; 
        }
          
        ImageBO imageBO = new ZMRImageBOImpl(imageTypeBO, imageViewBO, imageBaseName, getContext(), imageBORepository);
        
        return imageBO;
    }
    
}