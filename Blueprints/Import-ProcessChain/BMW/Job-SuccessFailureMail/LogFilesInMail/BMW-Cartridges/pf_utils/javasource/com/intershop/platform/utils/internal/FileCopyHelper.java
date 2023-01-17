package com.intershop.platform.utils.internal;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import com.intershop.beehive.core.capi.log.Logger;


/**
 * This class contains static helper methods used to copy files.
 * 
 * @author thofbeck@intershop.de
 * @version 1.0, 02.06.2016
 * 
 */
public class FileCopyHelper
{
    public void copyFileToFolder (File source, File destination) {
        
        // copy complete file to archive folder.
        try {
            
            if (!destination.exists()) {
                destination.createNewFile();
            }
            
            FileInputStream fis = null;
            FileOutputStream fos = null;
            try {
                fis = new FileInputStream(source);
                fos = new FileOutputStream(destination);
                byte[] buffer = new byte[1024];

                while (true) {
                    int bytesRead = fis.read(buffer);
                    if (bytesRead <= -1) break;
                    fos.write(buffer, 0, bytesRead);
                }
            }
            finally {
                if (fis != null)
                    try {
                        fis.close();
                    } catch(IOException ex) {
                        Logger.debug(this, ex.getMessage(), ex);
                    }
                if (fos != null)
                    try {
                        fos.close();
                    } catch(IOException ex) {
                        Logger.debug(this, ex.getMessage(), ex);
                    }
            }
        } catch (FileNotFoundException ex) {
            // log the unusual exception the inform the system administrator (e.g. disc full)
            Logger.error(this, "bmw_ac_pim.Exception", ex);
        }
        catch (IOException ex) {
            // log the unusual exception the inform the system administrator (e.g. disc full)
            Logger.error(this, "bmw_ac_pim.Exception", ex);
        }
   }         
}
