package org.onebusaway.probablecalls;

import java.io.IOException;

import org.asteriskjava.fastagi.AgiException;
import org.asteriskjava.fastagi.AgiOperations;

public interface TextToSpeechFactory {
    public char getAudio(AgiOperations opts, String text, String escapeDigits) throws IOException, AgiException;
}
