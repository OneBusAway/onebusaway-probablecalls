package org.onebusaway.probablecalls.agitemplates;

import org.onebusaway.probablecalls.AgiActionName;

import com.opensymphony.xwork2.ActionContext;

public interface AgiDTMFOperation {
    public AgiActionName execute(ActionContext context, String digitsDialed);
}
