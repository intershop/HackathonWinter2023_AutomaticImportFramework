package com.intershop.platform.utils.capi.template;

import java.util.Date;

/**
 * This defines the business interface methods for BasicTemplatesTools as defined in the object model.
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
 * @author m.langbein@intershop.de
 * @version 1.3, 01/20/2017
 * @comment added method getFormattedDisplaySKU()
 *          <br />
 * 
 */

public interface BasicTemplateTools extends TemplateTools
{
    /**
     * Constant to used as fallback for string cutting.
     */
    public final static String STRINGCUT_SUFFIX = "...";

    /**
     * Returns a trimmed string with a maximum length by given an input string. The default suffix "..." is used.
     * 
     * @param value
     *            The string to cut.
     * @param length
     *            The maximum length of the returned value excl. the suffix.
     * @return String The clipped string.
     */
    public String getCuttedString(String value, String length);

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
    public String getCuttedString(String value, String length, String suffix);

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
    public String getReplaceFirst(String value, String replaceable, String replacement);

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
    public String getFirstDelimiterStringValue(String value, String delimiter);

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
    public String getLastDelimiterStringValue(String value, String delimiter);

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
    public int getFirstIndexOf(String value, String substring);

    /**
     * Returns a generated UUID excl. replaced "." by "_" for template use.
     * 
     * @return String THe generated UUID.
     */
    public String getGeneratedUUID();

    /**
     * Returns an round up value given by input value
     * 
     * @param value
     *            The value for round up.
     * 
     * @return String The round up value as string.
     */
    public String getRoundUp(Object value);

    /**
     * Returns an round off value given by input value
     * 
     * @param value
     *            The value for round off.
     * 
     * @return String The round off value as string.
     */
    public String getRoundOff(Object value);

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
    public String getLineBreakDisplayValue(String value, String length);

    /**
     * Same as {@link BasicTemplateTools#getLineBreakDisplayValue(String, String)}, but checks if the value starts with
     * the current search term. If so, the search term is removed.
     * 
     * @param value
     *            The value for the line break.
     * @param length
     *            The length to checked the line break.
     * @param searchTerm
     *            The current search term.
     * 
     * @return String The value as string.
     */
    public String getLineBreakDisplayValue(String value, final String length, final String searchTerm);

    /**
     * Add a number of days to the passed date.
     * 
     * @param date
     *            The initial date.
     * @param days
     *            Offset of days.
     * @return Date The newly calculated date.
     */
    public Date getDateWithDayOffset(Date date, Integer days);

    /**
     * Check if a date is in the past or the future.
     * 
     * @param date
     *            The date that should be validated.
     * @return boolean <code>true</code> if the passed date is in the past, otherwise return <code>false</code>
     */
    public boolean isPastDate(Date date);

    /**
     * Get the formatted SKU of the passed productBO. In case the productBO is SRP, the actual SKU (e.g.
     * 6161041_AT11_19700101) must be formatted to a 7 or 11 digits long sku (e.g. 6161041).
     * 
     * @param sku
     * @param isSRP
     * @return the formatted SKU
     */
    public String getFormattedDisplaySKU(String sku, boolean isSRP);
}
