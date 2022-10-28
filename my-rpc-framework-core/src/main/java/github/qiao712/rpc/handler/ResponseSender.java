package github.qiao712.rpc.handler;

import github.qiao712.rpc.proto.RpcResponse;

/**
 * 回调函数
 * 用于传递给RequestHandler用于返回请求
 */
@FunctionalInterface
public interface ResponseSender {
    void send(RpcResponse rpcResponse);
}
