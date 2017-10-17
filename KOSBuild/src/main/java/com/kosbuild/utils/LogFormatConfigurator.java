package com.kosbuild.utils;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.Configurator;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.spi.ContextAwareBase;

public class LogFormatConfigurator extends ContextAwareBase implements Configurator {

    @Override
    public void configure(LoggerContext lc) {
        Logger rootLogger = lc.getLogger("ROOT");

        rootLogger.setLevel(Level.toLevel("INFO"));
        PatternLayoutEncoder encoder = new PatternLayoutEncoder();
        encoder.setContext(lc);

        encoder.setPattern("[%-5level%d{HH:mm:ss}] %msg%n");

        encoder.start();
        ConsoleAppender<ILoggingEvent> appender = new ConsoleAppender<ILoggingEvent>();
        appender.setContext(lc);
        appender.setEncoder(encoder);
        appender.start();

        rootLogger.addAppender(appender);
    }
}
