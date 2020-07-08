import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

/**
 * @Author: 霁
 * @Date: 2020/7/8 0:10
 */
public class Test {
    public static void main(String[] args) {
        Result  result = JUnitCore.runClasses(TestLocalCache.class);
        for (Failure failure : result.getFailures()){
            System.out.println(failure.toString());
        }
        System.out.println("Test Result:"+ result.wasSuccessful());
        System.exit(0);
    }
}
