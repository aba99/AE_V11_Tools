package com.automic.api;

import com.uc4.communication.TraceListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class LoggingTraceListener implements TraceListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoggingTraceListener.class);

    @Override
    public void messageIn(String msg) {
        LOGGER.debug(msg);
    }

    @Override
    public void messageOut(String msg) {
        LOGGER.debug(msg);
    }
}
