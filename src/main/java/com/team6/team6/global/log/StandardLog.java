package com.team6.team6.global.log;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;

import static com.team6.team6.global.log.LogType.ERROR;
import static com.team6.team6.global.log.LogType.INFO;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StandardLog extends AbstractLog {
    private final String message;
    private final String exceptionName;
    private final String exceptionMessage;

    private StandardLog(LogType type, String message, Throwable exception) {
        super(type);
        this.message = message;

        if (exception != null) {
            this.exceptionName = exception.getClass().getName();
            this.exceptionMessage = exception.getMessage();
        } else {
            this.exceptionName = null;
            this.exceptionMessage = null;
        }
    }

    public static StandardLog info(String message) {
        return new StandardLog(INFO, message, null);
    }

    public static StandardLog error(String message, Throwable exception) {
        return new StandardLog(ERROR, message, exception);
    }

    @Override
    public void output() {
        switch (getType()) {
            case INFO:
                log.info(getLogMessage());
                break;
            case ERROR:
                log.error(getLogMessage());
                break;
            default:
                log.warn("Unknown log type: {}", getType());
        }
    }
}
