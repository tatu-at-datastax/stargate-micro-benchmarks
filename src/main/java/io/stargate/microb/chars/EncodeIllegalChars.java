package io.stargate.microb.chars;

import java.util.List;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;

@BenchmarkMode(Mode.Throughput)
@State(Scope.Benchmark)
@Fork(value = 3)
@Measurement(iterations = 2, time = 3)
@Warmup(iterations = 1, time = 3)
public class EncodeIllegalChars
{
    private final static String[] TEST_STRINGS = IllegalCharConstants.TEST_STRINGS_FOR_ILLEGAL_CHARS;

    private static final int EXP_MATCHES = IllegalCharConstants.TEST_STRING_ILLEGAL_COUNT;

    @Benchmark
    public int naiveEncode(Blackhole bh) {
        int count = 0;
        for (String term : TEST_STRINGS) {
            String repl = _replaceNaive(term);
            if (repl != term) { ++count; }
        }
        return _verifyCount(count);
    }

    private String _replaceNaive(String str)
    {
        String newStr = str;
        for (Character y : IllegalCharConstants.FORBIDDEN_CHARS_AS_LIST) {
          newStr = newStr.replace(y, '_');
        }
        return newStr;
    }

    @Benchmark
    public int switchBasedEncode(Blackhole bh) {
        int count = 0;
        for (String term : TEST_STRINGS) {
            String repl = _replaceWithSwitch(term);
            if (repl != term) { ++count; }
        }
        return _verifyCount(count);
    }

    private String _replaceWithSwitch(String str)
    {
        StringBuilder result = null;

        for (int i = 0, len = str.length(); i < len; ++i) {
            switch (str.charAt(i)) {
            case '[':
            case ']':
            case ',':
            case '.':
            case '\'':
            case '*':
                if (result == null) {
                    result = new StringBuilder(str);
                }
                result.setCharAt(i, '_');
            }
        }
        return (result == null) ? str : result.toString();
    }

    private int _verifyCount(int count) {
        if (count != EXP_MATCHES) {
            throw new RuntimeException(String.format("Wrong: should have replaced %d Strings, did %s",
                    EXP_MATCHES, count));
        }
        return count;
    }
}
