package github.qiao712.rpc.handler;

import github.qiao712.rpc.proto.RpcRequest;
import github.qiao712.rpc.proto.RpcResponse;
import github.qiao712.rpc.proto.RpcResponseCode;
import github.qiao712.rpc.registry.ServiceRegistry;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * 同步地处理请求
 * 无处理业务的线程池
 */
@Slf4j
public class DefaultRequestHandler implements RequestHandler {
    private final ServiceRegistry serviceRegistry;

    public DefaultRequestHandler(ServiceRegistry serviceRegistry){
        this.serviceRegistry = serviceRegistry;
    }

    @Override
    public RpcResponse handleRequest(RpcRequest request) {
        Object service = serviceRegistry.getService(request.getServiceName());

        if(service == null){
            log.debug("未找到服务:{}", request.getServiceName());
            return RpcResponse.fail(RpcResponseCode.SERVICE_NOT_FOUND);
        }

        //获取方法
        Object[] arguments = request.getArgs();
        int argumentCount = arguments == null ? 0 : arguments.length;
        Class<?>[] argumentTypes = new Class<?>[argumentCount];
        for (int i = 0; i < argumentCount; i++) {
            argumentTypes[i] =arguments[i].getClass();
        }

        RpcResponse response;
        try {
            Method method = service.getClass().getMethod(request.getMethodName(), argumentTypes);

            //反射调用
            Object returnValue = method.invoke(service, arguments);
            response = RpcResponse.succeed(returnValue);
        } catch (NoSuchMethodException e) {
            log.debug("未找到方法", e);
            response = RpcResponse.fail(RpcResponseCode.METHOD_NOT_FOUND);
        } catch (IllegalAccessException e) {
            log.debug("调用失败", e);
            response = RpcResponse.fail();
        } catch (InvocationTargetException e) {
            //invoke的函数抛出异常
            log.debug("服务异常", e);
            response = RpcResponse.fail(RpcResponseCode.METHOD_THROWING);
        }

        return response;
    }
}
