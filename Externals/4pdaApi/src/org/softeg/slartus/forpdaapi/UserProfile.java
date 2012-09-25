package org.softeg.slartus.forpdaapi;

import android.text.Html;
import android.text.Spanned;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * User: slinkin
 * Date: 25.09.12
 * Time: 8:45
 */
public class UserProfile {
    public Spanned sign;
    public String avatar;
    public String Group;
    public String personalPhoto;
    public String reputation;
    public String rating;
    
    //О Себе
    public String about;
    public String site;

    // личная информация
    public String login;
    public String status;
    public String age;
    public String gender;
    public String location;
    public String born;
    // интересы
    public String interests;
    //Другая информация
    public String devices;
    public String city;
    public Device device;
    // Статистика
    public String registration;
    public String profileViewsCount;
    public Spanned lastActivity;
    public String timeZone;
    public String messagesCount;
    // Контактная информация
    public String aim;
    public String yahoo;
    public String icq;
    public String msn;

    public String[] getMain(){
        return new String[]{Group,sign.toString()};
    }

    public String[] getAbout(){
        return new String[]{about,site};
    }

    public String[] getPrivateInfo(){
        return new String[]{login,status,age,gender,location,born};
    }

    public String[] getInterests(){
        return new String[]{interests};
    }

    public String[] getOtherInfo(){
        return new String[]{devices,city, "Устройство: "+device.name};
    }

    public String[] getStatistic(){
        return new String[]{registration,profileViewsCount, lastActivity.toString(),timeZone,messagesCount};
    }

    public String[] getContactInfo(){
        return new String[]{aim,yahoo, icq,msn};
    }
    
    public static UserProfile parsePage(String page) {
        UserProfile res = new UserProfile();
        Matcher tablesMatcher = Pattern.compile("<!-- LEFT TABLE -->([\\s\\S]*?)<!-- / LEFT TABLE -->[\\s\\S]*?<!-- MAIN TABLE -->([\\s\\S]*?)<!-- / MAIN TABLE -->").matcher(page);
        if (!tablesMatcher.find()) return res;
        parseLeftTable(res,tablesMatcher.group(1));
        parseMainTable(res,tablesMatcher.group(2));

        return res;
    }
    
    private static void parseLeftTable(UserProfile res, String table){
        Matcher groupMatcher = Pattern.compile("<!--(.*?)-->([\\s\\S]*?)<!--\\s*/\\s*(\\1)-->").matcher(table);
        Matcher m;
        while (groupMatcher.find()) {
            String group = groupMatcher.group(1).trim();
            String groupBody = groupMatcher.group(2).trim();
            if (group.equals("Personal Photo")) {
                res.personalPhoto = getValue("src='(.*?)'", groupBody);
            } else if (group.equals("Quick contact")) {
                res.reputation = getValue("title='Просмотреть репутацию'>(.*?)</a>", groupBody);
            } else if (group.equals("Rating")) {
                // пропускаем - не знаю, что это
            } else if (group.equals("Options")) {
                // пропускаем
            } else if (group.equals("Personal Statement")) {
                res.about = Html.fromHtml(getValue("id='pp-personal_statement'>([\\s\\S]*?)</?div", groupBody)).toString();
                res.site = getValue("href='(.*?)'", groupBody);
            } else if (group.equals("Personal Info")) {
                m = Pattern.compile("<div class='row1' style='padding:6px; margin-bottom:1px; padding-left:10px'>(.*?)</div>").matcher(groupBody);
                if(m.find()){
                    res.login=m.group(1);
                    if(m.find())
                        res.status=m.group(1);
                }
                res.age=getValue("<span id='pp-entry-age-text'>(\\d+)</span> <span id='pp-entry-age-yearsold'>лет</span>",groupBody);
                if(res.age.equals(""))
                    res.age="Возраст не указан";
                else
                    res.age+=" лет";
                res.gender=getValue("<span id='pp-entry-gender-text'>(.*?)</span>",groupBody);
                res.location=getValue("<span id='pp-entry-location-text'>(.*?)</span>",groupBody);
                res.born=getValue("<span id='pp-entry-born-pretext'>Дата рождения:</span> <span id='pp-entry-born-text'>(.*?)</span>",groupBody,"Дата рождения: ");
                if(res.born.equals(""))
                    res.born="День рождения не указан";

            } else if (group.equals("Interests")) {
                res.interests=Html.fromHtml(getValue("id='pp-personal_statement'>([\\s\\S]*?)</div>",groupBody).trim()).toString();
            } else if (group.equals("Custom Fields")) {
                res.devices=getValue("Описание ваших девайсов:(.*?)</div>",groupBody,"Описание ваших девайсов: ");
                res.city=getValue("Город где вы живёте:(.*?)</div>",groupBody,"Город где вы живёте: ");
                res.device=Device.parse(getValue("Устройство:(.*?)</div>",groupBody));
            } else if (group.equals("Statistics")) {
                m=Pattern.compile("<div class='row\\d' style='padding:6px; margin-bottom:1px; padding-left:10px'>([\\s\\S]*?)</div>").matcher(groupBody);
                int ind=0;
                while (m.find()){
                    switch (ind){
                        case 0:res.registration=m.group(1);break;
                        case 1:res.profileViewsCount=m.group(1);break;
                        case 2:res.lastActivity=Html.fromHtml( m.group(1));break;
                        case 3:res.timeZone=m.group(1);break;
                        case 4:res.messagesCount=m.group(1);break;
                    }
                    ind++;
                }
            } else if (group.equals("Contact Information")) {
                m=Pattern.compile("<span id='pp-entry-contact-entry-(.*?)'>(.*?)</span>").matcher(groupBody);
                while (m.find()){
                    String contactGroup=m.group(1);
                    if(contactGroup.equals("aim"))
                        res.aim="Вконтакте: "+ Html.fromHtml(m.group(2)).toString();
                    else if(contactGroup.equals("yahoo") )
                        res.yahoo= "Twitter: "+Html.fromHtml(m.group(2)).toString();
                    else if(contactGroup.equals("icq"))
                        res.icq= "ICQ: "+Html.fromHtml(m.group(2)).toString();
                    else if(contactGroup.equals("msn"))
                        res.msn= "Jabber: "+Html.fromHtml(m.group(2)).toString();
                }
            }
        }
    }

    private static void parseMainTable(UserProfile res, String table){
        res.avatar=getValue("(http://s.4pda.ru/forum//uploads/av-\\d+-\\d+.png)",table);
        res.sign= Html.fromHtml(getValue("<hr class=\"sfr\" />([\\s\\S]*?)</div>",table));
        if(res.sign.toString()=="")
            res.sign=Html.fromHtml("<i>Нет подписи</i>");
        res.Group=getValue("<strong><span.*?>(.*?)</span></strong>",table);
    }

    private static String getValue(String pattern, String input) {

        return getValue(pattern,input,"");
    }

    private static String getValue(String pattern, String input, String prefix) {
        Matcher m = Pattern.compile(pattern).matcher(input);
        if (m.find()){
            String res= m.group(1);
            if(res==null)
                res="";
            return prefix+res.trim();
        }
        return "";
    }

    public static UserProfile loadProfile(IHttpClient client, String userId) throws IOException {
        String page = client.performGet("http://4pda.ru/forum/index.php?showuser=" + userId);
        return parsePage(page);
    }
}
