package io.stargate.microb.chars;

import java.util.List;

import com.google.common.collect.ImmutableList;

class IllegalCharConstants
{
    public static final String FORBIDDEN_CHARS_STRING = "[],.'*";

    public static final List<Character> FORBIDDEN_CHARS_AS_LIST;
    static {
        ImmutableList.Builder<Character> b = ImmutableList.builder();
        for (char c : FORBIDDEN_CHARS_STRING.toCharArray()) {
            b.add(c);
        }
        FORBIDDEN_CHARS_AS_LIST = b.build();
    }

    // Find some balance; most with no forbidden, one or two with match,
    // including first and last entries
    public static final String[] TEST_STRINGS_FOR_ILLEGAL_CHARS = new String[] {
            "basicName",
            "another one",
            "Foo*", // match
            "?",
            "Completely safe & sound!!!",
            "[nope]", // match
            "Typical_very",
            "--- Just some more stuff like that ---",
            "1",
            "Not complete 'done'" // match
    };

    // And of test Strings, 3 have illegal characters
    public static final int TEST_STRING_ILLEGAL_COUNT = 3;
}
