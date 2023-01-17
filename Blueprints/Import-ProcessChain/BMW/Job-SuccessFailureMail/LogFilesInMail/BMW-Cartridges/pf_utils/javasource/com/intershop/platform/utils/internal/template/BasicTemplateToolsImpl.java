package com.intershop.platform.utils.internal.template;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Formatter;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

import javax.servlet.ServletException;

import org.apache.commons.lang.StringUtils;

import com.intershop.beehive.bts.capi.orderprocess.LineItemCtnr;
import com.intershop.beehive.bts.capi.orderprocess.ProductLineItem;
import com.intershop.beehive.bts.internal.orderprocess.ProductLineItemPO;
import com.intershop.beehive.core.capi.common.SystemException;
import com.intershop.beehive.core.capi.currency.Currency;
import com.intershop.beehive.core.capi.currency.CurrencyMgr;
import com.intershop.beehive.core.capi.domain.Domain;
import com.intershop.beehive.core.capi.localization.LocaleMgr;
import com.intershop.beehive.core.capi.log.Logger;
import com.intershop.beehive.core.capi.naming.NamingMgr;
import com.intershop.beehive.core.capi.request.Request;
import com.intershop.beehive.core.capi.request.ServletRequest;
import com.intershop.beehive.core.capi.request.ServletResponse;
import com.intershop.beehive.core.capi.template.TemplateIdentifier;
import com.intershop.beehive.core.capi.template.TemplateMgr;
import com.intershop.beehive.core.capi.util.UUIDMgr;
import com.intershop.beehive.core.internal.template.TemplateExecutionConfig;
import com.intershop.beehive.foundation.quantity.CurrencyException;
import com.intershop.beehive.foundation.quantity.Money;
import com.intershop.beehive.foundation.quantity.MoneyCalculator;
import com.intershop.platform.utils.capi.template.BasicTemplateTools;

/**
 * This class defines the business implementation methods for BasicTemplatesTools.
 * 
 * <br />
 * 
 * <b>History:</b> <br />
 * 
 * @author t.hartmann@intershop.de
 * @version 1.0, 07/20/2012
 * 
 * @author m.grasser@intershop.de
 * @version 1.1, 10/05/2012
 * @comment added methods getReplaceFirst(), getFirstIndexOf()
 *
 * @author t.hartmann@intershop.de
 * @version 1.2, 10/05/2012
 * @comment added method getGeneratedUUID()
 * 
 *          <br />
 * 
 */

public class BasicTemplateToolsImpl implements BasicTemplateTools
{
    private static long tempId = 0;
    private Random random = new Random();

    /**
     * Internal UUID manager class.
     */
    private UUIDMgr uuidMgr;

    private LocaleMgr localeMgr;

    /**
     * Construct the object.
     */
    public BasicTemplateToolsImpl()
    {
        // get the UUID Manager
        uuidMgr = UUIDMgr.getInstance();
        localeMgr = (LocaleMgr)NamingMgr.getInstance().lookupManager(LocaleMgr.REGISTRY_NAME);
    }

    /**
     * Returns the unique name of the class.
     * 
     * @return String The tools name.
     */
	@Override
    public String getName()
    {
        return this.getClass().getName();
    }

    /**
     * Add a number of days to the passed date.
     * 
     * @param date
     *            The initial date.
     * @param days
     *            Offset of days.
     * @return Date The newly calculated date.
     */
    @Override
    public Date getDateWithDayOffset(Date date, Integer days)
    {
        if (days == null || days == 0)
        {
            return date;
        }

        GregorianCalendar gc = new GregorianCalendar();
        gc.setTime(date);
        gc.add(Calendar.DATE, days.intValue());
        return gc.getTime();
    }

    /**
     * Check if a date is in the past or the future.
     * 
     * @param date
     *            The date that should be validated.
     * @return boolean <code>true</code> if the passed date is in the past, otherwise return <code>false</code>
     */
    @Override
    public boolean isPastDate(Date date)
    {
        return date.getTime() < System.currentTimeMillis();
    }

    /**
     * Returns a trimmed string with a maximum length by given an input string. The default suffix "..." is used.
     * 
     * @param value
     *            The string to cut.
     * @param length
     *            The maximum length of the returned value excl. the suffix.
     * @return String The clipped string.
     */
    @Override
    public String getCuttedString(String value, String length)
    {
        return getCuttedString(value, length, BasicTemplateTools.STRINGCUT_SUFFIX);
    }

    /**
     * Returns a trimmed string with a maximum length by a given input string and suffix, e.g. "...".
     * 
     * @param value
     *            The string to cut.
     * @param length
     *            The maximum length of the returned value excl. the suffix.
     * @param suffix
     *            The suffix of the returned value.
     * @return String The clipped string.
     */
    @Override
    public String getCuttedString(String value, String length, String suffix)
    {
        int len = Integer.parseInt(length);
        if (value.length() >= len)
        {
            String substring = value.substring(0, len);
            // check for an open html tag, cut before
            if (substring.lastIndexOf("<") < substring.lastIndexOf(">"))
            {
                substring = value.substring(0, substring.lastIndexOf("<") - 1);
            }
            return substring.concat((StringUtils.isNotEmpty(suffix) ? suffix : BasicTemplateTools.STRINGCUT_SUFFIX));
        }
        else
        {
            return value;
        }
    }

    /**
     * Replaces the first substring of <code>value</code> that matches the given <code>replaceable</code> with the given
     * <code>replacement</code>.
     * 
     * @param value
     *            The string to be searched in.
     * @param replaceable
     *            The value to be replaced.
     * @param replacement
     *            The string to be substituted for the replaceable.
     * 
     * @return String The result <code>String</code>.
     */
    @Override
    public String getReplaceFirst(String value, String replaceable, String replacement)
    {
        return value.replaceFirst(Pattern.quote(replaceable), replacement).trim();
    }

    /**
     * Returns the first string value by delimiter.
     * 
     * @param value
     *            The string to be splitted.
     * @param delimiter
     *            The delimiter.
     * 
     * @return String The result <code>String</code>.
     */
    @Override
    public String getFirstDelimiterStringValue(String value, String delimiter)
    {
        String[] valueSplit = value.split(delimiter);
        return valueSplit[0];
    }

    /**
     * Returns the last string value by delimiter.
     * 
     * @param value
     *            The string to be splitted.
     * @param delimiter
     *            The delimiter.
     * 
     * @return String The result <code>String</code>.
     */
    @Override
    public String getLastDelimiterStringValue(String value, String delimiter)
    {
        String[] valueSplit = value.split(delimiter);
        return valueSplit[1];
    }

    /**
     * Returns the index within this string of the first occurrence of the specified substring.
     * 
     * @param value
     *            The string to be searched in.
     * @param substring
     *            Any string.
     * 
     * @return If the string argument occurs as a substring within this object, then the index of the first character of
     *         the first such substring is returned. If it does not occur as a substring, <code>-1</code> is returned.
     */
    @Override
    public int getFirstIndexOf(String value, String substring)
    {
        return value.indexOf(substring);
    }

    /**
     * Returns a generated UUID excl. replaced "." by "_" for template use.
     * 
     * @return String The generated UUID.
     */
    @Override
    public String getGeneratedUUID()
    {
        return uuidMgr.createUUIDString().replace(".", "_");
    }

    public boolean isStringStartsWith(String string, String prefix)
    {
        if (string.startsWith(prefix))
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    /**
     * Returns an round up value given by input value
     * 
     * @param value
     *            The value for round up.
     * 
     * @return String The round up value as string.
     */
    @Override
    public String getRoundUp(Object value)
    {
        String result = null;
        if (value instanceof Double)
        {
            Double dValue = (Double)value;
            dValue = Math.ceil(dValue);
            result = String.valueOf(dValue.intValue());
        }
        else if (value instanceof String)
        {
            Double dValue = new Double(value.toString());
            dValue = Math.ceil(dValue);
            result = String.valueOf(dValue.intValue());
        }

        return result;
    }

    /**
     * Returns an round off value given by input value
     * 
     * @param value
     *            The value for round off.
     * 
     * @return String The round off value as string.
     */
    @Override
    public String getRoundOff(Object value)
    {
        String result = null;
        if (value instanceof Double)
        {
            Double dValue = (Double)value;
            dValue = Math.floor(dValue);
            result = String.valueOf(dValue.intValue());
        }
        else if (value instanceof String)
        {
            Double dValue = new Double(value.toString());
            dValue = Math.floor(dValue);
            result = String.valueOf(dValue.intValue());
        }

        return result;
    }

    /**
     * Returns an line break display value given by input value
     * 
     * @param value
     *            The value for the line break.
     * @param length
     *            The length to checked the line break.
     * 
     * @return String The value as string.
     */
    @Override
    public String getLineBreakDisplayValue(String value, String length)
    {
        int len = Integer.parseInt(length);
        boolean checkReplace = false;
        if ((getFirstIndexOf(value, " ") >= 1 || getFirstIndexOf(value, "/") >= 1 || getFirstIndexOf(value, "-") >= 1)
                        && value.length() > len)
        {
            if (getFirstIndexOf(value, "/") >= 1)
            {
                value = getReplaceFirst(value, "/", "/<br/>");
                checkReplace = true;
            }

            if (getFirstIndexOf(value, "-") >= 1 && !checkReplace)
            {
                value = getReplaceFirst(value, "-", "-<br/>");
                checkReplace = true;
            }

            if (getFirstIndexOf(value, " ") >= 1 && !checkReplace)
            {
                value = getReplaceFirst(value, " ", "<br/>");
                checkReplace = true;
            }
        }
        return value;
    }

    @Override
    public String getLineBreakDisplayValue(String value, final String length, final String searchTerm)
    {
        if (searchTerm != null && !searchTerm.isEmpty() && getFirstIndexOf(value, "/") >= 1)
        {
            final String firstValuePart = replaceUmlaut(value.substring(0, getFirstIndexOf(value, "/")).toLowerCase());
            if (searchTerm.contains(firstValuePart))
            {
                value = value.substring(getFirstIndexOf(value, "/") + 1);
            }
        }

        return getLineBreakDisplayValue(value, length);
    }

    private String replaceUmlaut(String input)
    {

        // replace all lower Umlauts
        String o_strResult = input.replaceAll("\u00FC", "ue").replaceAll("\u00F6", "oe").replaceAll("\u00E4", "ae")
                        .replaceAll("\u00DF", "ss");

        // first replace all capital umlaute in a non-capitalized context (e.g. Übung)
        o_strResult = o_strResult.replaceAll("\u00DC(?=[a-zäöüß ])", "Ue").replaceAll("\u00D6(?=[a-zäöüß ])", "Oe")
                        .replaceAll("\u00C4(?=[a-zäöüß ])", "Ae");

        // now replace all the other capital umlaute
        o_strResult = o_strResult.replaceAll("\u00DC", "UE").replaceAll("\u00D6", "OE").replaceAll("\u00C4", "AE");

        return o_strResult;
    }

    public String getTempID()
    {
        return "ID-" + (System.nanoTime() % 1000000000L) + "-" + random.nextInt(1000) + "-" + ++tempId;
    }

    public String getJavascriptStringEncoded(String a)
    {
        return a.replaceAll("\\\\", "\\\\").replace("'", "\\'");
    }

    private Map<String, String> currencySymbolCache = new HashMap<String, String>();

    public String getCurrencySymbol(String currencyMnemonic)
    {
        String symbol = currencySymbolCache.get(currencyMnemonic);

        if (symbol == null)
        {
            Currency currency = NamingMgr.getManager(CurrencyMgr.class).getCurrency(currencyMnemonic);
            symbol = currency != null ? currency.getCurrencySymbol() : currencyMnemonic;
            currencySymbolCache.put(currencyMnemonic, symbol);
        }
        return symbol;
    }

    @SuppressWarnings("rawtypes")
    public Iterator createEnumValuesIterator(String enumName)
    {
        return getEnumValues(enumName).iterator();
    }

    public Collection<Object> getEnumValues(String enumName)
    {
        Object[] elements = new Object[0];
        try
        {
            Class<?> enumClass = Class.forName(enumName);
            elements = (Object[])enumClass.getMethod("values").invoke(null);
        }
        catch(Exception e)
        {
            Logger.error(this, "Getting enum " + enumName + " failed", e);
        }
        return Arrays.asList(elements);
    }

    public String getTargetHostName(Map<String, String> resource, Object objectSiteName)
    {
        String siteName;
        if (objectSiteName instanceof Domain)
        {
            siteName = ((Domain)objectSiteName).getDomainName();
        }
        else if (objectSiteName != null)
        {
            siteName = objectSiteName.toString();
        }
        else
        {
            return null;
        }
        String targetExtension = resource.get(siteName);
        if (targetExtension == null)
        {
            return null;
        }
        String searchPattern = resource.get("SearchPattern");
        String serverName = Request.getCurrent().getServletRequest().getServerName();
        if (serverName == null)
        {
            return null;
        }
        return serverName.replaceAll(searchPattern, targetExtension);
    }

    public Date getParseDate(String date, String format)
    {
        try
        {
            return new SimpleDateFormat(format, Request.getCurrent().getLocale().getJavaLocale()).parse(date);
        }
        catch(ParseException e)
        {
            Logger.error(this, "invalid date", e);
            return null;
        }
    }

    public String getDateToString(Date date, String format)
    {
        try
        {
            SimpleDateFormat dateFormat = new SimpleDateFormat(format);
            return dateFormat.format(date);
        }
        catch(Exception e)
        {
            Logger.error(this, "invalid date", e);
            return null;
        }
    }

    public String getCountryName(String countryCode)
    {
        Locale locale = new Locale("", countryCode);
        String displayName = locale.getDisplayName(Request.getCurrent().getLocale().getJavaLocale());
        if (displayName.equals("Schweiz"))
        {
            displayName = "der " + displayName;
        }
        return displayName;
    }

    public String getReplace(Object input, Object find, Object replace)
    {
        return input.toString().replaceAll(find.toString(), replace.toString());
    }

    public Money getSubtotalBasePricePC(LineItemCtnr lineItemCtnr)
    {
        return sumProductLineItemPrices(lineItemCtnr, new LinePriceGetterCommand()
        {
            @Override
            public Money getPrice(ProductLineItem pli)
            {
                return pli.getBasePricePC();
            }
        });
    }

    private interface LinePriceGetterCommand
    {
        Money getPrice(ProductLineItem pli);
    }

    @SuppressWarnings("rawtypes")
    private Money sumProductLineItemPrices(LineItemCtnr lineItemCtnr, LinePriceGetterCommand price)
    {
        Iterator plis;
        Money sum;
        MoneyCalculator high_calc;

        // Get high precision calculator and zero sum
        high_calc = MoneyCalculator.createHighPrecisionCalculator();
        sum = Money.getZeroMoney(lineItemCtnr.getPurchaseCurrencyCode());

        try
        {
            plis = lineItemCtnr.createProductLineItemsIterator();
            while(plis.hasNext())
            {
                ProductLineItemPO pli = (ProductLineItemPO)plis.next();

                Money next = price.getPrice(pli);

                sum = high_calc.add(sum, next);
            }
        }
        catch(CurrencyException cex)
        {
            sum = Money.NOT_AVAILABLE;
        }

        return sum;
    }

    @SuppressWarnings("rawtypes")
    public boolean isInstanceOf(Object object, String interfaceName)
    {
        Set<Class> allClasses = new HashSet<Class>();
        addClasses(allClasses, object.getClass());
        for (Class clazz : allClasses)
        {
            if (clazz.getSimpleName().equals(interfaceName))
            {
                return true;
            }
        }
        return false;
    }

    @SuppressWarnings("rawtypes")
    private void addClasses(Set<Class> allClasses, Class<?> clazz)
    {
        if (clazz == null)
        {
            return;
        }
        if (!allClasses.contains(clazz))
        {
            allClasses.add(clazz);
            addClasses(allClasses, clazz.getSuperclass());
            for (Class<?> interfaze : clazz.getInterfaces())
            {
                addClasses(allClasses, interfaze);
            }
        }
    }

    /**
     * Determine, if a given String contains a specific other string.
     * 
     * @param stringToCheck
     *            The {@link String} to check for the pattern.
     * @param pattern
     *            The pattern to search for in the {@link String}.
     * @return <code>true</code> if pattern is contained in the string, <code>false</code> otherwise.
     * 
     */
    public static boolean getContains(final String stringToCheck, final String pattern)
    {
        if (stringToCheck != null && !stringToCheck.isEmpty() && pattern != null && !pattern.isEmpty())
        {
            return stringToCheck.contains(pattern);
        }
        return false;
    }

    /**
     * Check a given {@link String}, if it is a number.
     * 
     * @param The
     *            {@link String} to check.
     * @return <code>true</code> if the given string is a number, <code>false</code> otherwise.
     */
    public static boolean getIsNumeric(final String stringToCheck)
    {
        return StringUtils.isNumeric(stringToCheck);
    }

    public static Map<String, Collection<String>> parseQuery(String query)
    {
        Map<String, Collection<String>> result = new HashMap<String, Collection<String>>();

        if (query == null)
        {
            return result;
        }

        StringTokenizer parameters = new StringTokenizer(query, "&");
        while(parameters.hasMoreTokens())
        {
            String parameter = parameters.nextToken();
            String[] keyValuePair = parameter.split("=", 2);

            if (keyValuePair != null)
            {
                try
                {
                    if (keyValuePair.length > 0)
                    {
                        String key = URLDecoder.decode(keyValuePair[0], "utf-8");
                        String value = "";
                        if (keyValuePair.length > 1)
                        {
                            value = URLDecoder.decode(keyValuePair[1], "utf-8");
                        }

                        Collection<String> values = result.get(key);
                        if (values == null)
                        {
                            values = new LinkedList<String>();
                            result.put(key, values);
                        }
                        values.add(value);
                    }
                }
                catch(Exception ex)
                {
                }
            }
        }

        return result;
    }

    public String getStringToLowerCase(String string)
    {
        return string.toLowerCase();
    }

    public String getCrypt(String stringToCrypt)
    {
        String checksum = "";
        try
        {
            MessageDigest crypt = MessageDigest.getInstance("SHA1");
            crypt.reset();
            crypt.update((stringToCrypt).getBytes("UTF-8"));
            byte[] sha1 = crypt.digest();
            Formatter formatter = new Formatter();
            for (byte ch : sha1)
            {
                formatter.format("%02x", ch);
            }
            checksum = formatter.toString();
            formatter.close();
        }
        catch(NoSuchAlgorithmException e)
        {
            Logger.error(this, "Internal error", e);
        }
        catch(UnsupportedEncodingException e)
        {
            Logger.error(this, "Internal error", e);
        }
        return checksum;
    }

    public long getUnixTimeStamp()
    {
        return System.currentTimeMillis() / 1000;
    }

    public static String getExecuteTemplate(String templateIdentifier)
    {
        final TemplateIdentifier template = new TemplateIdentifier(templateIdentifier);
        template.setLanguage(Request.getCurrent().getTemplateDirectory());

        TemplateMgr templateMgr = (TemplateMgr)NamingMgr.getInstance().lookupManager(TemplateMgr.REGISTRY_NAME);

        ServletResponse fwResponse = new ServletResponse();
        ServletRequest fwRequest = Request.getCurrent().getServletRequest();

        // load and execute template
        try
        {
            templateMgr.executeTemplate(template, fwRequest, fwResponse, new TemplateExecutionConfig(false, null));
        }
        catch(SystemException e)
        {
            Logger.error(BasicTemplateToolsImpl.class, "SystemException: " + e);
        }
        catch(ServletException e)
        {
            Logger.error(BasicTemplateToolsImpl.class, "ServletException: " + e);
        }
        catch(IOException e)
        {
            Logger.error(BasicTemplateToolsImpl.class, "IOException: " + e);
        }

        return fwResponse.getContent();
    }

    private static String removeHtmlComments(String html)
    {
        if (StringUtils.isBlank(html))
        {
            return null;
        }
        return html.replaceAll("(?s)<!--.*?-->", "");
    }

    private static String removeLineBreaks(String html)
    {
        if (StringUtils.isBlank(html))
        {
            return null;
        }
        return html.replaceAll("([\r\n]+)+", "").replaceAll("( )+", " ");
    }

    public static String getNormalizedDivId(String unnormalized)
    {
        String camelCase = null;
        StringTokenizer tokenizer = new StringTokenizer(unnormalized, "_");
        if (tokenizer != null)
        {
            while(tokenizer.hasMoreTokens())
            {
                String token = tokenizer.nextToken();
                if (camelCase != null)
                    camelCase += token.substring(0, 1).toUpperCase() + token.substring(1);
                else
                    camelCase = token;
            }
        }
        return camelCase;
    }

    @Override
    public String getFormattedDisplaySKU(String sku, boolean isSRP)
    {
        String delimiter = "_";
        // invalid sku
        if (sku == null || sku.trim().isEmpty())
        {
            return "";
        }

        // currently formatting is only done for SRP products 
        if (!isSRP)
        {
            return sku;
        }
        else {
            return sku.replace("SID_", "");
        }
    }
}