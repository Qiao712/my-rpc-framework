package github.qiao712.rpc.handler;

import github.qiao712.rpc.registry.ServiceProvider;

public abstract class AbstractRequestHandler implements RequestHandler{
    private final ServiceProvider serviceProvider;

    protected AbstractRequestHandler(ServiceProvider serviceProvider){
        this.serviceProvider = serviceProvider;
    }
}
