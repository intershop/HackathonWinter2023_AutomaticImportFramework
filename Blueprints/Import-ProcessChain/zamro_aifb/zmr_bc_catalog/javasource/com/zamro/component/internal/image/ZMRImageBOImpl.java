package com.zamro.component.internal.image;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import com.intershop.beehive.businessobject.capi.AbstractBusinessObject;
import com.intershop.beehive.businessobject.capi.BusinessObjectContext;
import com.intershop.beehive.core.capi.url.URLUtils;
import com.intershop.component.image.capi.ImageBO;
import com.intershop.component.image.capi.ImageBORepository;
import com.intershop.component.image.capi.ImageBOUrlExtension;
import com.intershop.component.image.capi.ImageTypeBO;
import com.intershop.component.image.capi.ImageViewBO;
import com.intershop.component.image.capi.common.ImageReferenceHolder;

/**
 * Creates a transient ImageBO
 * 
 * @author Jürgen Nützel
 *
 */
public class ZMRImageBOImpl extends AbstractBusinessObject implements ImageBO
{
    private ImageTypeBO imageTypeBO = null;
    private ImageViewBO imageViewBO = null;
    private String imageBaseName = null;

    private Set<ImageReferenceHolder> holders = new LinkedHashSet<>(); 

    final private ImageBOUrlExtension urlExtension;

    public ZMRImageBOImpl(ImageTypeBO imageTypeBO, ImageViewBO imageViewBO, String imageBaseName, BusinessObjectContext context, ImageBORepository repository)
    {
        super(imageTypeBO.getID() + "/" + imageViewBO.getID() + "/" + imageBaseName, context);

        this.urlExtension = getExtension(ImageBOUrlExtension.class);
    
        this.imageTypeBO = imageTypeBO;
        this.imageViewBO = imageViewBO;
        this.imageBaseName = imageBaseName;
    }

    @Override
    public void delete()
    {
        super.delete();
    }

    @Override
    public String getExternalUrl()
    {
        return urlExtension.getExternalUrl();
    }

    @Override
    public String getEffectiveUrl()
    {
        return urlExtension.getEffectiveUrl();
    }

    @Override
    public String getEffectiveUrl(Boolean absoluteUrl)
    {
        return urlExtension.getEffectiveUrl(absoluteUrl);
    }

    @Override
    public String getEffectiveUrl(String localeID, Boolean absoluteUrl)
    {
        return urlExtension.getEffectiveUrl(localeID, absoluteUrl);
    }

    @Override
    public String getImageBaseName()
    {
        return imageBaseName;
    }

    @Override
    public ImageViewBO getImageViewBO()
    {
        return getImageView();
    }

    @Deprecated
    @Override
    public ImageViewBO getImageView()
    {
        return imageViewBO;
    }

    @Override
    public ImageTypeBO getImageTypeBO()
    {
        return getImageType();
    }

    @Deprecated
    @Override
    public ImageTypeBO getImageType()
    {
        return imageTypeBO;
    }

    @Override
    public Integer getActualHeight()
    {
        return null;
    }

    @Override
    public void setActualHeight(Integer height)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Integer getActualWidth()
    {
        return null;
    }

    @Override
    public void setActualWidth(Integer width)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Integer getHeight()
    {
        return getImageTypeHeight();
    }

    @Override
    public Integer getWidth()
    {
        return getImageTypeWidth();
    }

    @Override
    public Integer getImageTypeHeight()
    {
        return getImageTypeBO().getHeight();
    }

    @Override
    public Integer getImageTypeWidth()
    {
        return getImageTypeBO().getWidth();
    }

    @Override
    public boolean removeImageReferenceHolder(ImageReferenceHolder holder)
    {
        if (null == holder) {
            throw new IllegalArgumentException("Argument 'holder' must not be null.");
        }
        return holders.remove(holder);
    }

    @Override
    public ImageReferenceHolder getImageReferenceHolder()
    {
        ImageReferenceHolder holder = null;
        if (!holders.isEmpty()) {
            holder = holders.iterator().next();
        }
        return holder;
    }

    @Override
    public Collection<ImageReferenceHolder> getImageReferenceHolders()
    {
        return holders;
    }

    @Override
    public boolean isPrimaryImage()
    {
        boolean primaryImage = false;
        if (!holders.isEmpty()) {
            primaryImage = isPrimaryImage(holders.iterator().next());
        }
        return primaryImage;
    }

    @Override
    public boolean isPrimaryImage(ImageReferenceHolder holder)
    {
        if (null == holder) {
            throw new IllegalArgumentException("Argument 'holder' must not be null.");
        }
        boolean primaryImage = true;
        return primaryImage;
    }

    @Override
    public boolean hasImageReferenceHolder(ImageReferenceHolder holder)
    {
        for (ImageReferenceHolder addedHolder : holders)
        {
            if (holder.getUUID().equals(addedHolder.getUUID()) && holder.getDomain().getUUID().equals(addedHolder.getDomain().getUUID())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void setImageBaseName(String name)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getContentRef()
    {
        if (imageTypeBO.getSourceType().equals("External")) 
        {
            String prefix = imageTypeBO.getCompiledPrefix();
            if (prefix.endsWith('/' + imageTypeBO.getID() + "/"))
            {
                return imageTypeBO.getCompiledPrefix() + getImageBaseName();                
            }
            else
            {
                return imageTypeBO.getCompiledPrefix() + imageTypeBO.getID() + "/" + getImageBaseName();
            }
        }
        else
        {
            String imageRef =  getImageTypeBO().getID() + "/" + imageBaseName;
            // create effective url
            String effectiveUrl = URLUtils.createContentURL(imageRef);
            return effectiveUrl;            
        }
    }

    @Override
    public boolean addImageReferenceHolder(ImageReferenceHolder holder)
    {
        if (null == holder) {
            throw new IllegalArgumentException("Argument 'holder' must not be null.");
        }
        return holders.add(holder);
    }

}
