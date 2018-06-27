package services;

import java.util.HashMap;
import java.util.Map;

public final class ParameterService {
    private static Map<String, String> vzDictionary = new HashMap<>();

    /**
     * @param parameterName  Is the name of the parameter to be stored
     * @param parameterValue Is the value of the parameter to be stored
     */
    public static void setParameter(String parameterName, String parameterValue) {
        vzDictionary.put(parameterName, parameterValue);
    }

    /**
     * @param parameterName Is the name of the parameter to be retrieved
     * @return Returns the value of the parameter
     */
    public static String getParameter(String parameterName) {
        return vzDictionary.get(parameterName);
    }
}