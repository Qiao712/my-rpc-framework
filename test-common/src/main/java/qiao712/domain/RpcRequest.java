package qiao712.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.lang.reflect.Method;

@Data
@AllArgsConstructor
public class RpcRequest implements Serializable {
    private Class<?> serviceType;
    private String methodName;
    private Object[] args;
}
