package github.qiao712.test;

import github.qiao712.rpc.proto.RpcRequest;
import github.qiao712.rpc.proto.RpcResponse;
import github.qiao712.rpc.transport.RpcClient;
import github.qiao712.rpc.transport.netty.client.NettyRpcClient;
import org.junit.Test;

public class TestNettyImpl {
    @Test
    public void requestSend(){
        RpcClient rpcClient = new NettyRpcClient("127.0.0.1", 9712);
        RpcRequest rpcRequest = new RpcRequest("testService", "hello", new Object[0]);
        RpcResponse rpcResponse = rpcClient.request(rpcRequest);
        System.out.println(rpcResponse);
    }
}
