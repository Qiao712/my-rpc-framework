### 当前实现:
* 序列化:
  * JDK序列化(所有的返回值、参数必须是Serializable)
  * Hessian序列化(所有的返回值、参数必须是Serializable)
* 传输: 
  * 传输方式: Socket BIO
  * 传输协议:
    * 负载为 RpcRequest, RpcResponse按指定序列化方式序列化结果
    * RpcRequest 携带接口、方法、参数信息
    * RpcResponse 携带状态码、返回值
    * 传输的消息的格式:
    
   |       | 说明                |
   |-------|--------------------|
   | 0-3   | magic number       |
   | 4-7   | 整个消息的长度        |
   | 8-11  | 消息类型(0: 请求, 1: 响应)|
   | 12-15 | 序列化方式           |
   | 16... | 负载                |
* Consumer发送调用请求:
  * 每次调用建立TCP长连接
  * JDK动态代理
* Provider接收并响应:
  * 使用一个线程池
  * 一个线程accept
  * 接收到请求，分配到一个线程进行处理，完成后关闭连接
* 服务注册:
  * 一个简单的服务名-对象Map
* 服务发现:
  * 未实现

#### 包结构
* github.qiao712.rpc
  * handler RequestHandler接口及其实现，处理请求调用目标方法
  * proto 定义通信协议
  * proxy 消费者一侧的代理相关
  * registry 服务注册
  * serializer 序列化方式
  * transport 各种实现的客户端(消费者发起请求)和服务器(提供者接收处理请求)
    * bio BIO实现
      * client
      * server
    * netty Netty实现
      * client
      * server
  * exception
  * util
  
### TODO:
* 优雅停机
* 粘包处理
* Netty通信

### 组成
* 最基本的远程调用
  * 序列化 反序列化
  * 代理
  * 网络传输
  * 服务提供者的线程池
* 负载均衡
* 容错
* 服务注册与发现
  * 可选注册中心:
    * Nacos
    * Zookeeper
    * Redis
* 使用注解发现并注册服务\客户代理