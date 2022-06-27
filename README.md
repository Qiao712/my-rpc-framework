### 简单的demo:
* 序列化: Java序列化
* 传输: Socket BIO
* Client
  * 每次调用建立连接
  * JDK动态代理
* Server
  * 使用线程池
  * 一个连接监听
  * 一个线程accept
  * 接收到请求，分配到一个线程进行处理，完成后关闭连接
* RpcRequest 携带接口、方法、参数信息
* RpcResponse 携带状态码、返回值

### TODO:
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