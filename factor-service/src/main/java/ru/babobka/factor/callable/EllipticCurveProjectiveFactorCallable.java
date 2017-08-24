package ru.babobka.factor.callable;

import ru.babobka.factor.model.FactoringResult;
import ru.babobka.factor.model.ec.EllipticCurvePoint;
import ru.babobka.nodeutils.util.ArrayUtil;
import ru.babobka.nodeutils.util.MathUtil;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by dolgopolov.a on 24.11.15.
 */
public class EllipticCurveProjectiveFactorCallable implements Callable<FactoringResult> {

    //TODO оптимизируй расход памяти
    private static final List<Long> primes = new ArrayList<>();
    private static final int MIN_BORDER = 10000;
    private static long lastMaxBorder = 0L;
    private final BigInteger n;
    private final AtomicBoolean done;
    private long border;

    public EllipticCurveProjectiveFactorCallable(AtomicBoolean done, BigInteger n) {
        if (ArrayUtil.isNull(done, n)) {
            throw new IllegalArgumentException("all the values must be non null");
        }
        this.done = done;
        this.n = n;
        this.border = MathUtil.sqrtBig(MathUtil.sqrtBig(MathUtil.sqrtBig(n))).longValue();
        if (border < MIN_BORDER) {
            border = MIN_BORDER;
        }
        initPrimes(border);
    }

    public static List<EllipticCurveProjectiveFactorCallable> createCalls(AtomicBoolean done, BigInteger n, int calls) {
        if (calls <= 0) {
            throw new IllegalArgumentException("there must be at least one callable");
        }
        List<EllipticCurveProjectiveFactorCallable> callables = new ArrayList<>(calls);
        for (int i = 0; i < calls; i++) {
            callables.add(new EllipticCurveProjectiveFactorCallable(done, n));
        }
        return callables;
    }

    private static synchronized void initPrimes(long border) {
        if (border > lastMaxBorder) {
            for (long i = lastMaxBorder; i <= border; i++) {
                if (MathUtil.isPrime(i)) {
                    primes.add(i);
                }
            }
            lastMaxBorder = border;
        }
    }

    private FactoringResult factor() {
        EllipticCurvePoint p = EllipticCurvePoint.generateRandomPoint(n);
        BigInteger g = p.getCurve().getN().gcd(p.getX().getNumber());
        if (g.compareTo(BigInteger.ONE) >= 1 && g.compareTo(p.getCurve().getN()) < 0) {
            return new FactoringResult(g, p);
        }
        EllipticCurvePoint beginCurve = p.copy();
        for (Long prime : primes) {
            long r = MathUtil.log(prime, border);
            for (long j = 0; j < r; j++) {
                if (done.get()) {
                    return null;
                }
                p = p.mult(prime);
                if (p.isInfinityPoint()) {
                    p = EllipticCurvePoint.generateRandomPoint(n);
                    beginCurve = p.copy();
                    break;
                }
                g = p.getCurve().getN().gcd(p.getX().getNumber());
                if (g.compareTo(BigInteger.ONE) >= 1 && g.compareTo(p.getCurve().getN()) < 0) {
                    done.set(true);
                    return new FactoringResult(g, beginCurve);
                }
            }
        }

        return null;
    }

    @Override
    public FactoringResult call() {
        FactoringResult result = null;
        for (int i = 0; i < n.bitLength() * 2; i++) {
            result = factor();
            if (done.get()) {
                return result;
            }
            if (result != null) {
                break;
            }
        }
        return result;
    }
}
