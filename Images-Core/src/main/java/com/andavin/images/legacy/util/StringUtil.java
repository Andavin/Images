package com.andavin.images.legacy.util;

import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public final class StringUtil {

    // CENTER_CHAT and INVENTORY_LENGTH are both only half of
    // of the actual length of each of them
    private static final int CENTER_CHAT = 160, INVENTORY_LENGTH = 78, LORE_LENGTH = 240, BOOK_LENGTH = 108;

    /**
     * Split the given string into different lines so it
     * looks nice in the lore of an item and doesn't take
     * up too much room.
     * <p>
     * Basic formatting can be achieved through using & for
     * color codes and %n for new lines.
     *
     * @param str The string to split for lore.
     * @return The different lines for the lore.
     */
    public static List<String> splitForLore(String str) {

        String[] strings = str.split("%n");
        List<String> lines = new LinkedList<>();
        for (String string : strings) {

            int length = 0;
            boolean isBold = false, previousCode = false;
            for (int i = 0; i < string.length(); i++) {

                char c = string.charAt(i);
                if (c == ChatColor.COLOR_CHAR) {
                    previousCode = true;
                } else if (previousCode) {
                    previousCode = false;
                    isBold = c == 'l' || c == 'L';
                }

                // If we found a space and the length if greater than our max length
                else if (c == ' ' && length > LORE_LENGTH) {
                    // Cut off the portion we're at and add it
                    lines.add("§f" + string.substring(0, i));
                    // Then cut it off the string (along with the space)
                    string = string.substring(i + 1);
                    // Then reset everything to start at the beginning
                    // of the new string
                    length = 0;
                    i = 0;
                } else {
                    FontCharacter craftChar = FontCharacter.getByCharacter(c);
                    length += craftChar != null ? isBold ? craftChar.getBold() : craftChar.getLength() : 5;
                }
            }

            // Add any left overs to the string
            lines.add("§f" + string);
        }

        return lines;
    }

    /**
     * Split the given string into different lines so that it
     * fits on the page of a book.
     * <p>
     * A single string with proper line break characters will
     * be input into the string at the correct points.
     *
     * @param str The string to split for the book.
     * @param center If each of the lines should be centered.
     * @param color The color to place before each line.
     * @return The text for the book with line breaks.
     */
    public static String splitForBook(String str, boolean center, ChatColor color) {

        String[] strings = StringUtils.split(str, '\n');
        List<String> lines = new LinkedList<>();
        for (String string : strings) {

            int length = 0, lastSpace = -1;
            boolean isBold = false, previousCode = false;
            for (int i = 0; i < string.length(); i++) {

                char c = string.charAt(i);
                if (c == ' ') {
                    lastSpace = i;
                }

                if (c == ChatColor.COLOR_CHAR) {
                    previousCode = true;
                } else if (previousCode) {
                    previousCode = false;
                    isBold = c == 'l' || c == 'L';
                } else {
                    FontCharacter craftChar = FontCharacter.getByCharacter(c);
                    int addition = craftChar != null ? isBold ? craftChar.getBold() : craftChar.getLength() : 5;
                    length += addition;
                }

                if (length >= BOOK_LENGTH && lastSpace != -1) {

                    lines.add(string.substring(0, lastSpace));
                    if (lastSpace + 1 < string.length()) {
                        string = string.substring(lastSpace + 1);
                    } else {
                        string = "";
                    }

                    // Then reset everything to start at the beginning
                    // of the new string
                    lastSpace = -1;
                    length = 0;
                    i = 0;
                }
            }

            // Add any left overs to the string
            lines.add(string);
        }

        if (center) {

            for (int i = 0; i < lines.size(); i++) {
                lines.set(i, color + StringUtil.centerBookLine(lines.get(i)));
            }
        }

        return StringUtils.join(lines, '\n');
    }

    /**
     * Center the given message for chat placing the required
     * amount of spaces before it to make it centered.
     *
     * @param msg The message to center.
     * @return The centered message.
     */
    public static String centerMessage(String msg) {

        if (msg == null || msg.isEmpty()) {
            return msg;
        }

        int length = StringUtil.getLength(msg);
        StringBuilder spaces = new StringBuilder();
        int neededSpaced = CENTER_CHAT - length / 2;
        for (int spaced = 0; spaced < neededSpaced; spaced += FontCharacter.SPACE.getLength()) {
            spaces.append(' ');
        }

        return spaces.append(msg).toString();
    }

    /**
     * Center the title of an inventory on the top of the
     * GUI screen.
     *
     * @param title The title to center.
     * @return The centered title.
     */
    public static String centerTitle(String title) {

        if (title == null || title.isEmpty()) {
            return title;
        }

        if (title.length() >= 32) {
            return title.substring(32);
        }

        int length = StringUtil.getLength(title);
        StringBuilder spaces = new StringBuilder();
        int neededSpaced = INVENTORY_LENGTH - length / 2;
        for (int spaced = 0; spaced < neededSpaced; spaced += FontCharacter.SPACE.getLength()) {
            spaces.append(' ');
        }

        return spaces.append(title).toString();
    }

    /**
     * Center a line of text in a book.
     *
     * @param line The line of text to center
     * @return The line, but centered for a book.
     */
    public static String centerBookLine(String line) {

        if (line == null || line.isEmpty()) {
            return line;
        }

        int length = StringUtil.getLength(line);
        if (length == 0) {
            return line;
        }

        StringBuilder spaces = new StringBuilder();
        int neededSpaced = BOOK_LENGTH / 2 - length / 2;
        for (int spaced = 0; spaced < neededSpaced; spaced += FontCharacter.SPACE.getLength()) {
            spaces.append(' ');
        }

        return spaces.append(line).toString();
    }

    /**
     * Center the given string given a base that represents
     * the longest string to center off of.
     *
     * @param str The string to center.
     * @param base The base length to center off of.
     * @return The centered form of the string.
     */
    public static String centerWithBase(String str, int base) {

        if (str == null || str.isEmpty()) {
            return str;
        }

        int length = StringUtil.getLength(str);
        StringBuilder spaces = new StringBuilder();
        int neededSpaced = (base - length) / 2;
        for (int spaced = 0; spaced < neededSpaced; spaced += FontCharacter.SPACE.getLength()) {
            spaces.append(' ');
        }

        return spaces.append(str).toString();
    }

    /**
     * Center the given lines of text based on the longest
     * line in the text.
     *
     * @param lines The lines to center.
     * @return The centered text.
     */
    public static List<String> centerLines(List<String> lines) {

        if (lines == null || lines.isEmpty()) {
            return lines;
        }

        int fullSize = StringUtil.getLongestLine(lines);
        List<String> newLines = new ArrayList<>(lines.size());
        lines.forEach(line -> {

            int length = StringUtil.getLength(line);
            StringBuilder spaces = new StringBuilder();
            int neededSpaces = (fullSize - length) / 2;
            for (int spaced = 0; spaced < neededSpaces; spaced += FontCharacter.SPACE.getLength()) {
                spaces.append(' ');
            }

            newLines.add(spaces.append(line).toString());
        });

        return newLines;
    }

    /**
     * Get the line with the longest total length in
     * the list of lines given.
     *
     * @param lines The lines of text to get the longest of.
     * @return The length of the longest line.
     */
    private static int getLongestLine(List<String> lines) {

        if (lines == null || lines.isEmpty()) {
            return 0;
        }

        int longest = 0;
        for (String line : lines) {

            int length = StringUtil.getLength(line);
            if (longest < length) {
                longest = length;
            }
        }

        return longest;
    }

    /**
     * Get the length of the given string where each letter
     * is based off of the widths given in it's respective
     * {@link FontCharacter} character.
     *
     * @param str The string to get the length of.
     * @return The length of the string.
     */
    static int getLength(String str) {

        int length = 0;
        boolean isBold = false, previousCode = false;
        for (char c : str.toCharArray()) {

            if (c == ChatColor.COLOR_CHAR) {
                previousCode = true;
            } else if (previousCode) {
                previousCode = false;
                isBold = c == 'l' || c == 'L';
            } else {
                FontCharacter fontChar = FontCharacter.getByCharacter(c);
                length += fontChar != null ? isBold ? fontChar.getBold() : fontChar.getLength() : 5;
            }
        }

        return length;
    }
}
