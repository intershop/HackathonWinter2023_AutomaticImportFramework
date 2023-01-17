package com.intershop.platform.utils.pipelet.process;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

import javax.inject.Inject;

import com.intershop.beehive.core.capi.common.CreateException;
import com.intershop.beehive.core.capi.common.FinderException;
import com.intershop.beehive.core.capi.domain.AttributeValue;
import com.intershop.beehive.core.capi.domain.Domain;
import com.intershop.beehive.core.capi.domain.PersistentObject;
import com.intershop.beehive.core.capi.job.JobConfiguration;
import com.intershop.beehive.core.capi.localization.LocaleInformation;
import com.intershop.beehive.core.capi.locking.Process;
import com.intershop.beehive.core.capi.locking.ProcessMgr;
import com.intershop.beehive.core.capi.log.Logger;
import com.intershop.beehive.core.capi.pipeline.Pipelet;
import com.intershop.beehive.core.capi.pipeline.PipeletExecutionException;
import com.intershop.beehive.core.capi.pipeline.PipelineDictionary;
import com.intershop.beehive.core.capi.pipeline.PipelineInitializationException;
import com.intershop.beehive.core.capi.user.User;
import com.intershop.beehive.foundation.quantity.Money;
import com.intershop.beehive.foundation.quantity.Quantity;

/**
 * Returns the process currently executed by this thread. If there is no such process then the error connector is used.
 * The implementation is a little strange as we cannot directly access the currently running processes of the current
 * thread. That's why we start and immediately stop a transient dummy process. In between we can access the parent
 * process of this "running" dummy - and this parent is the actual current process on top of the process stack. Since
 * the transient dummy process is not full featured, error log entries with "UnsupportedOperationException" may occur.
 * Anyway, the pipelet will ignore it and will continue execution. Nevertheless, there may be subsequent errors that
 * interrupt execution.
 */
public class GetCurrentProcess extends Pipelet
{
    @Inject
    private ProcessMgr processMgr;

    protected String pipeletName;
    protected String pipelineName;
    protected String errorMessageUnsupportedOperationException;

    /**
     * Constant used to access the pipeline dictionary with key 'CurrentProcess'
     * 
     * The class name defining the class to refresh the resource bundle for.
     */
    public static final String DN_CURRENT_PROCESS = "CurrentProcess";

    @Override
    public int execute(PipelineDictionary aPipelineDictionary) throws PipeletExecutionException
    {
        Process dummyProcess = new TransientProcessDummy();

        processMgr.startProcess(dummyProcess, "running");

        Process currentProcess = dummyProcess.getParent();

        processMgr.endProcess(dummyProcess, "ended");

        if (currentProcess != null)
        {
            aPipelineDictionary.put(DN_CURRENT_PROCESS, currentProcess);
            return PIPELET_NEXT;
        }
        else
        {
            return PIPELET_ERROR;
        }
    }

    class TransientProcessDummy implements Process
    {
        Process parentProcess = null;
        String state = "running";
        Date startDate = null;
        Date endDate = null;
        String uuid = "Transient_Dummy_Process.";

        @Override
        public void putAttributeValue(String paramString, AttributeValue paramAttributeValue) throws CreateException
        {
            Throwable ex = new UnsupportedOperationException();
            Logger.error(this, errorMessageUnsupportedOperationException, ex);
        }

        @Override
        public void putAttributeValue(AttributeValue paramAttributeValue) throws CreateException
        {
            Throwable ex = new UnsupportedOperationException();
            Logger.error(this, errorMessageUnsupportedOperationException, ex);
        }

        @Override
        public void putBoolean(String paramString, Boolean paramBoolean)
        {
            Throwable ex = new UnsupportedOperationException();
            Logger.error(this, errorMessageUnsupportedOperationException, ex);
        }

        @Override
        public void putDecimal(String paramString, BigDecimal paramBigDecimal)
        {
            Throwable ex = new UnsupportedOperationException();
            Logger.error(this, errorMessageUnsupportedOperationException, ex);
        }

        @Override
        public void putDate(String paramString, Date paramDate)
        {
            Throwable ex = new UnsupportedOperationException();
            Logger.error(this, errorMessageUnsupportedOperationException, ex);
        }

        @Override
        public void putDouble(String paramString, Double paramDouble)
        {
            Throwable ex = new UnsupportedOperationException();
            Logger.error(this, errorMessageUnsupportedOperationException, ex);
        }

        @Override
        public void putInteger(String paramString, Integer paramInteger)
        {
            Throwable ex = new UnsupportedOperationException();
            Logger.error(this, errorMessageUnsupportedOperationException, ex);
        }

        @Override
        public void putLocalizedBoolean(String paramString, Boolean paramBoolean, LocaleInformation paramLocaleInformation)
        {
            Throwable ex = new UnsupportedOperationException();
            Logger.error(this, errorMessageUnsupportedOperationException, ex);
        }

        @Override
        public void putLocalizedDecimal(String paramString, BigDecimal paramBigDecimal, LocaleInformation paramLocaleInformation)
        {
            Throwable ex = new UnsupportedOperationException();
            Logger.error(this, errorMessageUnsupportedOperationException, ex);
        }

        @Override
        public void putLocalizedDate(String paramString, Date paramDate, LocaleInformation paramLocaleInformation)
        {
            Throwable ex = new UnsupportedOperationException();
            Logger.error(this, errorMessageUnsupportedOperationException, ex);
        }

        @Override
        public void putLocalizedDouble(String paramString, Double paramDouble, LocaleInformation paramLocaleInformation)
        {
            Throwable ex = new UnsupportedOperationException();
            Logger.error(this, errorMessageUnsupportedOperationException, ex);
        }

        @Override
        public void putLocalizedInteger(String paramString, Integer paramInteger, LocaleInformation paramLocaleInformation)
        {
            Throwable ex = new UnsupportedOperationException();
            Logger.error(this, errorMessageUnsupportedOperationException, ex);
        }

        @Override
        public void putLocalizedLong(String paramString, Long paramLong, LocaleInformation paramLocaleInformation)
        {
            Throwable ex = new UnsupportedOperationException();
            Logger.error(this, errorMessageUnsupportedOperationException, ex);
        }

        @Override
        public void putLocalizedMoney(String paramString, Money paramMoney, LocaleInformation paramLocaleInformation)
        {
            Throwable ex = new UnsupportedOperationException();
            Logger.error(this, errorMessageUnsupportedOperationException, ex);
        }

        @Override
        public void putLocalizedObject(String paramString, PersistentObject paramPersistentObject, LocaleInformation paramLocaleInformation)
        {
            Throwable ex = new UnsupportedOperationException();
            Logger.error(this, errorMessageUnsupportedOperationException, ex);
        }

        @Override
        public void putLocalizedQuantity(String paramString, Quantity paramQuantity, LocaleInformation paramLocaleInformation)
        {
            Throwable ex = new UnsupportedOperationException();
            Logger.error(this, errorMessageUnsupportedOperationException, ex);
        }

        @Override
        public void putLocalizedString(String paramString1, String paramString2, LocaleInformation paramLocaleInformation)
        {
            Throwable ex = new UnsupportedOperationException();
            Logger.error(this, errorMessageUnsupportedOperationException, ex);
        }

        @Override
        public void putLocalizedText(String paramString1, String paramString2, LocaleInformation paramLocaleInformation)
        {
            Throwable ex = new UnsupportedOperationException();
            Logger.error(this, errorMessageUnsupportedOperationException, ex);
        }

        @Override
        public void putLong(String paramString, Long paramLong)
        {
            Throwable ex = new UnsupportedOperationException();
            Logger.error(this, errorMessageUnsupportedOperationException, ex);
        }

        @Override
        public void putMoney(String paramString, Money paramMoney)
        {
            Throwable ex = new UnsupportedOperationException();
            Logger.error(this, errorMessageUnsupportedOperationException, ex);
        }

        @Override
        public void putMultipleBooleans(String paramString, Iterator<Boolean> paramIterator)
        {
            Throwable ex = new UnsupportedOperationException();
            Logger.error(this, errorMessageUnsupportedOperationException, ex);
        }

        @Override
        public void putMultipleDates(String paramString, Iterator<Date> paramIterator)
        {
            Throwable ex = new UnsupportedOperationException();
            Logger.error(this, errorMessageUnsupportedOperationException, ex);
        }

        @Override
        public void putMultipleDecimals(String paramString, Iterator<BigDecimal> paramIterator)
        {
            Throwable ex = new UnsupportedOperationException();
            Logger.error(this, errorMessageUnsupportedOperationException, ex);
        }

        @Override
        public void putMultipleDoubles(String paramString, Iterator<Double> paramIterator)
        {
            Throwable ex = new UnsupportedOperationException();
            Logger.error(this, errorMessageUnsupportedOperationException, ex);
        }

        @Override
        public void putMultipleIntegers(String paramString, Iterator<Integer> paramIterator)
        {
            Throwable ex = new UnsupportedOperationException();
            Logger.error(this, errorMessageUnsupportedOperationException, ex);
        }

        @Override
        public void putMultipleLocalizedBooleans(String paramString, Iterator<Boolean> paramIterator, LocaleInformation paramLocaleInformation)
        {
            Throwable ex = new UnsupportedOperationException();
            Logger.error(this, errorMessageUnsupportedOperationException, ex);
        }

        @Override
        public void putMultipleLocalizedDates(String paramString, Iterator<Date> paramIterator, LocaleInformation paramLocaleInformation)
        {
            Throwable ex = new UnsupportedOperationException();
            Logger.error(this, errorMessageUnsupportedOperationException, ex);
        }

        @Override
        public void putMultipleLocalizedDecimals(String paramString, Iterator<BigDecimal> paramIterator, LocaleInformation paramLocaleInformation)
        {
            Throwable ex = new UnsupportedOperationException();
            Logger.error(this, errorMessageUnsupportedOperationException, ex);
        }

        @Override
        public void putMultipleLocalizedDoubles(String paramString, Iterator<Double> paramIterator, LocaleInformation paramLocaleInformation)
        {
            Throwable ex = new UnsupportedOperationException();
            Logger.error(this, errorMessageUnsupportedOperationException, ex);
        }

        @Override
        public void putMultipleLocalizedIntegers(String paramString, Iterator<Integer> paramIterator, LocaleInformation paramLocaleInformation)
        {
            Throwable ex = new UnsupportedOperationException();
            Logger.error(this, errorMessageUnsupportedOperationException, ex);
        }

        @Override
        public void putMultipleLocalizedLongs(String paramString, Iterator<Long> paramIterator, LocaleInformation paramLocaleInformation)
        {
            Throwable ex = new UnsupportedOperationException();
            Logger.error(this, errorMessageUnsupportedOperationException, ex);
        }

        @Override
        public void putMultipleLocalizedStrings(String paramString, Iterator<String> paramIterator, LocaleInformation paramLocaleInformation)
        {
            Throwable ex = new UnsupportedOperationException();
            Logger.error(this, errorMessageUnsupportedOperationException, ex);
        }

        @Override
        public void putMultipleLongs(String paramString, Iterator<Long> paramIterator)
        {
            Throwable ex = new UnsupportedOperationException();
            Logger.error(this, errorMessageUnsupportedOperationException, ex);
        }

        @Override
        public void putMultipleStrings(String paramString, Iterator<String> paramIterator)
        {
            Throwable ex = new UnsupportedOperationException();
            Logger.error(this, errorMessageUnsupportedOperationException, ex);
        }

        @Override
        public void putObject(String paramString, PersistentObject paramPersistentObject)
        {
            Throwable ex = new UnsupportedOperationException();
            Logger.error(this, errorMessageUnsupportedOperationException, ex);
        }

        @Override
        public void putQuantity(String paramString, Quantity paramQuantity)
        {
            Throwable ex = new UnsupportedOperationException();
            Logger.error(this, errorMessageUnsupportedOperationException, ex);
        }

        @Override
        public void putString(String paramString1, String paramString2)
        {
            Throwable ex = new UnsupportedOperationException();
            Logger.error(this, errorMessageUnsupportedOperationException, ex);
        }

        @Override
        public void putText(String paramString1, String paramString2)
        {
            Throwable ex = new UnsupportedOperationException();
            Logger.error(this, errorMessageUnsupportedOperationException, ex);
        }

        @Override
        public void removeAttribute(String paramString)
        {
            Throwable ex = new UnsupportedOperationException();
            Logger.error(this, errorMessageUnsupportedOperationException, ex);
        }

        @Override
        public void removeLocalizedAttribute(String paramString, LocaleInformation paramLocaleInformation)
        {
            Throwable ex = new UnsupportedOperationException();
            Logger.error(this, errorMessageUnsupportedOperationException, ex);
        }

        @Override
        public void setLastModified(Date paramDate)
        {
            Throwable ex = new UnsupportedOperationException();
            Logger.error(this, errorMessageUnsupportedOperationException, ex);
        }

        @Override
        public void setDomain(Domain paramDomain)
        {
            Throwable ex = new UnsupportedOperationException();
            Logger.error(this, errorMessageUnsupportedOperationException, ex);
        }

        @Override
        public String getUUID()
        {
            return uuid;
        }

        @Override
        public void setDomainID(String paramString)
        {
            Throwable ex = new UnsupportedOperationException();
            Logger.error(this, errorMessageUnsupportedOperationException, ex);
        }

        @Override
        public void setProgressSize(int paramInt)
        {
            Throwable ex = new UnsupportedOperationException();
            Logger.error(this, errorMessageUnsupportedOperationException, ex);
        }

        @Override
        public void setProgressValue(int paramInt)
        {
            Throwable ex = new UnsupportedOperationException();
            Logger.error(this, errorMessageUnsupportedOperationException, ex);
        }

        @Override
        public Date getEndDate()
        {
            return endDate;
        }

        @Override
        public String getName()
        {
            return getClass().getName();
        }

        @Override
        public Process getParent()
        {
            return parentProcess;
        }

        @Override
        public boolean getParentNull()
        {
            return parentProcess == null;
        }

        @Override
        public Date getStartDate()
        {
            return startDate;
        }

        @Override
        public String getState()
        {
            return state;
        }

        @Override
        public String getType()
        {
            return "dummy";
        }

        @Override
        public void setDescription(String paramString, LocaleInformation paramLocaleInformation)
        {
            Throwable ex = new UnsupportedOperationException();
            Logger.error(this, errorMessageUnsupportedOperationException, ex);
        }

        @Override
        public void setEndDate(Date paramDate)
        {
            endDate = paramDate;
        }

        @Override
        public void setName(String paramString)
        {
            Throwable ex = new UnsupportedOperationException();
            Logger.error(this, errorMessageUnsupportedOperationException, ex);
        }

        @Override
        public void setParent(Process paramProcess)
        {
            parentProcess = paramProcess;
        }

        @Override
        public void setParentNull(boolean paramBoolean)
        {
            parentProcess = null;
        }

        @Override
        public void setScheduledDate(Date paramDate)
        {
            Throwable ex = new UnsupportedOperationException();
            Logger.error(this, errorMessageUnsupportedOperationException, ex);
        }

        @Override
        public void setScheduledDateNull(boolean paramBoolean)
        {
            Throwable ex = new UnsupportedOperationException();
            Logger.error(this, errorMessageUnsupportedOperationException, ex);
        }

        @Override
        public void setStartDate(Date paramDate)
        {
            startDate = paramDate;
        }

        @Override
        public void setState(String paramString)
        {
            state = paramString;
        }

        @Override
        public void setType(String paramString)
        {
            Throwable ex = new UnsupportedOperationException();
            Logger.error(this, errorMessageUnsupportedOperationException, ex);
        }

        @Override
        public void setUser(User paramUser)
        {
            Throwable ex = new UnsupportedOperationException();
            Logger.error(this, errorMessageUnsupportedOperationException, ex);
        }

        @Override
        public Iterator<AttributeValue> createAttributeValuesIterator()
        {
            Throwable ex = new UnsupportedOperationException();
            Logger.error(this, errorMessageUnsupportedOperationException, ex);
            return null;
        }

        @Override
        public int getAttributeValuesCount()
        {
            Throwable ex = new UnsupportedOperationException();
            Logger.error(this, errorMessageUnsupportedOperationException, ex);
            return 0;
        }

        @Override
        public boolean isInAttributeValues(AttributeValue paramAttributeValue)
        {
            Throwable ex = new UnsupportedOperationException();
            Logger.error(this, errorMessageUnsupportedOperationException, ex);
            return false;
        }

        @Override
        public String getFactoryName()
        {
            Throwable ex = new UnsupportedOperationException();
            Logger.error(this, errorMessageUnsupportedOperationException, ex);
            return null;
        }

        @Override
        public boolean isIdenticalWith(PersistentObject paramPersistentObject)
        {
            Throwable ex = new UnsupportedOperationException();
            Logger.error(this, errorMessageUnsupportedOperationException, ex);
            return false;
        }

        @Override
        public Date getLastModified()
        {
            Throwable ex = new UnsupportedOperationException();
            Logger.error(this, errorMessageUnsupportedOperationException, ex);
            return null;
        }

        @Override
        public Domain getDomain()
        {
            Throwable ex = new UnsupportedOperationException();
            Logger.error(this, errorMessageUnsupportedOperationException, ex);
            return null;
        }

        @Override
        public String getDomainID()
        {
            Throwable ex = new UnsupportedOperationException();
            Logger.error(this, errorMessageUnsupportedOperationException, ex);
            return null;
        }

        @Override
        public boolean containsAttribute(String paramString)
        {
            Throwable ex = new UnsupportedOperationException();
            Logger.error(this, errorMessageUnsupportedOperationException, ex);
            return false;
        }

        @Override
        public Iterator<String> createAttributeNamesIterator()
        {
            Throwable ex = new UnsupportedOperationException();
            Logger.error(this, errorMessageUnsupportedOperationException, ex);
            return null;
        }

        @Override
        public Iterator<AttributeValue> createAttributeValuesIterator(String paramString)
        {
            Throwable ex = new UnsupportedOperationException();
            Logger.error(this, errorMessageUnsupportedOperationException, ex);
            return null;
        }

        @Override
        public Object getAttribute(String paramString)
        {
            Throwable ex = new UnsupportedOperationException();
            Logger.error(this, errorMessageUnsupportedOperationException, ex);
            return null;
        }

        @Override
        public int getAttributeType(String paramString) throws FinderException
        {
            Throwable ex = new UnsupportedOperationException();
            Logger.error(this, errorMessageUnsupportedOperationException, ex);
            return 0;
        }

        @Override
        public AttributeValue getAttributeValue(String paramString)
        {
            Throwable ex = new UnsupportedOperationException();
            Logger.error(this, errorMessageUnsupportedOperationException, ex);
            return null;
        }

        @Override
        public AttributeValue getAttributeValue(String paramString, LocaleInformation paramLocaleInformation)
        {
            Throwable ex = new UnsupportedOperationException();
            Logger.error(this, errorMessageUnsupportedOperationException, ex);
            return null;
        }

        @Override
        public Boolean getBoolean(String paramString)
        {
            Throwable ex = new UnsupportedOperationException();
            Logger.error(this, errorMessageUnsupportedOperationException, ex);
            return null;
        }

        @Override
        public Date getDate(String paramString)
        {
            Throwable ex = new UnsupportedOperationException();
            Logger.error(this, errorMessageUnsupportedOperationException, ex);
            return null;
        }

        @Override
        public BigDecimal getDecimal(String paramString)
        {
            Throwable ex = new UnsupportedOperationException();
            Logger.error(this, errorMessageUnsupportedOperationException, ex);
            return null;
        }

        @Override
        public Double getDouble(String paramString)
        {
            Throwable ex = new UnsupportedOperationException();
            Logger.error(this, errorMessageUnsupportedOperationException, ex);
            return null;
        }

        @Override
        public Integer getInteger(String paramString)
        {
            Throwable ex = new UnsupportedOperationException();
            Logger.error(this, errorMessageUnsupportedOperationException, ex);
            return null;
        }

        @Override
        public Object getLocalizedAttribute(String paramString, LocaleInformation paramLocaleInformation)
        {
            Throwable ex = new UnsupportedOperationException();
            Logger.error(this, errorMessageUnsupportedOperationException, ex);
            return null;
        }

        @Override
        public Boolean getLocalizedBoolean(String paramString, LocaleInformation paramLocaleInformation)
        {
            Throwable ex = new UnsupportedOperationException();
            Logger.error(this, errorMessageUnsupportedOperationException, ex);
            return null;
        }

        @Override
        public BigDecimal getLocalizedDecimal(String paramString, LocaleInformation paramLocaleInformation)
        {
            Throwable ex = new UnsupportedOperationException();
            Logger.error(this, errorMessageUnsupportedOperationException, ex);
            return null;
        }

        @Override
        public Date getLocalizedDate(String paramString, LocaleInformation paramLocaleInformation)
        {
            Throwable ex = new UnsupportedOperationException();
            Logger.error(this, errorMessageUnsupportedOperationException, ex);
            return null;
        }

        @Override
        public Double getLocalizedDouble(String paramString, LocaleInformation paramLocaleInformation)
        {
            Throwable ex = new UnsupportedOperationException();
            Logger.error(this, errorMessageUnsupportedOperationException, ex);
            return null;
        }

        @Override
        public Integer getLocalizedInteger(String paramString, LocaleInformation paramLocaleInformation)
        {
            Throwable ex = new UnsupportedOperationException();
            Logger.error(this, errorMessageUnsupportedOperationException, ex);
            return null;
        }

        @Override
        public Long getLocalizedLong(String paramString, LocaleInformation paramLocaleInformation)
        {
            Throwable ex = new UnsupportedOperationException();
            Logger.error(this, errorMessageUnsupportedOperationException, ex);
            return null;
        }

        @Override
        public Money getLocalizedMoney(String paramString, LocaleInformation paramLocaleInformation)
        {
            Throwable ex = new UnsupportedOperationException();
            Logger.error(this, errorMessageUnsupportedOperationException, ex);
            return null;
        }

        @Override
        public PersistentObject getLocalizedObject(String paramString, LocaleInformation paramLocaleInformation)
        {
            Throwable ex = new UnsupportedOperationException();
            Logger.error(this, errorMessageUnsupportedOperationException, ex);
            return null;
        }

        @Override
        public Quantity getLocalizedQuantity(String paramString, LocaleInformation paramLocaleInformation)
        {
            Throwable ex = new UnsupportedOperationException();
            Logger.error(this, errorMessageUnsupportedOperationException, ex);
            return null;
        }

        @Override
        public String getLocalizedString(String paramString, LocaleInformation paramLocaleInformation)
        {
            Throwable ex = new UnsupportedOperationException();
            Logger.error(this, errorMessageUnsupportedOperationException, ex);
            return null;
        }

        @Override
        public String getLocalizedText(String paramString, LocaleInformation paramLocaleInformation)
        {
            Throwable ex = new UnsupportedOperationException();
            Logger.error(this, errorMessageUnsupportedOperationException, ex);
            return null;
        }

        @Override
        public Long getLong(String paramString)
        {
            Throwable ex = new UnsupportedOperationException();
            Logger.error(this, errorMessageUnsupportedOperationException, ex);
            return null;
        }

        @Override
        public Money getMoney(String paramString)
        {
            Throwable ex = new UnsupportedOperationException();
            Logger.error(this, errorMessageUnsupportedOperationException, ex);
            return null;
        }

        @Override
        public <T> Iterator<T> getMultipleAttributes(String paramString)
        {
            Throwable ex = new UnsupportedOperationException();
            Logger.error(this, errorMessageUnsupportedOperationException, ex);
            return null;
        }

        @Override
        public int getMultipleAttributesCount(String paramString)
        {
            Throwable ex = new UnsupportedOperationException();
            Logger.error(this, errorMessageUnsupportedOperationException, ex);
            return 0;
        }

        @Override
        public Iterator<Boolean> getMultipleBooleans(String paramString)
        {
            Throwable ex = new UnsupportedOperationException();
            Logger.error(this, errorMessageUnsupportedOperationException, ex);
            return null;
        }

        @Override
        public Iterator<Date> getMultipleDates(String paramString)
        {
            Throwable ex = new UnsupportedOperationException();
            Logger.error(this, errorMessageUnsupportedOperationException, ex);
            return null;
        }

        @Override
        public Iterator<BigDecimal> getMultipleDecimals(String paramString)
        {
            Throwable ex = new UnsupportedOperationException();
            Logger.error(this, errorMessageUnsupportedOperationException, ex);
            return null;
        }

        @Override
        public Iterator<Double> getMultipleDoubles(String paramString)
        {
            Throwable ex = new UnsupportedOperationException();
            Logger.error(this, errorMessageUnsupportedOperationException, ex);
            return null;
        }

        @Override
        public Iterator<Integer> getMultipleIntegers(String paramString)
        {
            Throwable ex = new UnsupportedOperationException();
            Logger.error(this, errorMessageUnsupportedOperationException, ex);
            return null;
        }

        @Override
        public Iterator<AttributeValue> getMultipleLocalizedAttributes(String paramString, LocaleInformation paramLocaleInformation)
        {
            Throwable ex = new UnsupportedOperationException();
            Logger.error(this, errorMessageUnsupportedOperationException, ex);
            return null;
        }

        @Override
        public int getMultipleLocalizedAttributesCount(String paramString, LocaleInformation paramLocaleInformation)
        {
            Throwable ex = new UnsupportedOperationException();
            Logger.error(this, errorMessageUnsupportedOperationException, ex);
            return 0;
        }

        @Override
        public Iterator<Boolean> getMultipleLocalizedBooleans(String paramString, LocaleInformation paramLocaleInformation)
        {
            Throwable ex = new UnsupportedOperationException();
            Logger.error(this, errorMessageUnsupportedOperationException, ex);
            return null;
        }

        @Override
        public Iterator<Date> getMultipleLocalizedDates(String paramString, LocaleInformation paramLocaleInformation)
        {
            Throwable ex = new UnsupportedOperationException();
            Logger.error(this, errorMessageUnsupportedOperationException, ex);
            return null;
        }

        @Override
        public Iterator<BigDecimal> getMultipleLocalizedDecimals(String paramString, LocaleInformation paramLocaleInformation)
        {
            Throwable ex = new UnsupportedOperationException();
            Logger.error(this, errorMessageUnsupportedOperationException, ex);
            return null;
        }

        @Override
        public Iterator<Double> getMultipleLocalizedDoubles(String paramString, LocaleInformation paramLocaleInformation)
        {
            Throwable ex = new UnsupportedOperationException();
            Logger.error(this, errorMessageUnsupportedOperationException, ex);
            return null;
        }

        @Override
        public Iterator<Integer> getMultipleLocalizedIntegers(String paramString, LocaleInformation paramLocaleInformation)
        {
            Throwable ex = new UnsupportedOperationException();
            Logger.error(this, errorMessageUnsupportedOperationException, ex);
            return null;
        }

        @Override
        public Iterator<Long> getMultipleLocalizedLongs(String paramString, LocaleInformation paramLocaleInformation)
        {
            Throwable ex = new UnsupportedOperationException();
            Logger.error(this, errorMessageUnsupportedOperationException, ex);
            return null;
        }

        @Override
        public Iterator<String> getMultipleLocalizedStrings(String paramString, LocaleInformation paramLocaleInformation)
        {
            Throwable ex = new UnsupportedOperationException();
            Logger.error(this, errorMessageUnsupportedOperationException, ex);
            return null;
        }

        @Override
        public Iterator<Long> getMultipleLongs(String paramString)
        {
            Throwable ex = new UnsupportedOperationException();
            Logger.error(this, errorMessageUnsupportedOperationException, ex);
            return null;
        }

        @Override
        public Iterator<String> getMultipleStrings(String paramString)
        {
            Throwable ex = new UnsupportedOperationException();
            Logger.error(this, errorMessageUnsupportedOperationException, ex);
            return null;
        }

        @Override
        public PersistentObject getObject(String paramString)
        {
            Throwable ex = new UnsupportedOperationException();
            Logger.error(this, errorMessageUnsupportedOperationException, ex);
            return null;
        }

        @Override
        public Quantity getQuantity(String paramString)
        {
            Throwable ex = new UnsupportedOperationException();
            Logger.error(this, errorMessageUnsupportedOperationException, ex);
            return null;
        }

        @Override
        public String getString(String paramString)
        {
            Throwable ex = new UnsupportedOperationException();
            Logger.error(this, errorMessageUnsupportedOperationException, ex);
            return null;
        }

        @Override
        public String getText(String paramString)
        {
            Throwable ex = new UnsupportedOperationException();
            Logger.error(this, errorMessageUnsupportedOperationException, ex);
            return null;
        }

        @Override
        public boolean isAttributeLocalized(String paramString) throws FinderException
        {
            Throwable ex = new UnsupportedOperationException();
            Logger.error(this, errorMessageUnsupportedOperationException, ex);
            return false;
        }

        @Override
        public Iterator<AttributeValue> createCustomAttributesIterator()
        {
            Throwable ex = new UnsupportedOperationException();
            Logger.error(this, errorMessageUnsupportedOperationException, ex);
            return null;
        }

        @Override
        public int getProgressSize()
        {
            Throwable ex = new UnsupportedOperationException();
            Logger.error(this, errorMessageUnsupportedOperationException, ex);
            return 0;
        }

        @Override
        public int getProgressValue()
        {
            Throwable ex = new UnsupportedOperationException();
            Logger.error(this, errorMessageUnsupportedOperationException, ex);
            return 0;
        }

        @Override
        public Iterator createResourcesIterator()
        {
            Throwable ex = new UnsupportedOperationException();
            Logger.error(this, errorMessageUnsupportedOperationException, ex);
            return null;
        }

        @Override
        public boolean isStartedByThisAppServer()
        {
            Throwable ex = new UnsupportedOperationException();
            Logger.error(this, errorMessageUnsupportedOperationException, ex);
            return false;
        }

        @Override
        public String getDescription()
        {
            Throwable ex = new UnsupportedOperationException();
            Logger.error(this, errorMessageUnsupportedOperationException, ex);
            return null;
        }

        @Override
        public String getDescription(LocaleInformation paramLocaleInformation)
        {
            Throwable ex = new UnsupportedOperationException();
            Logger.error(this, errorMessageUnsupportedOperationException, ex);
            return null;
        }

        @Override
        public User getUser()
        {
            Throwable ex = new UnsupportedOperationException();
            Logger.error(this, errorMessageUnsupportedOperationException, ex);
            return null;
        }

        @Override
        public String getHostName()
        {
            Throwable ex = new UnsupportedOperationException();
            Logger.error(this, errorMessageUnsupportedOperationException, ex);
            return null;
        }

        @Override
        public String getInstallationID()
        {
            Throwable ex = new UnsupportedOperationException();
            Logger.error(this, errorMessageUnsupportedOperationException, ex);
            return null;
        }

        @Override
        public String getServerName()
        {
            Throwable ex = new UnsupportedOperationException();
            Logger.error(this, errorMessageUnsupportedOperationException, ex);
            return null;
        }

        @Override
        public Date getScheduledDate()
        {
            Throwable ex = new UnsupportedOperationException();
            Logger.error(this, errorMessageUnsupportedOperationException, ex);
            return null;
        }

        @Override
        public boolean getScheduledDateNull()
        {
            Throwable ex = new UnsupportedOperationException();
            Logger.error(this, errorMessageUnsupportedOperationException, ex);
            return false;
        }

        @Override
        public JobConfiguration getJobConfiguration()
        {
            Throwable ex = new UnsupportedOperationException();
            Logger.error(this, errorMessageUnsupportedOperationException, ex);
            return null;
        }

        @Override
        public Collection getChilds()
        {
            Throwable ex = new UnsupportedOperationException();
            Logger.error(this, errorMessageUnsupportedOperationException, ex);
            return null;
        }
    }

    @Override
    public void init() throws PipelineInitializationException
    {
        super.init();

        pipeletName = getPipeletName();
        pipelineName = getPipelineName();
        errorMessageUnsupportedOperationException = "UnsupportedOperationException in pipelet \"" + pipeletName
                        + "\" in pipeline \"" + pipelineName
                        + "\", ignoring it and continuing execution of pipelet";
    }
}
