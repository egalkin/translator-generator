package ru.ifmo.galkin.utils;

import java.util.StringJoiner;

public class FormatUtils {
    private static final String wsAndStringTemplate = "%s%s";

    public static String getWhitespacesString(int innerLevel) {
        if (innerLevel == 0)
            return "";
        else {
            StringBuilder whitespaces = new StringBuilder();
            for (int i = 0; i < innerLevel * 4; ++i)
                whitespaces.append(" ");
            return whitespaces.toString();
        }
    }

    public static StringJoiner getDefaultStringJoiner(String ws, boolean extraLineFeed) {
        return new StringJoiner(String.format(";\n%s", ws), ws, ";\n" + (extraLineFeed ? "\n" : "" ));
    }

    public static String getDefaultEnd(String ws, boolean extraLineFeed) {
        return String.format(wsAndStringTemplate, ws, "}\n" + (extraLineFeed ? "\n" : "" ));
    }

    public static String getModifiedWs(String ws, int modifier) {
        return FormatUtils.getWhitespacesString((ws.length() + modifier)/4);
    }

}
