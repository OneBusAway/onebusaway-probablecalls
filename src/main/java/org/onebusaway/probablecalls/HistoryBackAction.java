package org.onebusaway.probablecalls;

import com.opensymphony.xwork2.ActionSupport;

public class HistoryBackAction extends ActionSupport {

  private static final long serialVersionUID = 1L;

  private int _count = 1;

  public void setCount(int count) {
    _count = count;
  }

  public int getCount() {
    return _count;
  }
}
