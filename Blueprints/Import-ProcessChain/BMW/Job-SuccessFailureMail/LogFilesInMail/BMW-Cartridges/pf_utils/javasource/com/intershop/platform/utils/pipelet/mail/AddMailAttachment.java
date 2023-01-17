package com.intershop.platform.utils.pipelet.mail;

import javax.activation.MimetypesFileTypeMap;
import javax.inject.Inject;

import com.intershop.beehive.core.capi.pipeline.Pipelet;
import com.intershop.beehive.core.capi.pipeline.PipeletExecutionException;
import com.intershop.beehive.core.capi.pipeline.PipelineDictionary;
import com.intershop.component.mail.capi.Mail;
import com.intershop.component.mail.capi.MailAttachment;
import com.intershop.component.mail.capi.MailMgr;

public class AddMailAttachment extends Pipelet
{
    @Inject
    private MailMgr mailMgr;
    public static final String DN_EMAIL = "Email";
    public static final String DN_FILE_NAME = "FileName";
    public static final String DN_FILE_CONTENT = "FileContent";

    @Override
    public int execute(PipelineDictionary dict) throws PipeletExecutionException
    {
        Mail mail = dict.get(DN_EMAIL);
        String fileName = dict.get(DN_FILE_NAME);
        byte[] fileContent = dict.get(DN_FILE_CONTENT);

        MailAttachment attachment = mailMgr.createAttachment(fileContent, fileName, null);
        MimetypesFileTypeMap mimeTypeMap = new MimetypesFileTypeMap();
        attachment.setMimeType(mimeTypeMap.getContentType(fileName));
        mail.addToAttachments(attachment);

        return PIPELET_NEXT;
    }
}
