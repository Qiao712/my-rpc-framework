package github.qiao712.rpc.util;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntUnaryOperator;

/**
 * 用于生成循环的自增ID (int类型)
 * 线程安全的
 */
public class AutoIncrementIdGenerator {
    private final int begin;
    private final int end;
    private final AtomicInteger id = new AtomicInteger();


    public AutoIncrementIdGenerator() {
        this.begin = 0;
        this.end = Integer.MAX_VALUE;
    }

    public AutoIncrementIdGenerator(int begin, int end) {
        this.begin = begin;
        this.end = end;
    }

    public int generateId(){
        return id.getAndUpdate(operand -> operand + 1);
    }
}
