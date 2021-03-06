package ru.babobka.node.dlp.benchmark;

import ru.babobka.nodeclient.Client;
import ru.babobka.nodeserials.NodeRequest;
import ru.babobka.nodeserials.NodeResponse;
import ru.babobka.nodeserials.data.Data;
import ru.babobka.nodeserials.enumerations.ResponseStatus;
import ru.babobka.nodetester.benchmark.performer.ClientBenchmarkPerformer;
import ru.babobka.nodeutils.math.SafePrime;
import ru.babobka.nodeutils.time.Timer;
import ru.babobka.nodeutils.util.MathUtil;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by 123 on 01.02.2018.
 */
public class DlpNodeBenchmarkPerformer extends ClientBenchmarkPerformer {
    private static final Random RAND = new Random();
    private static final String TASK_NAME = "ru.babobka.dlp.task.regular.PollardDlpTask";
    private final SafePrime safePrime;
    private final BigInteger gen;

    public DlpNodeBenchmarkPerformer(int orderBitLength) {
        safePrime = SafePrime.random(orderBitLength - 1);
        gen = MathUtil.getGenerator(safePrime);
    }

    private BigInteger createNumber(BigInteger mod) {
        BigInteger number = BigInteger.valueOf(RAND.nextInt()).mod(mod);
        while (number.equals(BigInteger.ZERO)) {
            number = BigInteger.valueOf(RAND.nextInt()).mod(mod);
        }
        return number;
    }

    private static NodeRequest createDlpRequest(BigInteger x, BigInteger y, BigInteger mod) {
        Data data = new Data();
        data.put("x", x);
        data.put("y", y);
        data.put("mod", mod);
        return NodeRequest.regular(UUID.randomUUID(), TASK_NAME, data);
    }

    @Override
    protected void performBenchmark(Client client, AtomicLong timeStorage) throws IOException, ExecutionException, InterruptedException {
        NodeRequest request = createDlpRequest(gen, createNumber(safePrime.getPrime()), safePrime.getPrime());
        Future<NodeResponse> future = client.executeTask(request);
        Timer timer = new Timer();
        NodeResponse response = future.get();
        timeStorage.addAndGet(timer.getTimePassed());
        if (response.getStatus() != ResponseStatus.NORMAL) {
            String message = "cannot get the result. Response is " + response;
            throw new IOException(message);
        }
    }
}
