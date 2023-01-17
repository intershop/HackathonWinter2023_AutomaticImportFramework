package com.intershop.platform.utils.pipelet.utils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import com.intershop.beehive.core.capi.log.Logger;

import com.intershop.beehive.core.capi.pipeline.Pipelet;
import com.intershop.beehive.core.capi.pipeline.PipeletExecutionException;
import com.intershop.beehive.core.capi.pipeline.PipelineDictionary;

public class ExecuteCommandLine extends Pipelet
{

    public static final String DN_COMMAND = "Command";
    public static final String DN_OUTPUT = "Output";
    public static final String DN_EXIT_CODE = "ExitCode";

    @Override
    public int execute(PipelineDictionary dict) throws PipeletExecutionException
    {
        String command = dict.getRequired(DN_COMMAND);

        try
        {
            Runtime rt = Runtime.getRuntime();
            Process pr = rt.exec(command);

            BufferedReader input = new BufferedReader(new InputStreamReader(pr.getInputStream()));

            String output = "";
            String line = null;

            while((line = input.readLine()) != null)
            {
                output += line+System.getProperty("line.separator");
            }

            int exitVal = pr.waitFor();
            dict.put(DN_EXIT_CODE, exitVal);
            dict.put(DN_OUTPUT, output);

        }
        catch(Exception e)
        {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            Logger.error(this, e.getMessage() + "\n" + sw.toString());
            return PIPELET_ERROR;
        }

        return PIPELET_NEXT;
    }

}
