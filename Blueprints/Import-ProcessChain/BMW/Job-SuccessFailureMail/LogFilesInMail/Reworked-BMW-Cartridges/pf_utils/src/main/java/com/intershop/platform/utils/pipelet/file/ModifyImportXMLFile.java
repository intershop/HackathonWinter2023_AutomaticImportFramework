package com.intershop.platform.utils.pipelet.file;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Iterator;

import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import com.intershop.beehive.core.capi.file.FileUtils;
import com.intershop.beehive.core.capi.log.Logger;
import com.intershop.beehive.core.capi.pipeline.Pipelet;
import com.intershop.beehive.core.capi.pipeline.PipeletExecutionException;
import com.intershop.beehive.core.capi.pipeline.PipelineDictionary;
import com.intershop.component.foundation.capi.upload.MVCFile;

/**
 * Pipelet modifies an xml import file and supports 2 import modi: - AddPrefix:
 * add a prefix to an existing element - AddElement: add a new xml element as
 * custom attribute
 * 
 * The file to modify, attribute name and attribute value needs to be provided
 * input parameter.
 * 
 * @author t.hofbeck@intershop.de
 * @version 1.0, 2015-04-04
 * @since 3.2.2
 * 
 */

public class ModifyImportXMLFile extends Pipelet
{

    private static String ADD_PREFIX = "AddPrefix";
    private static String ADD_ELEMENT = "AddElement";
    
    private static String CUSTOM_ATTRIBUTES = "custom-attributes";
    private static String CUSTOM_ATTRIBUTE = "custom-attribute";
    
    @Override
    public int execute(PipelineDictionary aPipelineDictionary) throws PipeletExecutionException
    {
        String cfg_mode = (String)getConfiguration().get("Mode");
        if (cfg_mode == null) {
            throw new PipeletExecutionException("Input parameter 'Prefix' is not valid.");            
        }

        MVCFile file = (MVCFile)aPipelineDictionary.get("File");
        if (file == null) {
            throw new PipeletExecutionException("Input parameter 'File' is not valid.");            
        }

        String attributeName = (String)aPipelineDictionary.get("AttributeName");
        if (attributeName == null) {
            throw new PipeletExecutionException("Input parameter 'AttributeName' is not valid.");            
        }
        
        String attributeValue = (String)aPipelineDictionary.get("AttributeValue");
        if (attributeValue == null) {
            throw new PipeletExecutionException("Input parameter 'AttributeValue' is not valid.");            
        }

        // create import file path 
        File impexFile = new File(FileUtils.getUnitImpexDirectory(file.getUnitDomainName()), "src" + File.separator + file.getDirectoryPath() + File.separator +  file.getFullName());
        File impexFileTemp = new File(FileUtils.getUnitImpexDirectory(file.getUnitDomainName()), "src" + File.separator + file.getDirectoryPath() + File.separator +  file.getName() + "_temp." + file.getExtension());

        try {

            XMLInputFactory inFactory = XMLInputFactory.newInstance();
            inFactory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, Boolean.FALSE);
            XMLEventReader eventReader = inFactory.createXMLEventReader(new FileInputStream(impexFile));
            XMLOutputFactory factory = XMLOutputFactory.newInstance();

            FileOutputStream fos = new FileOutputStream(impexFileTemp);
            XMLEventWriter eventWriter = factory.createXMLEventWriter(fos, "UTF-8");
            XMLEventFactory eventFactory = XMLEventFactory.newInstance();            
            
            while (eventReader.hasNext()) {
                XMLEvent event = eventReader.nextEvent();
                
                // perform action on start element
                if (event.getEventType() == XMLEvent.START_ELEMENT) {
                    StartElement startElement = event.asStartElement();
                    
                    // is ADD_PREFIX mode
                    if (cfg_mode.equals(ADD_PREFIX)){
                        eventWriter.add(eventFactory.createStartElement("", null, startElement.getName().getLocalPart()));
                        // modify attributes
                        @SuppressWarnings("unchecked")
                        Iterator<Attribute> attributes = startElement.getAttributes();
                        while (attributes.hasNext()) {
                            Attribute attribute = attributes.next();
                            
                            // is the current attribute the one which needs to be edited
                            if (attributeName.equals(attribute.getName().toString())) {
                                eventWriter.add(eventFactory.createAttribute(attribute.getName(), attributeValue + attribute.getValue()));
                            } else {
                                eventWriter.add(attribute);
                            }
                        }

                        // check for the element which needs to be endited
                        if (startElement.getName().getLocalPart().equals(attributeName)) {
                            XMLEvent newTextEvent = eventFactory.createCharacters(attributeValue + eventReader.nextEvent().asCharacters().getData());
                            eventWriter.add(newTextEvent);
                        }
                        
                    } else {
                        // ok, we're in the ADD_ELEMENT mode
                        
                        eventWriter.add(startElement);

                        // append new element after opening custom-attributes block
                        if (startElement.getName().getLocalPart().equals(CUSTOM_ATTRIBUTES)) {
                            eventWriter.add(eventFactory.createStartElement("", null, CUSTOM_ATTRIBUTE));
                            eventWriter.add(eventFactory.createAttribute("dt:dt", "string"));
                            eventWriter.add(eventFactory.createAttribute("name", attributeName));
                            eventWriter.add(eventFactory.createCharacters(attributeValue));
                            eventWriter.add(eventFactory.createEndElement("", null, CUSTOM_ATTRIBUTE));
                        }
                    }
                } else {
                    eventWriter.add(event);  
                }
            }
            eventWriter.flush();
            eventWriter.close();
            fos.flush();
            fos.close();
            eventReader.close();
            
            Files.copy(impexFileTemp.toPath(), impexFile.toPath(), REPLACE_EXISTING);
            Files.delete(impexFileTemp.toPath());
                        
        } catch (IOException ioe) {
            Logger.error(this, "Error on modifying xml import file", ioe);
        } catch(XMLStreamException xse){
            Logger.error(this, "Error on modifying xml import file", xse);
        }
        
        return PIPELET_NEXT;
    }

}
