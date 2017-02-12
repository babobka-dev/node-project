package ru.babobka.primecounter.task;

import ru.babobka.nodeserials.NodeRequest;
import ru.babobka.primecounter.callable.PrimeCounterCallable;
import ru.babobka.primecounter.model.PrimeCounterDistributor;
import ru.babobka.primecounter.model.PrimeCounterReducer;
import ru.babobka.primecounter.model.Range;
import ru.babobka.primecounter.util.MathUtil;

import ru.babobka.subtask.model.ExecutionResult;
import ru.babobka.subtask.model.Reducer;
import ru.babobka.subtask.model.RequestDistributor;
import ru.babobka.subtask.model.SubTask;
import ru.babobka.subtask.model.ValidationResult;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by dolgopolov.a on 15.12.15.
 */
public class PrimeCounterTask extends SubTask {

    private volatile ExecutorService threadPool;

    private static final String BEGIN = "begin";

    private static final String END = "end";

    public static final String PRIME_COUNT = "primeCount";

    private static final Long MIN_RANGE_TO_PARALLEL = 5000L;

    private final PrimeCounterReducer reducer = new PrimeCounterReducer();

    private final PrimeCounterDistributor distributor;

    private static final String NAME = "Dummy prime counter";

    private static final String DESCRIPTION = "Counts prime numbers in a given range";

    public PrimeCounterTask() {
	distributor = new PrimeCounterDistributor(NAME);
    }

    @Override
    protected void stopCurrentTask() {
	if (threadPool != null)
	    threadPool.shutdownNow();
    }

    @Override
    public ExecutionResult execute(NodeRequest request) {
	try {
	    if (!isStopped()) {
		Map<String, Serializable> result = new HashMap<>();
		long begin = Long.parseLong(request.getStringDataValue(BEGIN));
		long end = Long.parseLong(request.getStringDataValue(END));
		int cores = getCores(request);
		try {
		    synchronized (this) {
			if (!isStopped()) {
			    threadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
			}
		    }
		    int primes = countPrimes(threadPool, begin, end, cores);
		    result.put(PRIME_COUNT, primes);
		} catch (InterruptedException | ExecutionException e) {
		    if (!isStopped()) {
			e.printStackTrace();
		    }
		}
		return new ExecutionResult(isStopped(), result);
	    } else {
		return new ExecutionResult(isStopped(), null);
	    }
	} finally {
	    if (threadPool != null)
		threadPool.shutdownNow();

	}
    }

    private int getCores(NodeRequest request) {

	if (isRequestDataTooSmall(request)) {
	    return 1;
	} else {
	    return Runtime.getRuntime().availableProcessors();
	}

    }

    @Override
    public ValidationResult validateRequest(NodeRequest request) {

	if (request == null) {
	    return ValidationResult.fail("Request is empty");
	} else {
	    try {
		long begin = Long.parseLong(request.getStringDataValue(BEGIN));
		long end = Long.parseLong(request.getStringDataValue(END));
		if (begin < 0 || end < 0 || begin > end) {
		    return ValidationResult.fail("begin is more than end");
		}
	    } catch (NumberFormatException e) {
		e.printStackTrace();
		return ValidationResult.fail(e);
	    }
	}
	return ValidationResult.ok();
    }

    private int countPrimes(ExecutorService threadPool, long rangeBegin, long rangeEnd, int cores)
	    throws InterruptedException, ExecutionException {
	int result = 0;
	if (threadPool != null) {
	    Range[] ranges = MathUtil.getRangeArray(rangeBegin, rangeEnd, cores);
	    List<Future<Integer>> futureList = new ArrayList<>(ranges.length);
	    for (int i = 0; i < ranges.length; i++) {
		futureList.add(threadPool.submit(new PrimeCounterCallable(ranges[i])));
	    }
	    for (Future<Integer> future : futureList) {
		result += future.get();
	    }
	    if (isStopped()) {
		result = 0;
	    }
	}
	return result;

    }

    @Override
    public RequestDistributor getDistributor() {
	return distributor;
    }

    @Override
    public Reducer getReducer() {
	return reducer;
    }

    @Override
    public boolean isRequestDataTooSmall(NodeRequest request) {
	long begin = Long.parseLong(request.getStringDataValue(BEGIN));
	long end = Long.parseLong(request.getStringDataValue(END));
	if (end - begin > MIN_RANGE_TO_PARALLEL) {
	    return false;
	}
	return true;

    }

    public SubTask newInstance() {
	return new PrimeCounterTask();
    }

    @Override
    public String getDescription() {
	return DESCRIPTION;
    }

    @Override
    public String getName() {
	return NAME;
    }

    @Override
    public boolean isRaceStyle() {
	return false;
    }

}
