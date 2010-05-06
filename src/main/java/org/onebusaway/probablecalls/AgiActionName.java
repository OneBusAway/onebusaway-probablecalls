package org.onebusaway.probablecalls;

import java.util.HashMap;
import java.util.Map;

public class AgiActionName {

  private String _namespace;

  private String _action;

  private boolean _excludedFromHistory = false;

  private Map<Object, Object> _params = new HashMap<Object, Object>();

  public AgiActionName(String namespace, String action) {
    _namespace = namespace;
    _action = action;
  }

  public AgiActionName(String action) {
    this("/", action);
  }

  public void setAction(String action) {
    _action = action;
  }

  public String getAction() {
    return _action;
  }

  public void setNamespace(String namespace) {
    _namespace = namespace;
  }

  public String getNamespace() {
    return _namespace;
  }

  public void putParam(Object key, Object value) {
    _params.put(key, value);
  }

  public void setParams(Map<Object, Object> params) {
    _params = params;
  }

  public Map<Object, Object> getParams() {
    return _params;
  }

  public void setExcludeFromHistory(boolean excludedFromHistory) {
    _excludedFromHistory = excludedFromHistory;
  }

  public boolean isExcludedFromHistory() {
    return _excludedFromHistory;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null || !(obj instanceof AgiActionName))
      return false;

    AgiActionName an = (AgiActionName) obj;
    return _action.equals(an.getAction()) && _params.equals(an.getParams());
  }

  @Override
  public int hashCode() {
    return _action.hashCode() + _params.hashCode();
  }

  @Override
  public String toString() {
    return _namespace + " " + _action + " " + _params;
  }
}
