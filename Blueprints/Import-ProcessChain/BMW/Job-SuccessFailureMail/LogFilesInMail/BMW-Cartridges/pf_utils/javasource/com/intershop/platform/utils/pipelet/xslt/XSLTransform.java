package com.intershop.platform.utils.pipelet.xslt;

import java.io.File;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import com.intershop.beehive.core.capi.pipeline.Pipelet;
import com.intershop.beehive.core.capi.pipeline.PipeletExecutionException;
import com.intershop.beehive.core.capi.pipeline.PipelineDictionary;

/**
 * This pipelet is used to execute an XSL 1.0 Transformation on the given
 * input XML fileusing the given style sheet file. The transformation result
 * is written to the provided output XML file.
 * 
 * @author JCMeyer
 * @version 1.0, 2015-09-14
 * @since 3.2.0
 */
public class XSLTransform extends Pipelet
{
    @Override
    public int execute(PipelineDictionary dict) throws PipeletExecutionException
    {
        File inputXMLFile = dict.getRequired("InputXMLFile");
        File outputXMLFile = dict.getRequired("OutputXMLFile");
        File styleSheetFile = dict.getRequired("StyleSheetFile");

        TransformerFactory factory = TransformerFactory.newInstance();
        Source xslt = new StreamSource(styleSheetFile);
        Transformer transformer;
        try
        {
            transformer = factory.newTransformer(xslt);
            Source text = new StreamSource(inputXMLFile);
            transformer.transform(text, new StreamResult(outputXMLFile));
        }
        catch (TransformerException ex)
        {
            throw new PipeletExecutionException(ex);
        }

        return PIPELET_NEXT;
    }
}
