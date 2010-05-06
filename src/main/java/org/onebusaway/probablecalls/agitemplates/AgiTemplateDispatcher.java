package org.onebusaway.probablecalls.agitemplates;

import org.onebusaway.probablecalls.AgiActionName;
import org.onebusaway.probablecalls.AgiEntryPoint;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.ObjectFactory;
import com.opensymphony.xwork2.Result;
import com.opensymphony.xwork2.inject.Container;

public class AgiTemplateDispatcher implements Result {

  private static final long serialVersionUID = 1L;

  /**
   * xwork reads this field to determine which property to set with the result
   * body
   */
  public static final String DEFAULT_PARAM = "target";

  private String _target;

  private AgiTemplateRegistry _registry;

  public void setTarget(String target) {
    _target = target;
  }

  public void setAgiTemplateRegistry(AgiTemplateRegistry registry) {
    _registry = registry;
  }

  public void execute(ActionInvocation action) throws Exception {

    Class<?> c = getTemplateClassForTargetName();

    if (c == null) {
      System.err.println("no such template: " + _target);
      return;
    }

    ActionContext context = action.getInvocationContext();
    AgiTemplate template = buildTemplateInstance(context, c);
    AgiActionName nextAction = executeTemplate(context, template);

    if (nextAction != null)
      AgiEntryPoint.setNextAction(context, nextAction);
  }

  protected Class<?> getTemplateClassForTargetName() {
    Class<?> c = _registry.getTemplateClassForTargetName(_target);
    return c;
  }

  protected AgiActionName executeTemplate(ActionContext context,
      AgiTemplate template) throws Exception {
    AgiActionName nextAction = template.execute(context);
    return nextAction;
  }

  protected AgiTemplate buildTemplateInstance(ActionContext context, Class<?> c)
      throws Exception {
    Container container = context.getContainer();
    ObjectFactory factory = container.getInstance(ObjectFactory.class);
    AgiTemplate template = (AgiTemplate) factory.buildBean(c,
        context.getContextMap());
    return template;
  }

}
