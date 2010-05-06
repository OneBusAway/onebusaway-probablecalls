package org.onebusaway.probablecalls.agitemplates;

import org.onebusaway.probablecalls.AgiActionName;

import com.opensymphony.xwork2.ActionContext;

public interface AgiTemplate {
    public AgiActionName execute(ActionContext context) throws Exception;
}
