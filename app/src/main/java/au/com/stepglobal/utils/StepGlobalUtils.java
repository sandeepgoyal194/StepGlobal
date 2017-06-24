package au.com.stepglobal.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by hiten.bahri on 24/06/2017.
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

    public static String getDateInFormat(long startTime) {
        String dateString = new SimpleDateFormat("HH:mm:ss").format(new Date(startTime));
        return dateString;
    }

    public static String getTripReasonCode(String tripReason) {
        String code = null;
        switch(tripReason) {
            case "Business Meeting":
                code = "BM";
                break;
            case "Site Inspection":
                code = "SI";
                break;
            case "Business To Office":
                code = "BO";
                break;
            case "To Depot":
                code = "DR";
        }
        return code;
    }
}
