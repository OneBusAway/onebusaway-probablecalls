package org.onebusaway.probablecalls.agitemplates;

import java.io.IOException;

import org.asteriskjava.fastagi.AgiException;
import org.asteriskjava.fastagi.AgiOperations;

import com.opensymphony.xwork2.ActionContext;

public interface AgiTemplateOperation {
    public char execute(ActionContext context, AgiOperations opts) throws IOException, AgiException;
}