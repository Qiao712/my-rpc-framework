package github.qiao712.rpc.serializer;

import github.qiao712.rpc.exception.RpcSerializationException;

import java.io.*;

public class JDKSerializer implements Serializer{
    @Override
    public byte[] serialize(Object obj) {
        try(ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()){
            serialize(obj, byteArrayOutputStream);
            return byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            throw new RpcSerializationException("序列化失败", e);
        }
    }

    @Override
    public void serialize(Object obj, OutputStream outputStream) {
        try (ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream)){
            objectOutputStream.writeObject(obj);
        } catch (IOException e) {
            throw new RpcSerializationException("序列化失败", e);
        }
    }

    @Override
    public <T> T deserialize(byte[] data, Class<T> cls) {
        try(ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(data)){
            return deserialize(byteArrayInputStream, cls);
        } catch (IOException e) {
            throw new RpcSerializationException("反序列化失败", e);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T deserialize(InputStream inputStream, Class<T> cls) {
        try(ObjectInputStream objectInputStream = new ObjectInputStream(inputStream)){
            return (T) objectInputStream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new RpcSerializationException("反序列化失败", e);
        } catch (ClassCastException e){
            throw new RpcSerializationException("类型错误", e);
        }
    }
}
