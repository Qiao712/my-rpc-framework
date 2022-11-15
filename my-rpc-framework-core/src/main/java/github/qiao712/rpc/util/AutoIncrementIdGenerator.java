package github.qiao712.rpc.util;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntUnaryOperator;

/**
 * 用于生成循环的自增ID (int类型)
 * 线程安全的
 */
public class AutoIncrementIdGenerator {
    private static class Holder{
        static AutoIncrementIdGenerator instance = new AutoIncrementIdGenerator();
    }

    private AutoIncrementIdGenerator(){}

    public static AutoIncrementIdGenerator getInstance(){
        return Holder.instance;
    }

    private final AtomicInteger id = new AtomicInteger();

    public int generateId(){
        return id.getAndUpdate(operand -> {
            ++operand;
            if(operand == Integer.MAX_VALUE){
                return 0;
            }else{
                return operand;
            }
        });
    }
}
