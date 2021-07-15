package io.stargate.microb;

import java.util.List;
import java.util.regex.Pattern;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;

import com.google.common.collect.ImmutableList;

@BenchmarkMode(Mode.Throughput)
@State(Scope.Benchmark)
@Fork(value = 3)
@Measurement(iterations = 1, time = 3)
@Warmup(iterations = 1, time = 3)
public class IllegalCharsCheck
{
    private static final String FORBIDDEN_CHAR_STRING = "[],.'*";
    private static final List<Character> FORBIDDEN_CHARS_LIST;

    static {
        ImmutableList.Builder<Character> b = ImmutableList.builder();
        for (char c : FORBIDDEN_CHAR_STRING.toCharArray()) {
            b.add(c);
        }
        FORBIDDEN_CHARS_LIST = b.build();
//        = ImmutableList.of('[', ']', ',', '.', '\'', '*');
    }

    private final static long FORBIDDEN_MASK;
    static {
        long l = 0L;
        for (char c : FORBIDDEN_CHAR_STRING.toCharArray()) {
            l |= (1L << (c - 32));
        }
        FORBIDDEN_MASK = l;
    }

    private final static Pattern FORBIDDEN_PATTERN;
    static {
        StringBuilder sb = new StringBuilder().append("[");
        // to add escaping dynamically
        for (char c : FORBIDDEN_CHAR_STRING.toCharArray()) {
            sb.append(Pattern.quote(String.valueOf(c)));
        }
        FORBIDDEN_PATTERN = Pattern.compile(sb.append("]").toString());
    }
    

    // Find some balance; most with no forbidden, one or two with match,
    // including first and last entries
    private static final String[] TEST_STRINGS = new String[] {
            "basicName", "another one",
            "Foo*",
            "?",
            "Complete safe & sound!!!",
            "[nope]",
            "Typical_very",
            "--- Just some more stuff like that ---",
            "1"
    };

    private static final int EXP_MATCHES = 2;

    @Benchmark
    public int defaultNaiveLooping(Blackhole bh) {
        int count = 0;
        for (String term : TEST_STRINGS) {
            if (naiveMatch(term)) {
                ++count;
            }
        }
        return _verifyCount(count);
    }

    private boolean naiveMatch(String str) {
        return FORBIDDEN_CHARS_LIST.stream().anyMatch(ch -> str.indexOf(ch) >= 0);
    }

    @Benchmark
    public int explicitLoopingType1(Blackhole bh) {
        int count = 0;
        for (String term : TEST_STRINGS) {
            if (explicitMatch1(term)) { ++count; }
        }
        return _verifyCount(count);
    }

    private boolean explicitMatch1(String str) {
        for (int i = 0, len = FORBIDDEN_CHAR_STRING.length(); i < len; ++i) {
            if (str.indexOf(FORBIDDEN_CHAR_STRING.charAt(i)) >= 0) {
                return true;
            }
        }
        return false;
    }

    @Benchmark
    public int explicitLoopingType2(Blackhole bh) {
        int count = 0;
        for (String term : TEST_STRINGS) {
            if (explicitMatch2(term)) { ++count; }
        }
        return _verifyCount(count);
    }

    private boolean explicitMatch2(String str) {
        for (int i = 0, len = str.length(); i < len; ++i) {
            if (FORBIDDEN_CHAR_STRING.indexOf(str.charAt(i)) >= 0) {
                return true;
            }
        }
        return false;
    }


    @Benchmark
    public int checkUsingBitset(Blackhole bh) {
        int count = 0;
        for (String term : TEST_STRINGS) {
            if (_checkUsingBitSet(term)) { ++count; }
        }
        return _verifyCount(count);
    }

    private boolean _checkUsingBitSet(String str) {
        for (int i = 0, len = str.length(); i < len; ++i) {
            final int ch = str.charAt(i);
            int offset = ch - 32;
            if ((offset < 64) && (offset >= 0)
                    && (FORBIDDEN_MASK & (1L << offset)) != 0) {
                return true;
            }
        }
        return false;
    }

    @Benchmark
    public int regExpBasedCheck(Blackhole bh) {
        int count = 0;
        for (String term : TEST_STRINGS) {
            if (_regExpBasedCheck(term)) { ++count; }
        }
        return _verifyCount(count);
    }

    private boolean _regExpBasedCheck(String str) {
        return FORBIDDEN_PATTERN.matcher(str).find();
    }

    @Benchmark
    public int switchInLoop(Blackhole bh) {
        int count = 0;
        for (String term : TEST_STRINGS) {
            if (_matchWithLoopAndSwitch(term)) { ++count; }
        }
        return _verifyCount(count);
    }

    private boolean _matchWithLoopAndSwitch(String str) {
        for (int i = 0, len = str.length(); i < len; ++i) {
            switch (str.charAt(i)) {
            case '[':
            case ']':
            case ',':
            case '.':
            case '\'':
            case '*':
                return true;
            }
        }
        return false;

    }

    private int _verifyCount(int count) {
        if (count != EXP_MATCHES) {
            throw new RuntimeException(String.format("Wrong: should get %d matches, got %s",
                    EXP_MATCHES, count));
        }
        return count;
    }
}
