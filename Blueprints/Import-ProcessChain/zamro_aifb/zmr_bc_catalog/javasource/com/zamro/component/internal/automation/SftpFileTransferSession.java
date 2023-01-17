package com.zamro.component.internal.automation;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import com.intershop.beehive.core.capi.log.Logger;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelSftp.LsEntry;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import com.zamro.component.capi.automation.FileTransferSession;

/**
 * @author JCMeyer
 */
public class SftpFileTransferSession implements FileTransferSession
{
    protected Session session = null;

    protected String baseDirectory = null;

    /**
     * This is a password string based session constructor.
     */
    public SftpFileTransferSession(String sftpHost, Integer sftpPort, String sftpUser, String sftpPassword) throws Exception
    {
        try
        {
            JSch jSch = new JSch();
            
            session = jSch.getSession(sftpUser, sftpHost, sftpPort);
            session.setPassword(sftpPassword);
            
            Logger.warn(this, "No host key checking in password mode. Consider using key based authentication for production environments.");
            session.setConfig("StrictHostKeyChecking", "no");
            allowTransportCompression();
        }
        catch (JSchException e)
        {
            throw new Exception(e.getCause());
        }
    }

    /**
     * This is a private key based session constructor.
     */
    public SftpFileTransferSession(String sftpHost, Integer sftpPort, String sftpUser, File sftpPrivateKeyFile, String sftpPrivateKeyPassword, File knownHostsFile) throws Exception
    {
        try
        {
            JSch jSch = new JSch();
            jSch.addIdentity(sftpPrivateKeyFile.getPath(), sftpPrivateKeyPassword.getBytes());
            if (null == knownHostsFile)
            {
                jSch.setKnownHosts(System.getProperty("user.home") + "/.ssh/known_hosts");
            }
            else
            {
                jSch.setKnownHosts(new FileInputStream(knownHostsFile));
            }
            session = jSch.getSession(sftpUser, sftpHost, sftpPort);
            session.setConfig("StrictHostKeyChecking", "yes");
            allowTransportCompression();
        }
        catch (JSchException e)
        {
            throw new Exception(e.getCause());
        }
    }

    @Override
    public void setSessionBaseDirectory(String baseDirectory) throws Exception
    {
        this.baseDirectory = baseDirectory;
    }

    @Override
    public List<String> getFolderElements(String pattern) throws Exception
    {
        List<String> folderElements;
        
        try
        {
            ChannelSftp channelSftp = setupChannelSftp();
            
            Vector<LsEntry> elements = channelSftp.ls(pattern);
            
            tearDownChannelSftp(channelSftp);
            
            folderElements = new ArrayList<String>(elements.size());
            elements.forEach(e -> folderElements.add(e.getFilename()));
             
            return folderElements;
        }
        catch (JSchException e)
        {
            throw new Exception(e);
        }
    }

    @Override
    public void copyFile(String source, String target) throws Exception
    {
        ChannelSftp channelSftp = setupChannelSftp();
        
        BufferedInputStream bis = new BufferedInputStream(channelSftp.get(source));
        File targetFile = new File(target);
        OutputStream os = new FileOutputStream(targetFile);
        BufferedOutputStream bos = new BufferedOutputStream(os);
        
        int readCount;
        byte[] buffer = new byte[32768];
        while ((readCount = bis.read(buffer)) > 0)
        {
            bos.write(buffer, 0, readCount);
        }
        
        bis.close();
        bos.close();
        
        tearDownChannelSftp(channelSftp);
    }

    @Override
    public void prefixFile(String sourceFolder, String sourceFileName, String prefix) throws Exception
    {
        ChannelSftp channelSftp = setupChannelSftp();
        
        channelSftp.rename(sourceFolder + '/' + sourceFileName, sourceFolder + '/' + prefix + sourceFileName);
        
        tearDownChannelSftp(channelSftp);
    }

    @Override
    public void close() throws Exception
    {
        session.disconnect();
    }

    /**
     * Helper method setting up the SFTP channel. It checks of the current session is in status 'connected' and does
     * the connect if required. It then opens the {@link ChannelSftp SFTP channel} and connects to it. If a session
     * base directory had been set it will change the directory accordingly before returning the set up channel.
     * <p>
     * See also: {@link #setSessionBaseDirectory(String)} 
     * 
     * @return the set up SFTP channel
     * @throws JSchException
     * @throws SftpException
     */
    protected ChannelSftp setupChannelSftp() throws JSchException, SftpException
    {
        if (! session.isConnected())
        {
            session.connect();
        }
        
        ChannelSftp channelSftp = (ChannelSftp) session.openChannel("sftp");
        channelSftp.connect();
        if (null != baseDirectory)
        {
            channelSftp.cd(baseDirectory);
        }
        
        return channelSftp;
    }

    /**
     * Helper method tearing down the given SFTP channel. Counterpart of {@link #setupChannelSftp()}.
     * 
     * @param channelSftp
     */
    protected void tearDownChannelSftp(ChannelSftp channelSftp)
    {
        channelSftp.disconnect();
    }

    /**
     * Helper method setting session configuration values telling the SFTP server to use transport compression when
     * available.
     */
    protected void allowTransportCompression()
    {
        session.setConfig("compression.s2c", "zlib@openssh.com,zlib,none");
        session.setConfig("compression.c2s", "zlib@openssh.com,zlib,none");
        session.setConfig("compression_level", "9");
    }

}
