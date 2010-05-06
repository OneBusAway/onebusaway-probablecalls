package org.onebusaway.probablecalls;

import java.util.Map;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.Result;
import com.opensymphony.xwork2.util.ValueStack;

public class HistoryBackResult implements Result {

  private static final long serialVersionUID = 1L;

  public void execute(ActionInvocation invocation) throws Exception {
    ActionContext context = invocation.getInvocationContext();

    int count = getBackCount(context);

    AgiActionName action = null;
    for (int i = 0; i < count; i++)
      action = History.back(context);
    AgiEntryPoint.setNextAction(context, action);
  }

  private int getBackCount(ActionContext context) {
    
    int count = 1;

    Object value = context.get("count");
    if (value != null)
      count = Integer.parseInt(value.toString());

    Map<String, Object> params = context.getParameters();
    value = params.get("count");
    if (value != null)
      count = Integer.parseInt(value.toString());

    ValueStack stack = context.getValueStack();
    value = stack.findValue("count");
    if (value != null)
      count = Integer.parseInt(value.toString());

    return count;
  }
}
