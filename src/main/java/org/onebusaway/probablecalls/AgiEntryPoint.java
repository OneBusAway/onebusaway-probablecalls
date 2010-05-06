package org.onebusaway.probablecalls;

import java.util.HashMap;
import java.util.Map;

import org.asteriskjava.fastagi.AgiChannel;
import org.asteriskjava.fastagi.AgiException;
import org.asteriskjava.fastagi.AgiHangupException;
import org.asteriskjava.fastagi.AgiNetworkException;
import org.asteriskjava.fastagi.AgiRequest;
import org.asteriskjava.fastagi.AgiScript;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.ActionProxy;
import com.opensymphony.xwork2.ActionProxyFactory;
import com.opensymphony.xwork2.DefaultActionProxyFactory;
import com.opensymphony.xwork2.config.Configuration;
import com.opensymphony.xwork2.inject.Container;

public class AgiEntryPoint implements AgiScript {

  private static final String AGI_REQUEST_PARAM = AgiEntryPoint.class.getName()
      + ".agiRequest";

  private static final String AGI_CHANNEL_PARAM = AgiEntryPoint.class.getName()
      + ".agiChannel";

  private static final String ACTIVE_ACTION = AgiEntryPoint.class.getName()
      + ".active";

  private static final String NEXT_ACTION = AgiEntryPoint.class.getName()
      + ".next";

  private ActionProxyFactory _factory = new DefaultActionProxyFactory();

  private String _namespace = "/";

  private String _action;

  private int _pause = 0;

  public void setNamespace(String namespace) {
    _namespace = namespace;
  }

  public void setAction(String action) {
    _action = action;
  }

  public void setConfiguration(Configuration configuration) {
    Container con = configuration.getContainer();
    _factory = con.getInstance(ActionProxyFactory.class);
  }

  public void setPauseBetweenActions(int pause) {
    _pause = pause;
  }

  public void service(AgiRequest request, AgiChannel channel)
      throws AgiException {

    // Answer the call?
    channel.answer();

    Map<String, Object> contextMap = new HashMap<String, Object>();
    contextMap.put(AGI_REQUEST_PARAM, request);
    contextMap.put(AGI_CHANNEL_PARAM, channel);

    contextMap.put(ActionContext.APPLICATION, new HashMap<Object, Object>());
    contextMap.put(ActionContext.SESSION, new HashMap<Object, Object>());

    try {

      AgiActionName action = new AgiActionName(_namespace, _action);

      while (action != null) {

        Map<Object, Object> params = new HashMap<Object, Object>(
            action.getParams());
        params.put("userId", request.getCallerIdNumber());
        contextMap.put(ActionContext.PARAMETERS, params);
        contextMap.put(ACTIVE_ACTION, action);
        contextMap.remove(NEXT_ACTION);

        ActionProxy actionProxy = _factory.createActionProxy(
            action.getNamespace(), action.getAction(), null, contextMap);

        actionProxy.execute();

        ActionInvocation invoke = actionProxy.getInvocation();
        ActionContext context = invoke.getInvocationContext();
        action = (AgiActionName) context.get(NEXT_ACTION);

        if (_pause > 0) {
          channel.waitForDigit(_pause);
        }
      }

    } catch (AgiHangupException ex) {
      // Don't sweat it
    } catch (AgiNetworkException ex) {
      // Don't sweat it
    } catch (Exception ex) {
      throw new AgiException("something went wrong", ex);
    }
  }

  /***************************************************************************
   * Static Methods
   **************************************************************************/

  public static AgiRequest getAgiRequest(ActionContext context) {
    return (AgiRequest) context.get(AGI_REQUEST_PARAM);
  }

  public static String getCallerIdNumber(ActionContext context) {
    AgiRequest request = getAgiRequest(context);
    return request.getCallerIdNumber();
  }

  public static AgiChannel getAgiChannel(ActionContext context) {
    return (AgiChannel) context.get(AGI_CHANNEL_PARAM);
  }

  public static AgiActionName getActiveAction(ActionContext context) {
    return (AgiActionName) context.get(ACTIVE_ACTION);
  }

  public static void setNextAction(ActionContext context, AgiActionName action) {
    context.put(NEXT_ACTION, action);
  }
}
