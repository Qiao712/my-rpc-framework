package github.qiao712.provider;

import github.qiao712.entity.RpcRequest;
import github.qiao712.entity.RpcResponse;

public interface RequestHandler{
    RpcResponse handleRequest(RpcRequest request);
}
