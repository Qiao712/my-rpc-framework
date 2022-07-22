package github.qiao712.rpc.serializer;

import com.caucho.hessian.io.Hessian2Input;
import com.caucho.hessian.io.Hessian2Output;
import github.qiao712.rpc.exception.RpcSerializationException;

import java.io.*;

public class HessianSerializer implements Serializer{
    @Override
    public byte[] serialize(Object obj) {
        try(ByteArrayOutputStream outputStream = new ByteArrayOutputStream()){
            serialize(obj, outputStream);
            return outputStream.toByteArray();
        } catch (IOException e) {
            throw new RpcSerializationException("序列化失败", e);
        }
    }

    @Override
    public void serialize(Object obj, OutputStream outputStream) {
        try {
            Hessian2Output hessian2Output = new Hessian2Output(outputStream);
            hessian2Output.writeObject(obj);
            hessian2Output.flush();
        } catch (IOException e) {
            throw new RpcSerializationException("序列化失败", e);
        }
    }

    @Override
    public <T> T deserialize(byte[] data, Class<T> cls) {
        try(ByteArrayInputStream inputStream = new ByteArrayInputStream(data)){
            return deserialize(inputStream, cls);
        } catch (IOException e) {
            throw new RpcSerializationException("反序列化失败", e);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T deserialize(InputStream inputStream, Class<T> cls) {
        try {
            Hessian2Input hessian2Input = new Hessian2Input(inputStream);
            return (T) hessian2Input.readObject(cls);
        } catch (IOException e) {
            throw new RpcSerializationException("反序列化失败", e);
        }
    }
}
