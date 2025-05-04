import java.util.*;

/*
JDice: Java Dice Rolling Program
Copyright (C) 2006 Andrew D. Hilton  (adhilton@cis.upenn.edu)

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
*/

public class DiceParser {

    /**
     * A helper class to process the input string like a stream.
     */
    private static class StringStream {
        StringBuffer buffer;

        /** Refactored: changed 'buff' → 'buffer' for clarity */
        public StringStream(String s) {
            buffer = new StringBuffer(s);
        }

        /** Refactored: renamed 'munchWhiteSpace' → 'skipWhitespace' for clarity */
        private void skipWhitespace() {
            int index = 0;
            while (index < buffer.length() && Character.isWhitespace(buffer.charAt(index))) {
                index++;
            }
            buffer.delete(0, index);
        }

        public boolean isEmpty() {
            skipWhitespace();
            return buffer.length() == 0;
        }

        /** Refactored: renamed and unified into one method (no longer need separate getInt vs readInt) */
        public Integer readInt() {
            skipWhitespace();
            int index = 0;
            while (index < buffer.length() && Character.isDigit(buffer.charAt(index))) {
                index++;
            }

            if (index == 0) return null;

            try {
                Integer value = Integer.parseInt(buffer.substring(0, index));
                buffer.delete(0, index);
                return value;
            } catch (NumberFormatException e) {
                return null;
            }
        }

        public Integer readSignedInt() {
            skipWhitespace();
            StringStream saved = save();
            if (checkAndConsume("+")) {
                Integer num = readInt();
                if (num != null) return num;
                restore(saved);
                return null;
            } else if (checkAndConsume("-")) {
                Integer num = readInt();
                if (num != null) return -num;
                restore(saved);
                return null;
            }
            return readInt();
        }

        /** Renamed: checkAndEat → checkAndConsume for clarity */
        public boolean checkAndConsume(String s) {
            skipWhitespace();
            if (buffer.indexOf(s) == 0) {
                buffer.delete(0, s.length());
                return true;
            }
            return false;
        }

        public StringStream save() {
            return new StringStream(buffer.toString());
        }

        public void restore(StringStream other) {
            this.buffer = new StringBuffer(other.buffer);
        }

        @Override
        public String toString() {
            return buffer.toString();
        }
    }

    /** Entry point for parsing a full roll expression */
    public static Vector<DieRoll> parseRoll(String input) {
        StringStream stream = new StringStream(input.toLowerCase());
        Vector<DieRoll> rolls = parseMultipleRolls(stream, new Vector<>());
        if (stream.isEmpty()) {
            return rolls;
        }
        return null;
    }

    /** Parses multiple roll segments separated by ';' */
    private static Vector<DieRoll> parseMultipleRolls(StringStream stream, Vector<DieRoll> accumulator) {
        Vector<DieRoll> group = parseXDiceGroup(stream);
        if (group == null) return null;
        accumulator.addAll(group);
        if (stream.checkAndConsume(";")) {
            return parseMultipleRolls(stream, accumulator);
        }
        return accumulator;
    }

    /** Parses formats like '4x3d6+2' into multiple identical rolls */
    private static Vector<DieRoll> parseXDiceGroup(StringStream stream) {
        StringStream saved = stream.save();
        Integer times = stream.readInt();
        int repeat = 1;

        if (times != null && stream.checkAndConsume("x")) {
            repeat = times;
        } else {
            stream.restore(saved);
        }

        DieRoll dieRoll = parseDice(stream);
        if (dieRoll == null) return null;

        Vector<DieRoll> results = new Vector<>();
        for (int i = 0; i < repeat; i++) {
            results.add(dieRoll);
        }
        return results;
    }

    /** Parses a single dice expression, possibly with "&" chaining */
    private static DieRoll parseDice(StringStream stream) {
        return parseAndChain(parseDiceInner(stream), stream);
    }

    /** Parses individual dice string like '2d6+3' */
    private static DieRoll parseDiceInner(StringStream stream) {
        Integer count = stream.readInt();
        int numDice = count != null ? count : 1;

        if (!stream.checkAndConsume("d")) return null;

        Integer sides = stream.readInt();
        if (sides == null) return null;

        Integer modifier = stream.readSignedInt();
        int bonus = modifier != null ? modifier : 0;

        return new DieRoll(numDice, sides, bonus);
    }

    /** Recursively parses '&' joined dice expressions into DiceSum */
    private static DieRoll parseAndChain(DieRoll firstRoll, StringStream stream) {
        if (firstRoll == null) return null;

        if (stream.checkAndConsume("&")) {
            DieRoll secondRoll = parseDice(stream);
            return parseAndChain(new DiceSum(firstRoll, secondRoll), stream);
        }
        return firstRoll;
    }

    /** Refactored: test method made cleaner with better naming and output */
    private static void test(String expression) {
        Vector<DieRoll> rolls = parseRoll(expression);
        if (rolls == null) {
            System.out.println("❌ Invalid input: " + expression);
        } else {
            System.out.println("✅ Parsing result for \"" + expression + "\":");
            for (DieRoll roll : rolls) {
                System.out.println("  " + roll + " → " + roll.makeRoll());
            }
        }
    }

    public static void main(String[] args) {
        test("d6");
        test("2d6");
        test("d6+5");
        test("4x3d8-5");
        test("12d10+5 & 4d6+2");
        test("d6 ; 2d4+3");
        test("4d6+3 ; 8d12 -15 ; 9d10 & 3d6 & 4d12 +17");
        test("4d6 + xyzzy");  // Expected to fail
        test("hi");           // Expected to fail
        test("4d4d4");        // Invalid
    }
}