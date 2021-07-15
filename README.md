# Stargate Micro-Benchmarks (feat: jmh)

As per what's on the tin: this repo contains various trivial micro-benchmarks I use
to validate (or disprove) minor performance improvements, especially ones that
are unlikely to have globally measurable effect on overall performance.

## Results

### Illegal Chars Check test

On my dev machine I get numbers like this (reordered fastest-to-slowest):

```
java -jar target/microbenchmarks.jar .\*IllegalChars.\*

Benchmark                                Mode  Cnt        Score         Error  Units
IllegalCharsCheck.checkUsingBitset      thrpt    6  7766475.485 ± 4833682.790  ops/s
IllegalCharsCheck.switchInLoop          thrpt    6  7667539.164 ± 1236293.708  ops/s
IllegalCharsCheck.explicitLoopingType2  thrpt    6  5398358.111 ±  220305.428  ops/s
IllegalCharsCheck.explicitLoopingType1  thrpt    6  3624089.868 ± 1733100.773  ops/s
IllegalCharsCheck.guavaBasedCheck       thrpt    6  1808928.499 ±  758903.593  ops/s
IllegalCharsCheck.regExpBasedCheck      thrpt    6  1779749.670 ±   44544.703  ops/s
IllegalCharsCheck.defaultNaiveLooping   thrpt    6  1591499.269 ±   53157.329  ops/s
```

So: 3 groups in terms of performance:

1. Using `switch` or `long`-backed bitset is the fastest: due to simplicity I'd prefer switch
2. Explicitly looping, using `String.indexOf()` can be quite fast too, simple
3. Use of Regexp generated from chars is similar to Guava (perhaps Guava uses that apparoach); naive Stream usage has high overhead as well

