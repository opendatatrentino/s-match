package it.unitn.disi.smatch.gui;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;

import javax.swing.*;

/**
 * Logs Log4J messages into S-Match GUI window.
 *
 * @author <a rel="author" href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public class SMatchGUILog4Appender extends AppenderSkeleton {

    private static JTextArea taLog = null;
    private static StringBuilder cache = new StringBuilder();

    public static void setTextArea(JTextArea newLogArea) {
        taLog = newLogArea;
        if (0 < cache.length()) {
            taLog.append(cache.toString());
            cache = new StringBuilder();
        }
    }

    public static JTextArea getTextArea() {
        return taLog;
    }

    @Override
    protected void append(LoggingEvent loggingEvent) {
        String logOutput = this.layout.format(loggingEvent);
        if (null != taLog) {
            taLog.append(logOutput);
        } else {
            cache.append(logOutput).append("\n");
        }
    }

    public void close() {
        //nop
    }

    public boolean requiresLayout() {
        return true;
    }
}
