* 端序
  * DataOutputStream 装饰器的 writeXXX() 以大端序输出(high byte first)
  * 网络字节序(Network Byte Order) -- 为大端序
* Magic Number 用于表示协议类型
* FilterOutputStream close() 会同时关闭其底层的流
  * `Closes this output stream and releases any system resources associated with the stream. The close method of FilterOutputStream calls its flush method, and then calls the close method of its underlying output stream.`
* 阿里规范: `定义时区分 unchecked / checked 异常，避免直接抛出 new RuntimeException()， 更不允许抛出 Exception 或者 Throwable，应使用有业务含义的自定义异常。推荐业界已定义过的自定义异常，如：DAOException / ServiceException 等。`
* BIO传输方式--消费者发送请求的--BUG--没有flush():
```java
try(BufferedOutputStream outputStream = new BufferedOutputStream(socket.getOutputStream());
    BufferedInputStream inputStream = new BufferedInputStream(socket.getInputStream())){
    //发送请求
    Message<RpcRequest> requestMessage = new Message<>();
    requestMessage.setMessageType(MessageType.RESPONSE);
    requestMessage.setSerializationType(SerializationType.JDK_SERIALIZATION);
    requestMessage.setPayload(rpcRequest);
    messageCoder.encodeMessage(requestMessage, outputStream);
    outputStream.flush();       //!!! 这里不刷新，可能不会写出，提供者接不到----一直等待。。。。

    //等待响应
    Message<Object> responseMessage = messageCoder.decodeMessage(inputStream);

    if(!(responseMessage.getPayload() instanceof RpcResponse)){
        throw new RpcException("响应格式错误");
    }

    rpcResponse = (RpcResponse) responseMessage.getPayload();
} catch (IOException e) {
    throw new RpcException("请求失败", e);
}
```