package github.qiao712.rpc.transport;

import github.qiao712.rpc.proto.RpcRequest;
import github.qiao712.rpc.proto.RpcResponse;
import github.qiao712.rpc.exception.RpcException;
import github.qiao712.rpc.proto.RpcResponseCode;
import github.qiao712.rpc.proto.SerializationType;
import io.netty.util.internal.ObjectUtil;

public abstract class AbstractRpcClient implements RpcClient {
    protected SerializationType serializationType = SerializationType.JDK_SERIALIZATION;        //所使用的序列化方式
    protected long responseTimeout = 0;      //响应的超时时间(ms). 为0表示一直等待.

    @Override
    public Object invoke(String serviceName, String methodName, Object[] args) {
        RpcRequest rpcRequest = new RpcRequest(serviceName, methodName, args);
        RpcResponse rpcResponse = request(rpcRequest);

        if(rpcResponse.getCode() != RpcResponseCode.SUCCESS){
            if (rpcResponse.getCode() == RpcResponseCode.METHOD_THROWING){
                //重新抛出服务提供者函数抛出的异常
                throw new RpcException("异常返回:" + rpcResponse.getCode(), (Throwable) rpcResponse.getData());
            }else{
                throw new RpcException("请求失败:" + rpcResponse.getCode());
            }
        }

        return rpcResponse.getData();
    }

    @Override
    public SerializationType getSerializationType() {
        return serializationType;
    }

    @Override
    public void setSerializationType(SerializationType serializationType) {
        this.serializationType = serializationType;
    }

    @Override
    public long getResponseTimeout() {
        return responseTimeout;
    }

    @Override
    public void setResponseTimeout(long responseTimeout) {
        ObjectUtil.checkPositiveOrZero(responseTimeout, "响应等待超时时间");
        this.responseTimeout = responseTimeout;
    }
}
