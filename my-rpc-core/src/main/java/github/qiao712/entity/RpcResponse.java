package github.qiao712.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@Data
@AllArgsConstructor
public class RpcResponse implements Serializable {
    private RpcResponseCode code;
    private Object data;

    public static RpcResponse ok(Object data){
        return new RpcResponse(RpcResponseCode.SUCCESS, data);
    }

    public static RpcResponse bad(RpcResponseCode code){
        return new RpcResponse(code, null);
    }

    public static RpcResponse bad(){
        return bad(RpcResponseCode.FAILURE);
    }
}
