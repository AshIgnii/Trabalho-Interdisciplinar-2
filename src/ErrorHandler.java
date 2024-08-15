import java.util.Arrays;

public class ErrorHandler {
    public static void handleError(Throwable e) {
        String msg = e.getMessage();
        String stack = Arrays.toString(e.getStackTrace());

        System.out.println("Error: " + msg + " at " + stack);
    }
}
