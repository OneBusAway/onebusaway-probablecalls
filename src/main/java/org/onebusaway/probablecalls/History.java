package org.onebusaway.probablecalls;

import java.util.Map;
import java.util.Stack;

import com.opensymphony.xwork2.ActionContext;

public class History {
  
  private static final String HISTORY_PARAM = History.class.getName();

    @SuppressWarnings("unchecked")
    public static void push(ActionContext context, AgiActionName action) {

        Map<String, Object> session = context.getSession();
        Stack<AgiActionName> stack = (Stack<AgiActionName>) session.get(HISTORY_PARAM);

        if (stack == null) {
            stack = new Stack<AgiActionName>();
            session.put(HISTORY_PARAM, stack);
        }

        if (!stack.isEmpty()) {
            AgiActionName peek = stack.peek();
            if (peek.equals(action))
                return;
        }

        stack.push(action);
    }

    @SuppressWarnings("unchecked")
    public static AgiActionName back(ActionContext context) {
        Map<String, Object> session = context.getSession();
        Stack<AgiActionName> stack = (Stack<AgiActionName>) session.get(HISTORY_PARAM);
        if (stack == null)
            return null;
        if (stack.isEmpty())
            return null;
        if (stack.size() == 1)
            return stack.peek();
        stack.pop();
        return stack.peek();
    }

    @SuppressWarnings("unchecked")
    public static AgiActionName repeat(ActionContext context) {
        Map<String, Object> session = context.getSession();
        Stack<AgiActionName> stack = (Stack<AgiActionName>) session.get(HISTORY_PARAM);
        if (stack == null)
            return null;
        if (stack.isEmpty())
            return null;
        return stack.peek();
    }
}
