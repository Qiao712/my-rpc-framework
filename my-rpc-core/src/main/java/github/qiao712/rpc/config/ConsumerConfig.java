package github.qiao712.rpc.config;

import github.qiao712.rpc.proto.SerializationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ConsumerConfig {
    /**
     * 请求超时时间
     */
    private long responseTimeout;

    /**
     * 序列化方式
     */
    private SerializationType serializationType;
}
