package com.zamro.component.internal.automation;

import java.io.File;

import com.intershop.beehive.configuration.capi.common.Configuration;
import com.intershop.beehive.core.capi.configuration.ConfigurationMgr;
import com.intershop.beehive.core.capi.domain.Domain;
import com.intershop.beehive.core.capi.request.Request;
import com.zamro.component.capi.automation.FileTransferSession;
import com.zamro.component.capi.automation.FileTransferSessionFactory;

/**
 * An implementation of a {@link FileTransferSessionFactory} reading its configuration values from the configuration
 * framework. This allows to keep multiple configurations for different target environments.
 * <p>
 * See also: {@link BasicSftpFileTransferSessionFactory}
 * 
 * @author JCMeyer
 *
 */
public class ConfigurationBasedSftpFileTransferSessionFactory implements FileTransferSessionFactory
{
    public static final String CONFIG_KEY_HOST = "host";

    public static final String CONFIG_KEY_PORT = "port";

    public static final String CONFIG_KEY_USER = "user";

    public static final String CONFIG_KEY_PASSWORD = "password";

    public static final String CONFIG_KEY_PRIVATE_KEY_FILE = "privateKeyFile";

    public static final String CONFIG_KEY_PRIVATE_KEY_PASSWORD = "privateKeyPassword";

    public static final String CONFIG_KEY_KNOWN_HOSTS_FILE = "knownHostsFile";

    protected String configurationKeyPrefix = null;

    /**
     * Sets the configuration key prefix used as a base during look-up of configuration values.
     * 
     * @param configurationKeyPrefix the configuration key prefix
     */
    public void setConfigurationKeyPrefix(String configurationKeyPrefix)
    {
        this.configurationKeyPrefix = configurationKeyPrefix;
    }

    @Override
    public FileTransferSession createFileTransferSession() throws Exception
    {
        Configuration configuration = loadConfiguration();

        String host = configuration.getString(CONFIG_KEY_HOST);
        Integer port = configuration.getInteger(CONFIG_KEY_PORT);

        String user = configuration.getString(CONFIG_KEY_USER);
        String password = configuration.getString(CONFIG_KEY_PASSWORD);

        File privateKeyFile = wrapByFile(configuration.getString(CONFIG_KEY_PRIVATE_KEY_FILE));
        String privateKeyPassword = configuration.getString(CONFIG_KEY_PRIVATE_KEY_PASSWORD);

        File knownHostsFile =  wrapByFile(configuration.getString(CONFIG_KEY_KNOWN_HOSTS_FILE));

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

    /**
     * Helper method loading the SFTP connection parameter configuration. Tries to make use of the request site domain.
     * 
     * @return the SFTP connection parameter configuration
     */
    protected Configuration loadConfiguration()
    {
        Configuration configuration = null;
        Domain requestSite = null;

        Request request = Request.getCurrent();
        if (null != request)
        {
            requestSite = request.getRequestSite();
        }
        if (null == requestSite)
        {
            configuration = ConfigurationMgr.getInstance().getConfiguration();
        }
        else
        {
            configuration = ConfigurationMgr.getInstance().getConfiguration(requestSite);
        }

        return configuration.subConfig(configurationKeyPrefix);
    }

    /**
     * Helper method returning a file object for a given optional pathname after applying a null check.
     * 
     * @param pathname the optional pathname to get a file object for
     * @return a file object or <code>null</code> in case no pathname has been provided
     */
    protected File wrapByFile(String pathname)
    {
        return pathname == null ? null : new File(pathname);
    }

}
