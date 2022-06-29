package github.qiao712.provider;

import github.qiao712.proto.RpcRequest;
import github.qiao712.proto.RpcResponse;

public interface RequestHandler{
    RpcResponse handleRequest(RpcRequest request);
}
