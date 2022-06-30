package github.qiao712.consumer.client;

import github.qiao712.consumer.client.RpcClient;
import github.qiao712.proto.RpcRequest;
import github.qiao712.proto.RpcResponse;
import github.qiao712.exception.RpcException;
import github.qiao712.proto.RpcResponseCode;

public abstract class AbstractRpcClient implements RpcClient {
    @Override
    public Object invoke(String serviceName, String methodName, Object[] args) {
        RpcRequest rpcRequest = new RpcRequest(serviceName, methodName, args);
        RpcResponse rpcResponse = request(rpcRequest);

        if(rpcResponse.getCode() != RpcResponseCode.SUCCESS){
            throw new RpcException("请求调用失败:" + rpcResponse.getCode());
        }

        return rpcResponse.getData();
    }
}