package github.qiao712.utils;

public class NameUtil {
    public static String firstLetterToUpperCase(String str){
        if(!str.isEmpty()){
            char firstLetter = str.charAt(0);
            if(Character.isLowerCase(firstLetter)){
                str = Character.toUpperCase(firstLetter) + str.substring(1);
            }
        }

        return str;
    }
}
