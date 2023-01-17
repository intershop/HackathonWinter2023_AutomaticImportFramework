package com.intershop.platform.utils.pipelet.regex;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.intershop.beehive.core.capi.pipeline.Pipelet;
import com.intershop.beehive.core.capi.pipeline.PipeletExecutionException;
import com.intershop.beehive.core.capi.pipeline.PipelineDictionary;
import com.intershop.beehive.core.capi.pipeline.PipelineInitializationException;

/**
 * Regular Expression matching operations Uses error connector, if no match
 * found.
 * 
 * @author h.grasser@intershop.de
 * @version 1.0, 05/10/2011
 * @comment Basic functionality.
 * 
 */

public class Matches extends Pipelet
{

    /**
     * Constant used to access pipelet configuration with key 'Mode'
     */
    public static final String CN_MODE = "Mode";
    /**
     * Member attribute that holds the pipelet configuration value 'Mode'
     */
    private String cfg_mode = "";
    /**
     * Constant used to access pipelet configuration with key 'DefaultRegEx'
     */
    public static final String CN_DEFAULT_REG_EX = "DefaultRegEx";
    /**
     * Member attribute that holds the pipelet configuration value
     * 'DefaultRegEx'
     */
    private String cfg_defaultRegEx = "";
    /**
     * Constant used to access the pipeline dictionary with key 'RegEx'
     */
    public static final String DN_REG_EX = "RegEx";
    /**
     * Constant used to access the pipeline dictionary with key 'String'
     */
    public static final String DN_STRING = "String";
    /**
     * Constant used to access pipelet configuration with key
     * 'DefaultReplacement'
     */
    public static final String CN_DEFAULT_REPLACEMENT = "DefaultReplacement";
    /**
     * Member attribute that holds the pipelet configuration value
     * 'DefaultReplacement'
     */
    private String cfg_defaultReplacement = "";
    /**
     * Constant used to access the pipeline dictionary with key 'Replacement'
     */
    public static final String DN_REPLACEMENT = "Replacement";
    /**
     * Constant used to access the pipeline dictionary with key 'Match0'
     */
    public static final String DN_MATCH_0 = "Match0";
    /**
     * Constant used to access the pipeline dictionary with key 'Match1'
     */
    public static final String DN_MATCH_1 = "Match1";
    /**
     * Constant used to access the pipeline dictionary with key 'Match2'
     */
    public static final String DN_MATCH_2 = "Match2";
    /**
     * Constant used to access the pipeline dictionary with key 'Match3'
     */
    public static final String DN_MATCH_3 = "Match3";
    /**
     * Constant used to access the pipeline dictionary with key 'Match4'
     */
    public static final String DN_MATCH_4 = "Match4";
    /**
     * Constant used to access the pipeline dictionary with key 'Match5'
     */
    public static final String DN_MATCH_5 = "Match5";
    /**
     * Constant used to access the pipeline dictionary with key 'ReplacedString'
     */
    public static final String DN_REPLACED_STRING = "ReplacedString";

    private static enum Modes {
        Match, Find;
    }

    /**
     * The pipelet's execute method is called whenever the pipelets gets
     * executed in the context of a pipeline and a request. The pipeline
     * dictionary valid for the currently executing thread is provided as a
     * parameter.
     * 
     * @param dict
     *            The pipeline dictionary to be used.
     * @throws PipeletExecutionException
     *             Thrown in case of severe errors that make the pipelet execute
     *             impossible (e.g. missing required input data).
     */
    public int execute(PipelineDictionary dict) throws PipeletExecutionException
    {
        // lookup 'Replacement' in pipeline dictionary
        String replacement = (String)dict.get(DN_REPLACEMENT);

        // lookup 'String' in pipeline dictionary
        String string = (String)dict.get(DN_STRING);

        // lookup 'RegEx' in pipeline dictionary
        String regEx = (String)dict.get(DN_REG_EX);

        if (regEx == null)
        {
            regEx = cfg_defaultRegEx;
        }
        if (regEx == null)
        {
            throw new PipeletExecutionException("No regular expression given");
        }

        if (replacement == null)
        {
            replacement = cfg_defaultReplacement;
        }

        Matcher matcher;
        boolean matches;

        if (string == null)
        {
            return PIPELET_ERROR;
        }

        matcher = Pattern.compile(regEx, Pattern.DOTALL | Pattern.MULTILINE).matcher(string);

        switch(Modes.valueOf(cfg_mode))
        {
            case Find:
                matches = matcher.find();
                break;

            default:
                matches = matcher.matches();
                break;
        }

        if (!matches)
        {
            return PIPELET_ERROR;
        }

        // store 'Match0' in pipeline dictionary
        dict.put(DN_MATCH_0, matcher.group(0));

        // store 'Match1' in pipeline dictionary
        dict.put(DN_MATCH_1, matcher.groupCount() > 0 ? matcher.group(1) : null);

        // store 'Match2' in pipeline dictionary
        dict.put(DN_MATCH_2, matcher.groupCount() > 1 ? matcher.group(2) : null);

        // store 'Match3' in pipeline dictionary
        dict.put(DN_MATCH_3, matcher.groupCount() > 2 ? matcher.group(3) : null);

        // store 'Match4' in pipeline dictionary
        dict.put(DN_MATCH_4, matcher.groupCount() > 3 ? matcher.group(4) : null);

        // store 'Match5' in pipeline dictionary
        dict.put(DN_MATCH_5, matcher.groupCount() > 4 ? matcher.group(5) : null);

        if (replacement != null)
        {
            // store 'ReplacedString' in pipeline dictionary
            dict.put(DN_REPLACED_STRING, string.replaceAll(regEx, replacement));
        }
        return PIPELET_NEXT;
    }

    /**
     * The pipelet's initialization method is called whenever the pipeline used
     * to read and process pipelet configuration values that are required during
     * the pipelet execution later on.
     * 
     * @throws PipelineInitializationException
     *             Thrown if some error occured when reading the pipelet
     *             configuration.
     */
    public void init() throws PipelineInitializationException
    {
        // store 'DefaultReplacement' config value in field variable
        cfg_defaultReplacement = (String)getConfiguration().get(CN_DEFAULT_REPLACEMENT);

        // store 'DefaultRegEx' config value in field variable
        cfg_defaultRegEx = (String)getConfiguration().get(CN_DEFAULT_REG_EX);

        // store 'Mode' config value in field variable
        cfg_mode = (String)getConfiguration().get(CN_MODE);
        if (null == cfg_mode)
        {
            throw new PipelineInitializationException("Mandatory attribute 'Mode' not found in pipelet configuration.");
        }

    }
}