package es.vargontoc.educational.framework.shared.security;

import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;

public class SanitizationText {
    
    public static String sanitaze(String input){
        return Jsoup.clean(input, Safelist.none());
    }
}
