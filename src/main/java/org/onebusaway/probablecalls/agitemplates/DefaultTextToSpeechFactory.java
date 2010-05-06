package org.onebusaway.probablecalls.agitemplates;

import java.io.IOException;

import org.asteriskjava.fastagi.AgiException;
import org.asteriskjava.fastagi.AgiOperations;
import org.onebusaway.probablecalls.TextToSpeechFactory;

public class DefaultTextToSpeechFactory implements TextToSpeechFactory {

    public char getAudio(AgiOperations opts, String text, String escapeDigits) throws IOException, AgiException {
        return opts.sayAlpha(text, escapeDigits);
    }

}
