package org.onebusaway.probablecalls.agitemplates;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AgiTemplateRegistry implements Serializable {

  private static final long serialVersionUID = 1L;
  
  private Map<String, Class<?>> _templates = new HashMap<String, Class<?>>();

  public void setSource(AgiTemplateSource source) throws IOException {
    addSource(source);
  }

  public void setSources(List<AgiTemplateSource> sources) throws IOException {
    for (AgiTemplateSource source : sources)
      addSource(source);
  }

  public void addSource(AgiTemplateSource source) throws IOException {
    Map<String, Class<?>> templates = source.getTemplates();
    _templates.putAll(templates);
  }

  public Class<?> getTemplateClassForTargetName(String targetName) {
    return _templates.get(targetName);
  }
}
