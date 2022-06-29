package github.qiao712.proto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@Data
@AllArgsConstructor
public class RpcRequest implements Serializable {
    private String serviceName;
    private String methodName;
    private Object[] args;
}
