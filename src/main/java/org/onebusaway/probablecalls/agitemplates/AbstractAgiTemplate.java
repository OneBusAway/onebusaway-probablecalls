package org.onebusaway.probablecalls.agitemplates;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.asteriskjava.fastagi.AgiChannel;
import org.asteriskjava.fastagi.AgiException;
import org.asteriskjava.fastagi.AgiOperations;
import org.onebusaway.probablecalls.AgiActionName;
import org.onebusaway.probablecalls.AgiEntryPoint;
import org.onebusaway.probablecalls.History;
import org.onebusaway.probablecalls.TextToSpeechFactory;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.util.LocalizedTextUtil;
import com.opensymphony.xwork2.util.ValueStack;

public abstract class AbstractAgiTemplate implements AgiTemplate {

  private static final String ALL_DIGITS = "0123456789#*";

  private static final long serialVersionUID = 1L;

  private boolean _built = false;

  private boolean _buildOnEachReqeust = false;

  private TextToSpeechFactory _textToSpeechFactory = new DefaultTextToSpeechFactory();

  private List<AgiTemplateOperation> _operations = new ArrayList<AgiTemplateOperation>();

  private List<AgiDTMFOperation> _actionMappings = new ArrayList<AgiDTMFOperation>();

  private int _secondaryTimeout = 5000;

  private AgiActionName _nextAction = null;

  private boolean _hangupOnCompletion = false;

  private ActionContext _context;

  public AbstractAgiTemplate() {
    this(false);
  }

  public AbstractAgiTemplate(boolean buildOnEachRequest) {
    _buildOnEachReqeust = buildOnEachRequest;
  }

  public void setTextToSpeechFactory(TextToSpeechFactory textToSpeechFactory) {
    _textToSpeechFactory = textToSpeechFactory;
  }

  public AgiActionName execute(ActionContext context) throws Exception {

    _context = context;

    checkBuild(context);

    // Push an entry onto the history stack
    AgiActionName action = AgiEntryPoint.getActiveAction(_context);
    if (!action.isExcludedFromHistory())
      History.push(_context, action);

    AgiChannel channel = AgiEntryPoint.getAgiChannel(_context);
    AgiOperations opts = new AgiOperations(channel);

    while (true) {

      boolean repeat = false;

      for (AgiTemplateOperation op : _operations) {

        char c = op.execute(context, opts);

        if (c != '\0') {
          AgiActionName result = waitForRemainingInput(context, opts, c);
          if (result != null) {
            if (result.getAction().equals("/repeat")) {
              repeat = true;
              break;
            }
            return result;
          }

          break;
        }
      }

      if (repeat)
        continue;

      if (_nextAction != null)
        return _nextAction;

      if (_hangupOnCompletion) {
        opts.hangup();
        return null;
      }
    }
  }

  public abstract void buildTemplate(ActionContext context);

  /***************************************************************************
   * Protected Methods
   **************************************************************************/

  protected AgiActionName addAction(String dtmfRegex, String action,
      Object... params) {

    if (params.length % 2 != 0)
      throw new IllegalArgumentException(
          "params must be an even set of key-value pairs");

    AgiActionName agiAction = addActionWithParameterFromMatch(dtmfRegex,
        action, null, 0);

    for (int i = 0; i < params.length; i += 2)
      agiAction.putParam(params[i], params[i + 1]);

    return agiAction;
  }

  protected AgiActionName addActionWithParameterFromMatch(String dtmfRegex,
      String action, String parameterFromMatch, int matchGroup) {

    if (dtmfRegex == null || dtmfRegex.length() == 0)
      throw new IllegalArgumentException("dtmfRegex cannot be null or empty");

    if (!dtmfRegex.startsWith("^"))
      dtmfRegex = "^" + dtmfRegex;

    if (!dtmfRegex.endsWith("$"))
      dtmfRegex = dtmfRegex + "$";

    Pattern pattern = Pattern.compile(dtmfRegex);

    DTMFActionMapping mapping = new DTMFActionMapping(action);
    mapping.setPattern(pattern);
    mapping.setParameterFromMatch(parameterFromMatch);
    mapping.setMatchGroup(matchGroup);

    _actionMappings.add(mapping);

    return mapping.getAction();
  }

  protected void addAction(AgiDTMFOperation operation) {
    _actionMappings.add(operation);
  }

  protected AgiActionName setNextAction(String action) {
    if (_hangupOnCompletion && action != null)
      throw new IllegalArgumentException(
          "You cannot specify a hangup on template completion AND a default next action");
    _nextAction = new AgiActionName(action);
    return _nextAction;
  }

  protected void setHangupOnCompletion(boolean hangupOnCompletion) {
    if (hangupOnCompletion && _nextAction != null)
      throw new IllegalArgumentException(
          "You cannot specify a hangup on template completion AND a default next action");
    _hangupOnCompletion = hangupOnCompletion;
  }

  protected String getCallerIdNumber() {
    return AgiEntryPoint.getCallerIdNumber(_context);
  }

  /***************************************************************************
     * 
     **************************************************************************/

  protected void addPause(int durationInMilliseconds) {
    _operations.add(new PauseOperation(durationInMilliseconds));
  }

  protected void addMessage(String message, Object... args) {
    _operations.add(new MessageOperation(message, args));
  }

  protected void addText(String text) {
    _operations.add(new TextOperation(text));
  }

  protected void addOperation(AgiTemplateOperation operation) {
    _operations.add(operation);
  }

  protected void setSecondaryTimeout(int timeoutInMilliseconds) {
    _secondaryTimeout = timeoutInMilliseconds;
  }

  /***************************************************************************
   * Private Methods
   **************************************************************************/

  private synchronized void checkBuild(ActionContext context) {
    if (!_built || _buildOnEachReqeust) {
      buildTemplate(context);
      _built = true;
    }
  }

  private AgiActionName waitForRemainingInput(ActionContext context,
      AgiOperations opts, char c) throws AgiException {

    StringBuilder b = new StringBuilder();

    while (true) {

      b.append(c);

      for (AgiDTMFOperation mapping : _actionMappings) {
        AgiActionName result = mapping.execute(context, b.toString());
        if (result != null)
          return result;
      }

      c = opts.waitForDigit(_secondaryTimeout);

      if (c == '\0') {
        if (_nextAction != null)
          return _nextAction;
        return null;
      }
    }
  }

  private final class PauseOperation implements AgiTemplateOperation {

    private int _duration;

    public PauseOperation(int duration) {
      _duration = duration;
    }

    public char execute(ActionContext context, AgiOperations opts)
        throws IOException, AgiException {
      return opts.waitForDigit(_duration);
    }
  }

  private final class TextOperation implements AgiTemplateOperation {

    private String _text;

    public TextOperation(String text) {
      _text = text;
    }

    public char execute(ActionContext context, AgiOperations opts)
        throws IOException, AgiException {
      return _textToSpeechFactory.getAudio(opts, _text, ALL_DIGITS);
    }
  }

  private final class MessageOperation implements AgiTemplateOperation {

    private String _message;

    private Object[] _args;

    public MessageOperation(String message, Object[] args) {
      _message = message;
      _args = args;
    }

    public char execute(ActionContext context, AgiOperations opts)
        throws IOException, AgiException {
      Locale locale = context.getLocale();
      ValueStack valueStack = context.getValueStack();
      String text = LocalizedTextUtil.findText(
          AbstractAgiTemplate.this.getClass(), _message, locale, _message,
          _args, valueStack);
      return _textToSpeechFactory.getAudio(opts, text, ALL_DIGITS);
    }
  }

  private final class DTMFActionMapping implements AgiDTMFOperation {

    private Pattern _pattern;

    private AgiActionName _action;

    private String _parameterFromMatch;

    private int _matchGroup;

    public DTMFActionMapping(String name) {
      _action = new AgiActionName(name);
    }

    public AgiActionName getAction() {
      return _action;
    }

    public void setPattern(Pattern pattern) {
      _pattern = pattern;
    }

    public void setParameterFromMatch(String parameter) {
      _parameterFromMatch = parameter;
    }

    public void setMatchGroup(int matchGroup) {
      _matchGroup = matchGroup;
    }

    public AgiActionName execute(ActionContext context, String digitsDialed) {

      Matcher matcher = _pattern.matcher(digitsDialed);

      if (matcher.matches()) {
        if (_parameterFromMatch != null && matcher.groupCount() >= _matchGroup)
          _action.putParam(_parameterFromMatch, matcher.group(_matchGroup));

        return _action;
      }

      return null;
    }
  }
}
