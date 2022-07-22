package github.qiao712.rpc.proto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RpcResponse implements Serializable {
    private RpcResponseCode code;
    private Object data;

    public static RpcResponse succeed(Object data){
        return new RpcResponse(RpcResponseCode.SUCCESS, data);
    }

    public static RpcResponse fail(RpcResponseCode code){
        return new RpcResponse(code, null);
    }

    public static RpcResponse fail(Throwable e) {
        return new RpcResponse(RpcResponseCode.METHOD_THROWING, e);
    }
}
