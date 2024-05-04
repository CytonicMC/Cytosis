/*
    THIS CODE WAS WRITTEN BY THE CONTRIBUTORS OF 'Minestom/VanillaReimplementation'
    https://github.com/Minestom/VanillaReimplementation
    ** THIS FILE MAY HAVE BEEN EDITED BY THE CYTONIC DEVELOPMENT TEAM **
 */
package net.cytonic.cytosis.logging;

import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Calendar;
import java.util.Objects;
import java.util.regex.Pattern;

record LoggerImpl(Level level) implements Logger {

    public static final Level LOG_LEVEL = Level.valueOf(Objects.requireNonNullElse(System.getenv("cytosis.log-level"), "SETUP").toUpperCase());
    static final LoggerImpl DEFAULT = new LoggerImpl(LOG_LEVEL);

    private static LoggerImpl lastLogger = DEFAULT;
    private static final Object printLock = new Object();
    private static final PrintStream sysOut = System.out;
    /**
     * true if this logger implementation was the last used to log a message. false if it was an external call to {@link System#out}
     */
    private static boolean loggerWasLast = true;
    private static boolean newLine = true;

    static {
        System.setOut(new PrintStream(new OutputStream() {
            @Override
            public void write(int b) {
                synchronized (printLock) {
                    if (loggerWasLast) {
                        loggerWasLast = false;
                        if (!newLine)
                            lastLogger.newLine();
                    }
                    sysOut.write(b);
                }
            }
        }, false));
    }

    @Override
    public Logger level(Level level) {
        if (this.level == level) return this;
        return new LoggerImpl(level);
    }

    private void consolePrint(String str) {
        sysOut.print(str);
        loggerWasLast = true;
        lastLogger = this;
    }

    private void newLine() {
        consolePrint(System.lineSeparator());
        newLine = true;
    }

    @Override
    public Logger print(String message) {
        synchronized (printLock) {
            if (LOG_LEVEL.ordinal() > level.ordinal()) return this;
            if (loggerWasLast && !lastLogger.equals(this) && !newLine) {
                newLine();
            }
            if (newLine) {
                consolePrint(preparePrefix());
                newLine = false;
            }
            String[] lines = message.split(Pattern.quote(System.lineSeparator()), -1);
            if (lines.length == 1 && !message.equals(System.lineSeparator())) {
                printNonNewLine(translateColor() + message);
                return this;
            }
            for (int i = 0; i < lines.length; i++) {
                String line = lines[i];
                if (i != 0) newLine();
                if (!line.isEmpty()) print(line);
            }
            return this;
        }
    }

    private void printNonNewLine(String message) {
        if (!message.contains("\r")) {
            consolePrint(translateColor() + message);
            return;
        }

        String[] split = message.split(Pattern.quote("\r"), -1);
        consolePrint("\r");
        consolePrint(preparePrefix());
        consolePrint(translateColor() + split[split.length - 1]);
    }

    private String preparePrefix() {
        Calendar date = Calendar.getInstance();
        int seconds = date.get(Calendar.SECOND);
        int minutes = date.get(Calendar.MINUTE);
        int hours = date.get(Calendar.HOUR_OF_DAY);
        return String.format("%s[%s:%s:%02d]%s %s -> ",
                Color.GREEN,
                hours, minutes, seconds,
                Color.RESET,
                prepareLevelPrefix());
    }

    @Override
    public Logger nextLine() {
        synchronized (printLock) {
            if (LOG_LEVEL.ordinal() < level.ordinal()) return this;
            if (!newLine) newLine();
            return this;
        }
    }

    private String prepareLevelPrefix() {
        return String.format("%s[%s]%s", translateColor(), level, Color.RESET);
    }

    @Override
    public Logger printf(String message, Object... args) {
        return print(String.format(translateColor() + message, args));
    }

    @Override
    public Logger throwable(Throwable throwable, Object... args) {
        String info = "";
        if (args.length == 1) {
            info = " -> (" + args[0] + ")";
        } else if (args.length > 1) {
            info = " -> (" + String.format(args[0].toString(), args[1]) + ")";
        }
        return print(throwable.getMessage() + info);
    }

    @Override
    public Logger throwable(String message, Throwable ex) {
        String throwable = "";
        if (ex != null) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            pw.println();
            ex.printStackTrace(pw);
            pw.close();
            throwable = sw.toString();
        }
        return print(message + throwable);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (!(obj instanceof LoggerImpl other)) return false;
        return level == other.level;
    }

    public Color translateColor() {
        return switch (level) {
            case TRACE, INFO -> Color.WHITE;
            case DEBUG -> Color.BLUE;
            case SETUP -> Color.MAGENTA;
            case WARN -> Color.YELLOW;
            case ERROR -> Color.RED_BOLD_BRIGHT;
        };
    }
}