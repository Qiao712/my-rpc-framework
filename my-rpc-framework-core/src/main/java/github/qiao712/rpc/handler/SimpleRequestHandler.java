package github.qiao712.rpc.handler;

import github.qiao712.rpc.proto.RpcRequest;
import github.qiao712.rpc.proto.RpcResponse;
import github.qiao712.rpc.proto.RpcResponseCode;
import github.qiao712.rpc.registry.ServiceProvider;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * 直接在读写线程中处理请求
 * 无处理业务的线程池
 */
@Slf4j
public class SimpleRequestHandler implements RequestHandler {
    private final ServiceProvider serviceProvider;

    public SimpleRequestHandler(ServiceProvider serviceProvider){
        this.serviceProvider = serviceProvider;
    }

    @Override
    public void handleRequest(RpcRequest request, ResponseSender responseSender) {
        Object service = serviceProvider.getServiceObject(request.getServiceName());

        if(service == null){
            log.debug("未找到服务:{}", request.getServiceName());
            responseSender.send(RpcResponse.fail(RpcResponseCode.SERVICE_NOT_FOUND));
            return;
        }

        RpcResponse response;
        try {
            //反射调用
            Method method = service.getClass().getMethod(request.getMethodName(), request.getParamTypes());
            Object returnValue = method.invoke(service, request.getParams());
            response = RpcResponse.succeed(returnValue);
        } catch (NoSuchMethodException e) {
            log.debug("未找到方法", e);
            response = RpcResponse.fail(RpcResponseCode.METHOD_NOT_FOUND);
        } catch (IllegalAccessException e) {
            log.debug("调用失败", e);
            response = RpcResponse.fail(RpcResponseCode.SERVER_ERROR);
        } catch (InvocationTargetException e) {
            //invoke的函数抛出异常
            log.debug("服务异常", e);
            //将服务函数抛出的异常作为数据返回
            response = RpcResponse.fail(e.getCause());
        }

        responseSender.send(response);
    }
}
