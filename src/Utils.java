import java.text.Normalizer;
import java.util.regex.Pattern;

public class Utils {
    public static boolean equalsInsensitive(String s1, String s2) {
        if (s1 == null || s2 == null) {
            return s1 == s2;
        }

        String normalizedS1 = normalizeAcento(s1);
        String normalizedS2 = normalizeAcento(s2);

        return normalizedS1.equalsIgnoreCase(normalizedS2);
    }

    public static String normalizeAcento(String s) {
        String normalized = Normalizer.normalize(s, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(normalized).replaceAll("");
    }
}
