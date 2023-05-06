package pl.robakowski;

import io.activej.bytebuf.ByteBuf;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Warmup;
import pl.robakowski.transactions.TransactionsHandler;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Warmup(time = 1)
@Measurement(time = 5)
@Fork(4)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class TransactionsHandlerBenchmark {

    private static final byte[] bytes;

    static {
        try {
            bytes = TransactionsHandlerBenchmark.class.getClassLoader().getResourceAsStream("transactions_big_request.json").readAllBytes();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Benchmark
    public ByteBuf transactionHandler() throws Exception {
        return new TransactionsHandler().handle(new ByteArrayInputStream(bytes));
    }
}
