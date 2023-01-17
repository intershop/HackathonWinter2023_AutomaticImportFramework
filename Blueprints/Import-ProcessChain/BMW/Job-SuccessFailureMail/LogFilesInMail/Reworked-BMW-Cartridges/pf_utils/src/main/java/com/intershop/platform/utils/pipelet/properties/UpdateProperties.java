package com.intershop.platform.utils.pipelet.properties;

import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.intershop.beehive.core.capi.pipeline.Pipelet;
import com.intershop.beehive.core.capi.pipeline.PipeletExecutionException;
import com.intershop.beehive.core.capi.pipeline.PipelineDictionary;

/**
 * This pipelet is used to update the provided properties according to the given pair of key and value. When no value is
 * provided the pair will be removed.
 * <p />
 * Optionally the pipelet supports wildcards. The first star contained in a given key will be replaced by a number and the
 * given value will be assigned to the resulting key. The number will start at one and will be incremented as long as the
 * resulting key can be found within the provided properties. See example below:
 * <p />
 * <code>animal.mammal.*.size = "large"</code> becomes<br /><br />
 * <code>animal.mammal.1.size = "large"</code><br />
 * <code>animal.mammal.2.size = "large"</code><br />
 * <code>animal.mammal.n.size = "large"</code>
 * 
 * @author JCMeyer
 * @version 1.0, 2015-08-03
 * @since 3.1.0
 */
public class UpdateProperties extends Pipelet
{
    /**
     * Constant used to access the pipeline dictionary with key 'Properties'
     * 
     * The properties to be updated.
     */
    public static final String DN_PROPERTIES = "Properties";

    /**
     * Constant used to access the pipeline dictionary with key 'Key'
     * 
     * The key defining the property to be updated.
     */
    public static final String DN_KEY = "Key";

    /**
     * Constant used to access the pipeline dictionary with key 'Value'
     * 
     * The new value to be set.
     */
    public static final String DN_VALUE = "Value";

    /**
     * A dot constant used for pattern matching.
     */
    protected static final char DOT = '.';

    @Override
    public int execute(PipelineDictionary dict) throws PipeletExecutionException
    {
        Properties properties = dict.getRequired(DN_PROPERTIES);

        String key = dict.getRequired(DN_KEY);

        String value = dict.getOptional(DN_VALUE);

        if (key.indexOf('*') == -1)  // single key mode
        {
            if (value == null)
            {
                properties.remove(key);
            }
            else
            {
                properties.setProperty(key, value);
            }
        }
        else  // wildcard mode
        {
            Pattern pattern = Pattern.compile("\\" + DOT + "(\\d+?)");
            if (value == null)
            {
                for (String nextKey = key.replace('*', '0'); properties.containsKey(nextKey = getNextKey(nextKey, pattern)); properties.remove(nextKey));
            }
            else
            {
                for (String nextKey = key.replace('*', '0'); properties.containsKey(nextKey = getNextKey(nextKey, pattern)); properties.setProperty(nextKey, value));
            }
        }

        return PIPELET_NEXT;
    }

    /**
     * Helper method incrementing the number within the given key by one when found using the give pattern.
     * 
     * @param key the key containing the number to be incremented
     * @param pattern the pattern used for matching the number
     * @return the resulting key containing the number, incremented when found 
     */
    protected String getNextKey(String key, Pattern pattern)
    {
        Matcher matcher = pattern.matcher(key);
        if (matcher.find())
        {
            int counter = Integer.parseInt(matcher.group(1));
            key = matcher.replaceFirst(DOT + Integer.toString(++counter));
        }
        return key;
    }

}
