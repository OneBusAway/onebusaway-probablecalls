package org.onebusaway.probablecalls;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.Result;

public class HistoryRepeatResult implements Result {

    private static final long serialVersionUID = 1L;

    public void execute(ActionInvocation invocation) throws Exception {
        ActionContext context = invocation.getInvocationContext();
        AgiActionName action = History.repeat(context);
        AgiEntryPoint.setNextAction(context, action);
    }
}
