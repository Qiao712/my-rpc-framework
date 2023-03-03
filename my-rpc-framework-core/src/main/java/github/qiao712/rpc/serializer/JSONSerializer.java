package github.qiao712.rpc.serializer;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.Feature;
import com.alibaba.fastjson.serializer.SerializerFeature;
import github.qiao712.rpc.exception.RpcException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class JSONSerializer implements Serializer{
    @Override
    public byte[] serialize(Object obj) {
        return JSON.toJSONBytes(obj, SerializerFeature.WriteClassName);
    }

    @Override
    public void serialize(Object obj, OutputStream outputStream) {
        try {
            JSON.writeJSONString(outputStream, obj, SerializerFeature.WriteClassName);
        } catch (IOException e) {
            throw new RpcException("序列化失败", e);
        }
    }

    @Override
    public <T> T deserialize(byte[] data, Class<T> cls) {
        return (T) JSON.parseObject(data, Feature.SupportAutoType);
    }

    @Override
    public <T> T deserialize(InputStream inputStream, Class<T> cls) {
        try {
            return JSON.parseObject(inputStream, cls, Feature.SupportAutoType);
        } catch (IOException e) {
            throw new RpcException("反序列化失败", e);
        }
    }
}
