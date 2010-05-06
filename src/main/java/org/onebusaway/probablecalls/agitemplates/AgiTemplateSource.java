package org.onebusaway.probablecalls.agitemplates;

import java.io.IOException;
import java.util.Map;

public interface AgiTemplateSource {
    public Map<String, Class<?>> getTemplates() throws IOException;
}
