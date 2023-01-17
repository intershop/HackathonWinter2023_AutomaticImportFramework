package com.zamro.component.internal.automation;

import java.io.File;

import com.zamro.component.capi.automation.FileTransferSession;
import com.zamro.component.capi.automation.FileTransferSessionFactory;

/**
 * A basic implementation of a {@link FileTransferSessionFactory} offering setters for all configuration values.
 * <p>
 * See also: {@link ConfigurationBasedSftpFileTransferSessionFactory}
 * 
 * @author JCMeyer
 *
 */
public class BasicSftpFileTransferSessionFactory implements FileTransferSessionFactory
{
    protected String  host = null;
    protected Integer port = null;

    protected String  user = null;
    protected String  password = null;

    protected File    privateKeyFile = null;
    protected String  privateKeyPassword = null;
    protected File    knownHostsFile = null;

    /**
     * Sets the host name or address of the remote system. Usually set via component framework.
     * 
     * @param host the host name or address
     */
    public void setHost(String sftpHost)
    {
        this.host = sftpHost;
    }

    /**
     * Sets the port of the remote system. Usually set via component framework.
     * 
     * @param port the port the remote system listens on
     */
    public void setPort(Integer sftpPort)
    {
        this.port = sftpPort;
    }

    /**
     * Sets the name of the remote user executing the file operations. Usually set via component framework.
     * 
     * @param user the user
     */
    public void setUser(String sftpUser)
    {
        this.user = sftpUser;
    }

    /**
     * Sets the password used for authentication against the remote system. Depending on the underlying implementation
     * a password or a private key can be used for authentication. Usually set via component framework.
     * <p>
     * Password based authentication should be considered as a last resort only.
     * 
     * @param password the password used for authentication
     */
    public void setPassword(String sftpPassword)
    {
        this.password = sftpPassword;
    }

    /**
     * Sets the private key file used for authentication against the remote system. Depending on the underlying
     * implementation a password or a private key can be used for authentication. Usually set via component framework.
     * 
     * @param privateKeyFile the private key file used for authentication
     */
    public void setPrivateKeyFile(File privateKeyFile)
    {
        this.privateKeyFile = privateKeyFile;
    }

    /**
     * The password used to access a password protected private key. Usually set via component framework.
     * 
     * @param privateKeyPassword the password used to access the private key
     */
    public void setPrivateKeyPassword(String privateKeyPassword)
    {
        this.privateKeyPassword = privateKeyPassword;
    }

    /**
     * Sets the known hosts file used for strict host key checking. Depending on the underlying implementation or
     * environment the file will be determined automatically or even not required at all. Usually set via component
     * framework.
     * 
     * @param knownHostsFile the known hosts file used for strict host key checking
     */
    public void setKnownHostsFile(File knownHostsFile)
    {
        this.knownHostsFile = knownHostsFile;
    }

    @Override
    public FileTransferSession createFileTransferSession() throws Exception
    {
        FileTransferSession session;
        if (null == privateKeyFile)
        {
            session = new SftpFileTransferSession(host, port, user, password);
        }
        else
        {
            session = new SftpFileTransferSession(host, port, user, privateKeyFile, privateKeyPassword, knownHostsFile);
        }
        return session;
    }

    @Override
    public FileTransferSession createFileTransferSession(String baseDirectory) throws Exception
    {
        FileTransferSession session = createFileTransferSession();
        session.setSessionBaseDirectory(baseDirectory);
        return session;
    }

}
