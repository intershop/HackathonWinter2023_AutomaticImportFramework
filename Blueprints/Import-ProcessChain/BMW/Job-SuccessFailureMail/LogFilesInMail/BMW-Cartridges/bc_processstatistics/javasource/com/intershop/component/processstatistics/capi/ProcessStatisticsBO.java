package com.intershop.component.processstatistics.capi;

import java.util.Date;

import com.intershop.beehive.businessobject.capi.BusinessObject;
import com.intershop.beehive.core.capi.locking.Process;

/**
 * This defines the business interface methods for ProcessStatistics as defined in the object model.
 * 
 * The ProcessStatisticsBO business interface is used for statistics information
 * about any kind of processes.
 * It is usually used for collecting information about all replication processes
 * or about all import process chains.
 * There is currently no automatic collection of any data. Instead, one has to
 * use the pipelets or similar code to create according objects.
 * 
 * @author t.koerbs@intershop.de
 * @version 1.0, 2016-Nov-22
 * @since 3.3.2
 * 
 *        Initial version.
 */
public interface ProcessStatisticsBO extends BusinessObject
{
    /**
     * The type of the process. Used to group processes of the same type when evaluating statistics.
     *
     * @return Usually something like: "Import", "Replication"; Cannot be null.
     */
    public String getType();

    /**
     * The name of the process.
     * Usually the name of the scheduled job or the replication process id.
     *
     * @return The name of the process; Cannot be null.
     */
    public String getName();

    /**
     * The name of the process.
     *
     * @param name  Usually the name of the scheduled job or the replication process id; Cannot be null.
     */
    public void setName(String name);

    /**
     * The start date of the process.
     *
     * @return The start date of the process; Cannot be null.
     */
    public Date getStartDate();

    /**
     * Sets the start date of the process.
     *
     * @param startDate  The start date of the process; Cannot be null.
     */
    public void setStartDate(Date startDate);

    /**
     * The end date of the process.
     *
     * @return The end date of the process; Can be null.
     */
    public Date getEndDate();

    /**
     * Sets the end date of the process.
     *
     * @param endDate  The end date of the process; Can be null.
     */
    public void setEndDate(Date endDate);

    /**
     * The result code of the process.
     * Usually something like: "SUCCESS", "WARNING", "FAILURE", "ERROR", "NOTFOUND", "INTERRUPTED".
     * See also process chain errors (&lt;p:ignore&gt;).
     *
     * @return The result code of the process; Can be null.
     */
    public String getResult();

    /**
     * Sets the result code of the process.
     * See also process chain errors (&lt;p:ignore&gt;).
     *
     * @param result  Usually something like: "SUCCESS", "WARNING", "FAILURE", "ERROR", "NOTFOUND", "INTERRUPTED"; Can be null.
     */
    public void setResult(String result);

    /**
     * The underlying process, if any.
     *
     * @return The underlying process; Can be null.
     */
    public Process getProcess();

    /**
     * Sets the underlying process.
     *
     * @param process The underlying process; Can be null.
     */
    public void setProcess(Process process);
}
