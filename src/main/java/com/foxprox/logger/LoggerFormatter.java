package com.foxprox.logger;

import com.foxprox.logger.api.Colours;
import com.foxprox.logger.api.Components;
import com.foxprox.logger.api.Level;

public class LoggerFormatter {
    public static void format(String prefix, Level level, String message) {
        System.out.println(Colours.PURPLE + prefix + " " +  Components.separator + " " +  levelToColour(level)  + " " + Components.point + Colours.CYAN + " : " + Colours.PURPLE_BOLD + message);
    }
    private static String levelToColour(Level level) {
        switch (level) {
            case INFO:
                return Colours.GREEN + Components.question + " " + level.name().toUpperCase();
            case WARNING:
                return Colours.YELLOW + Components.warning + " " + level.name().toUpperCase();
            case SEVERE:
                return Colours.RED + Components.error + " " + level.name().toUpperCase();
            case CONFIG:
                return Colours.CYAN + "" + level.name().toUpperCase();
            default:
                return Colours.CYAN_UNDERLINED + "" + level.name().toUpperCase();
        }
    }
}
