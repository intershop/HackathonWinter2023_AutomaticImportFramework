package com.zamro.component.capi.automation;

import java.util.List;

/**
 * A file transfer level interface to a run remote file operations in a session context. 
 * 
 * @author JCMeyer
 */
public interface FileTransferSession extends AutoCloseable
{
    /**
     * Sets an explicit base directory context for the remaining file operations being executed. If no such directory
     * is set the remote system typically sets the directory based on the remote user executing the operations. Usually
     * set via component framework.
     * <p>
     * See also {@link FileTransferSessionFactory#setUser(String)}
     * 
     * @param baseDirectory the base directory to be set
     * @throws Exception
     */
    public void setSessionBaseDirectory(String baseDirectory) throws Exception;

    /**
     * Gets the remote folders located under the base directory matching the provided pattern. Evaluation of the
     * pattern is defined by the underlying implementation.
     * <p>
     * See also: {@link #setSessionBaseDirectory(String)}
     * 
     * @param pattern the pattern to match the remote folders against
     * @return remote folders located under the base directory
     * @throws Exception
     */
    public List<String> getFolderElements(String pattern) throws Exception;

    /**
     * Copies the remote file defined through given source to the local location defined through given target. The
     * source path is evaluated relatively to the base directory.
     * <p>
     * See also: {@link #setSessionBaseDirectory(String)}
     * 
     * @param source the remote source file path
     * @param target the local target file path
     * @throws Exception
     */
    public void copyFile(String source, String target) throws Exception;

    /**
     * Adds the given prefix string to the name of the remote file defined through the given source folder and source
     * name. The source folder path is evaluated relatively to the base directory.
     * <p>
     * See also: {@link #setSessionBaseDirectory(String)}
     * 
     * @param sourceFolder the remote source folder path
     * @param sourceFileName the remote source file name
     * @param prefix the prefix to be added to the file name
     * @throws Exception
     */
    public void prefixFile(String sourceFolder, String sourceFileName, String prefix) throws Exception;
}
