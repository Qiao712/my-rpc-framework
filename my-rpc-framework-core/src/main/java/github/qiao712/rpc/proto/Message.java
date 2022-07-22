package github.qiao712.rpc.proto;

import github.qiao712.rpc.exception.RpcMessageCodecException;
import lombok.Data;

/**
 * 传输协议报文
 */
@Data
public class Message<T> {
    public static final int HEADER_LENGTH = 4*5;                //不包括有效负载的长度
    public static final int MAX_LENGTH = 1024 * 1024 * 1024;    //最大长度

    //header
    public static final int MAGIC_NUMBER = 0x712AB917;  //4bytes 标识协议类型
    private int length;                                 //4bytes 整个消息的长度
    private int requestId;                              //4bytes 请求id
    private MessageType messageType;                    //4bytes 消息的类型
    private SerializationType serializationType;        //4bytes 序列化方式

    //payload
    private T payload;                                  //有效负载数据

    /**
     * 检查字段合法性(不检查负载序列化后长度)
     * @throws RpcMessageCodecException 描述不合法原因
     */
    public void check(){
        //检查字段完整性
        if(messageType == null){
            throw new RpcMessageCodecException("消息类型未知");
        }
        if(serializationType == null){
            throw new RpcMessageCodecException("序列化方式未知");
        }
        if(payload == null){
            throw new RpcMessageCodecException("无有效负载");
        }
        if(payload.getClass() != messageType.getPayloadClass()){
            throw new RpcMessageCodecException("负载对象类型与消息类型字段不匹配");
        }
    }
}