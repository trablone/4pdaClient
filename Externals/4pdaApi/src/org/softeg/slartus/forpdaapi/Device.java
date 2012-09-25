package org.softeg.slartus.forpdaapi;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * User: slinkin
 * Date: 25.09.12
 * Time: 9:05
 */
public class Device {
    public String name;
    public String url;
    
    public static Device parse(String deviceString){
        Device device=new Device();

        Matcher m= Pattern.compile("<a href=\"(.*?)\" target=\"_blank\">(.*?)</a>").matcher(deviceString);
        if(m.find()){
            device.name=m.group(2);
            device.url=m.group(1);
        }else{
            device.name="Нет";
        }

        return device;
    }
}
