package org.softeg.slartus.forpda.classes;

/**
 * User: slinkin
 * Date: 22.11.11
 * Time: 13:11
 */
public final class ThemeOpenParams {
    public static final String FIRST_POST = "getfirstpost";
    public static final String LAST_POST = "getlastpost";
    public static final String NEW_POST = "getnewpost";
    public static final String BROWSER = "browser";

    public static String getUrlParams(String openParam, String defaultUrlParam) {
        if(openParam==null)return defaultUrlParam;
        if (openParam.equals(ThemeOpenParams.BROWSER))
            return "";
        if (openParam.equals(ThemeOpenParams.FIRST_POST))
            return "";
        if (openParam.equals(ThemeOpenParams.LAST_POST))
            return "view=getlastpost";
        if (openParam.equals(ThemeOpenParams.NEW_POST))
            return "view=getnewpost";

        return defaultUrlParam;

    }
}
