package github.qiao712.rpc.transport;

import github.qiao712.rpc.proto.RpcRequest;
import github.qiao712.rpc.proto.RpcResponse;
import github.qiao712.rpc.exception.RpcException;
import github.qiao712.rpc.proto.RpcResponseCode;
import github.qiao712.rpc.proto.SerializationType;

public abstract class AbstractRpcClient implements RpcClient {
    protected SerializationType serializationType = SerializationType.JDK_SERIALIZATION;        //所使用的序列化方式

    @Override
    public Object invoke(String serviceName, String methodName, Object[] args) {
        RpcRequest rpcRequest = new RpcRequest(serviceName, methodName, args);
        RpcResponse rpcResponse = request(rpcRequest);

        if(rpcResponse.getCode() != RpcResponseCode.SUCCESS){
            throw new RpcException("请求调用失败:" + rpcResponse.getCode());
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
}
