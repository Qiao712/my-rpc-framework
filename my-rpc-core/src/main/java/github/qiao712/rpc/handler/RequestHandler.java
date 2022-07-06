package github.qiao712.rpc.handler;

import github.qiao712.rpc.proto.RpcRequest;
import github.qiao712.rpc.proto.RpcResponse;

/**
 * 服务端处理响应
 */
public interface RequestHandler{
    RpcResponse handleRequest(RpcRequest request);
}
