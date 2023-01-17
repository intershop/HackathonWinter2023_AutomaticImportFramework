package com.zamro.component.capi.automation;

/**
 * A file transfer level factory to create a {@link FileTransferSession} running remote file operations in a session
 * context. 
 * 
 * @author JCMeyer
 */
public interface FileTransferSessionFactory
{
    /**
     * Creates a new file transfer session based on the values configured on factory level.
     * 
     * @return a new file transfer session
     * @throws Exception
     */
    public FileTransferSession createFileTransferSession() throws Exception;

    /**
     * Creates a new file transfer session based on the values configured on factory level. All file operations of the
     * session will be executed relatively to the provided base directory.
     * 
     * @param baseDirectory the base directory for file operations.
     * @return a new file transfer session
     * @throws Exception
     */
    public FileTransferSession createFileTransferSession(String baseDirectory) throws Exception;
}
