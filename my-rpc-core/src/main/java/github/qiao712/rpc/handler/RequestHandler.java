package github.qiao712.rpc.handler;

import github.qiao712.rpc.proto.RpcRequest;
import github.qiao712.rpc.proto.RpcResponse;

public interface RequestHandler{
    RpcResponse handleRequest(RpcRequest request);
}
