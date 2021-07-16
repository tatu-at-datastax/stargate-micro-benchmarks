package io.stargate.microb.chars;

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

import com.google.common.base.CharMatcher;

@BenchmarkMode(Mode.Throughput)
@State(Scope.Benchmark)
@Fork(value = 3)
@Measurement(iterations = 2, time = 3)
@Warmup(iterations = 1, time = 3)
public class IllegalCharsCheck
{
    private final static long FORBIDDEN_MASK;
    static {
        long l = 0L;
        for (char c : IllegalCharConstants.FORBIDDEN_CHARS_STRING.toCharArray()) {
            l |= (1L << (c - 32));
        }
        FORBIDDEN_MASK = l;
    }

    private final static Pattern FORBIDDEN_PATTERN;
    static {
        StringBuilder sb = new StringBuilder().append("[");
        // to add escaping dynamically
        for (char c : IllegalCharConstants.FORBIDDEN_CHARS_STRING.toCharArray()) {
            sb.append(Pattern.quote(String.valueOf(c)));
        }
        FORBIDDEN_PATTERN = Pattern.compile(sb.append("]").toString());
    }
    
    private final static CharMatcher FORBIDDEN_GUAVA_MATCHER = CharMatcher.anyOf(IllegalCharConstants.FORBIDDEN_CHARS_STRING);

    private final static String[] TEST_STRINGS = IllegalCharConstants.TEST_STRINGS_FOR_ILLEGAL_CHARS;

    private static final int EXP_MATCHES = IllegalCharConstants.TEST_STRING_ILLEGAL_COUNT;

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
        return IllegalCharConstants.FORBIDDEN_CHARS_AS_LIST.stream().anyMatch(ch -> str.indexOf(ch) >= 0);
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
        final String FORBIDDEN = IllegalCharConstants.FORBIDDEN_CHARS_STRING;
        for (int i = 0, len = FORBIDDEN.length(); i < len; ++i) {
            if (str.indexOf(FORBIDDEN.charAt(i)) >= 0) {
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
        final String FORBIDDEN = IllegalCharConstants.FORBIDDEN_CHARS_STRING;
        for (int i = 0, len = str.length(); i < len; ++i) {
            if (FORBIDDEN.indexOf(str.charAt(i)) >= 0) {
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
    public int guavaBasedCheck(Blackhole bh) {
        int count = 0;
        for (String term : TEST_STRINGS) {
            if (_guavaBasedCheck(term)) { ++count; }
        }
        return _verifyCount(count);
    }

    private boolean _guavaBasedCheck(String str) {
        return FORBIDDEN_GUAVA_MATCHER.matchesAnyOf(str);
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
