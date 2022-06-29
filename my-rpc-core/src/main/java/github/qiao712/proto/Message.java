package github.qiao712.proto;

import github.qiao712.serializer.JDKSerializer;
import github.qiao712.serializer.Serializer;
import lombok.Data;

/**
 * 传输协议的消息
 */
@Data
public class Message<T> {
    public static final int HEADER_LENGTH = 4*4;                //不包括有效负载的长度
    public static final int MAX_LENGTH = 1024 * 1024 * 1024;    //最大长度

    public static final int MAGIC_NUMBER = 0x712AB917;  //4bytes 标识协议类型
    private int length;                                 //4bytes 整个消息的长度
    private MessageType messageType;                    //4bytes 消息的类型
    private SerializationType serializationType;        //4bytes 序列化方式
    private T payload;                                  //有效负载数据
}