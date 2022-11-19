package github.qiao712.rpc.registry;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 指向一个服务提供者的URL
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProviderURL {
    private InetSocketAddress address;
    private String service;
    private Integer weight = 0;

    private static Pattern pattern = Pattern.compile("(.*):(\\d*)");
    public static ProviderURL parseURL(String url){
        ProviderURL providerURL = new ProviderURL();

        //host:port
        Matcher matcher = pattern.matcher(url);
        if(matcher.find() && matcher.groupCount() == 2){
            String host = matcher.group(1);
            int port = Integer.parseInt(matcher.group(2));
            providerURL.address = new InetSocketAddress(host, port);
        }else{
            throw new IllegalArgumentException("URL格式错误");
        }

        //参数
        Map<String, String> params = new HashMap<>();
        int p = url.indexOf('?', matcher.end());
        String s = url.substring(p+1);
        if(s.length() != 0){
            String[] paramStrings = s.split("&");
            for (String paramString : paramStrings) {
                String[] kv = paramString.split("=");
                if(kv.length != 2) continue;
                params.put(kv[0].trim(), kv[1].trim());
            }
        }

        //weight
        String weight = params.get("weight");
        if(weight != null) providerURL.setWeight(Integer.parseInt(weight));
        //service
        providerURL.setService(params.get("service"));

        return providerURL;
    }

    @Override
    public String toString() {
        return address.getHostString() + ":" + address.getPort() + "?" + "service=" + service + "&weight=" + weight;
    }

    public void setWeight(Integer weight) {
        if(weight < 0){
            throw new IllegalArgumentException("weight < 0.");
        }
        this.weight = weight;
    }
}
