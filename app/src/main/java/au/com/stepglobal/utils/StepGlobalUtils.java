package au.com.stepglobal.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by hiten.bahri on 16/06/2017.
 */

public class StepGlobalUtils {


    public static boolean isValidUserId(String userId) {
        if(userId.length() <= 8) {
            String nameMatcher = "^[a-zA-Z0-9]*$";
            Pattern pattern = Pattern.compile(nameMatcher);
            Matcher matcher = pattern.matcher(userId);
            return matcher.matches();
        }
        return false;
    }

}
