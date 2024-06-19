package fr.insee.onyxia.api.services.utils;

import java.util.Base64;

public class Base64Utils {

    public static String base64Encode(String str) {
        return Base64.getEncoder().encodeToString(str.getBytes());
    }

    public static String base64Decode(String str) {
        return new String(Base64.getDecoder().decode(str));
    }
}
