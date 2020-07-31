package transaction;

/**
 * @Author Louhwz
 * @Date 2020/07/31
 * @Time 16:25
 */
public class Utils {
    public static String genrConSyntax(String rmiPort) {
        if(rmiPort == null)
            return "";
        else
            return "//:" + rmiPort + "/";
    }
}
