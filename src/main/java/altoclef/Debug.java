/*
 * This file is part of Baritone.
 *
 * Baritone is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Baritone is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Baritone.  If not, see <https://www.gnu.org/licenses/>.
 */

package altoclef;

import altoclef.player2api.Character;

public class Debug {
   private static final int DEBUG_LOG_LEVEL = 0;
   private static final int WARN_LOG_LEVEL = 1;
   private static final int ERROR_LOG_LEVEL = 2;

   public static void logInternal(String message) {
      if (canLog(0)) {
         System.out.println("ALTO CLEF: " + message);
      }
   }

   public static void logInternal(String format, Object... args) {
      logInternal(String.format(format, args));
   }

   private static String getLogPrefix() {
      return "[Alto Clef] ";
   }

   public static void logMessage(String message, boolean prefix) {
      logInternal(message);
   }

   public static void logCharacterMessage(String message, Character character, boolean isPublic) {
      message = String.format("§1§l§o<%s>§r %s", character.shortName(), message);
      logInternal(message);
   }

   public static void logUserMessage(String message) {
      logInternal(message);
   }

   public static void logMessage(String message) {
      logMessage(message, true);
   }

   public static void logMessage(String format, Object... args) {
      logMessage(String.format(format, args));
   }

   public static void logWarning(String message) {
      if (canLog(1)) {
         System.out.println("ALTO CLEF: WARNING: " + message);
      }
   }

   public static void logWarning(String format, Object... args) {
      logWarning(String.format(format, args));
   }

   public static void logError(String message) {
      String stacktrace = getStack(2);
      if (canLog(2)) {
         System.err.println(message);
         System.err.println("at:");
         System.err.println(stacktrace);
      }
   }

   public static void logError(String format, Object... args) {
      logError(String.format(format, args));
   }

   public static void logStack() {
      logInternal("STACKTRACE: \n" + getStack(2));
   }

   private static String getStack(int toSkip) {
      StringBuilder stacktrace = new StringBuilder();

      for (StackTraceElement ste : Thread.currentThread().getStackTrace()) {
         if (toSkip-- <= 0) {
            stacktrace.append(ste.toString()).append("\n");
         }
      }

      return stacktrace.toString();
   }

   private static boolean canLog(int level) {
      String enabledLogLevel = "ALL";
      switch (enabledLogLevel) {
         case "NONE":
         case "ALL":
         case "NORMAL":
            return level == 1 || level == 2;
         case "WARN":
            return level == 1;
         case "ERROR":
            return level == 2;
         default:
            return level != 0;
      }
   }
}
