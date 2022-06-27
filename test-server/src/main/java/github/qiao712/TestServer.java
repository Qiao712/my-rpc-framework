package github.qiao712;

import github.qiao712.server.RpcServer;
import github.qiao712.service.TestServiceImpl;

import java.io.IOException;

public class TestServer {
    public static void main(String[] args) throws IOException {
        RpcServer rpcServer = new RpcServer(9712);
        rpcServer.register(new TestServiceImpl());
        rpcServer.start();
    }
}
