package github.qiao712.rpc.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HashUtil {
    private static ThreadLocal<MessageDigest> localMessageDigest = new ThreadLocal<>();

    public static byte[] getMD5(byte[] data){
        MessageDigest messageDigest = getMessageDigest();
        return messageDigest.digest(data);
    }

    public static byte[] getMD5(String data){
        return getMD5(data.getBytes(StandardCharsets.UTF_8));
    }

    private static MessageDigest getMessageDigest(){
        MessageDigest messageDigest = localMessageDigest.get();
        if(messageDigest == null){
            try {
                messageDigest = MessageDigest.getInstance("MD5");
                localMessageDigest.set(messageDigest);
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }
        }

        return messageDigest;
    }
}
