package pl.robakowski;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;
import pl.robakowski.transactions.Account;
import pl.robakowski.transactions.AccountMap;
import pl.robakowski.transactions.AccountNumber;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

@Warmup(time = 1)
@Measurement(time = 5)
@Fork(4)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class AccountMapBenchmark {

    private static final Random RAND = new SecureRandom();
    private static final AccountNumber[] numbers = IntStream.range(0, 200000).mapToObj($ -> new AccountNumber(l(), l())).toArray(AccountNumber[]::new);

    private static final ThreadLocal<AccountMap> thMap = ThreadLocal.withInitial(AccountMap::new);

    private static long l() {
        return RAND.nextLong(10000000000L);
    }

    @Benchmark
    public void testAccountMap4x(Blackhole blackhole) {
        AccountMap accountMap = thMap.get();
        accountMap.clear();
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < numbers.length / 4; j++) {
                AccountNumber number = numbers[j];
                blackhole.consume(accountMap.get(number).getBalance());
            }
        }
        blackhole.consume(accountMap.values().size());
    }

    @Benchmark
    public void testHashMap4x(Blackhole blackhole) {
        Map<AccountNumber, Account> map = new HashMap<>(135000);
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < numbers.length / 4; j++) {
                AccountNumber number = numbers[j];
                blackhole.consume(map.computeIfAbsent(number, Account::new).getBalance());
            }
        }
        ArrayList<Account> accounts = new ArrayList<>(map.values());
        accounts.sort(Comparator.comparing(Account::getAccount));
        blackhole.consume(accounts.size());
    }

    @Benchmark
    public void testAccountMap(Blackhole blackhole) {
        AccountMap accountMap = thMap.get();
        accountMap.clear();
        for (AccountNumber number : numbers) {
            blackhole.consume(accountMap.get(number).getBalance());
        }
        blackhole.consume(accountMap.values().size());
    }

    @Benchmark
    public void testHashMap(Blackhole blackhole) {
        Map<AccountNumber, Account> map = new HashMap<>(135000);
        for (AccountNumber number : numbers) {
            blackhole.consume(map.computeIfAbsent(number, Account::new).getBalance());
        }
        ArrayList<Account> accounts = new ArrayList<>(map.values());
        accounts.sort(Comparator.comparing(Account::getAccount));
        blackhole.consume(accounts.size());
    }
}
