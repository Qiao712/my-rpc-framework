package github.qiao712.rpc.registry.zookeeper;

import java.net.InetSocketAddress;
import java.util.List;

public class CuratorUtils {
    /**
     * 将一组地址拼接成一个字符串
     */
    public static String getAddressString(InetSocketAddress... addresses){
        StringBuilder stringBuilder = new StringBuilder();
        for (InetSocketAddress address : addresses) {
            stringBuilder.append(address.getHostString());
            stringBuilder.append(':');
            stringBuilder.append(address.getPort());
            stringBuilder.append(',');
        }
        stringBuilder.deleteCharAt(stringBuilder.length() - 1);
        return stringBuilder.toString();
    }

    /**
     * 将一组地址拼接成一个字符串
     */
    public static String getAddressString(String... addresses){
        StringBuilder stringBuilder = new StringBuilder();
        for (String address : addresses) {
            stringBuilder.append(address);
            stringBuilder.append(',');
        }
        stringBuilder.deleteCharAt(stringBuilder.length() - 1);
        return stringBuilder.toString();
    }
}
