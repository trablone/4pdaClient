package org.softeg.slartus.forpdaapi;

import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * User: slinkin
 * Date: 25.09.12
 * Time: 8:45
 */
public class UserProfile {
    public Spanned info;
    public Spanned sign;
    public String avatar;
    public String Group;
    public String personalPhoto;
    public String reputation;
    public String rating;
    public Users UserViews = new Users();
    public Comments UserComments = new Comments();
    public Users Friends = new Users();
    //О Себе
    public String about;
    public String getAbout(){
        if(TextUtils.isEmpty(about))
            return login+" не указал(а) ничего о себе.";
        return about;
    }
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
    public Spanned devices;
    public Spanned city;
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

    public String[] getMain() {
//        ArrayList<String> res= new ArrayList<String>();
//        if()
        return new String[]{Group, sign==null?"":sign.toString()};
    }
    
    public static String[] getGroupData(String[] fullGroupData){
        ArrayList<String> res=new ArrayList<String>();
        for(String str: fullGroupData){
            if(TextUtils.isEmpty(str))continue;
            res.add(str);
        }
        String[] resArray=new String[res.size()];
        return res.toArray(resArray);
    }

    public String[] getAboutGroup() {
        return new String[]{getAbout()+(site==null?"":("\n"+ site)),info==null?"":info.toString()};
    }

    public String[] getPrivateInfo() {
        return new String[]{status, age, gender, location, born};
    }

    public String[] getInterests() {
        return new String[]{interests};
    }

    public String[] getOtherInfo() {
        return new String[]{devices==null?"":devices.toString(), city==null?"":city.toString(), device==null?"":("Устройство: " + device.name)};
    }

    public String[] getStatistic() {
        return new String[]{registration, profileViewsCount, lastActivity==null?"":lastActivity.toString(), timeZone, messagesCount};
    }

    public String[] getContactInfo() {
        return new String[]{aim, yahoo, icq, msn};
    }

    public void parsePage(String page) {
//        Matcher tablesMatcher = Pattern.compile("<!-- LEFT TABLE -->([\\s\\S]*?)<!-- / LEFT TABLE -->[\\s\\S]*?<!-- MAIN TABLE -->([\\s\\S]*?)<!-- / MAIN TABLE -->[\\s\\S]*?<!-- RIGHT TABLE -->([\\s\\S]*?)<!-- / RIGHT TABLE -->").matcher(page);
//        if (!tablesMatcher.find()) return;
        parseLeftTable(this, getValue("<!-- LEFT TABLE -->([\\s\\S]*?)<!-- / LEFT TABLE -->",page));
        parseMainTable(this, getValue("<!-- MAIN TABLE -->([\\s\\S]*?)<!-- / MAIN TABLE -->", page));
        parseRightTable(this, getValue("<!-- RIGHT TABLE -->([\\s\\S]*?)<!-- / RIGHT TABLE -->", page));
    }

    private static void parseLeftTable(UserProfile res, String table) {
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
                if (m.find()) {
                    res.login = m.group(1);
                    if (m.find())
                        res.status = m.group(1);
                }
                res.age = getValue("<span id='pp-entry-age-text'>(\\d+)</span> <span id='pp-entry-age-yearsold'>лет</span>", groupBody);
                if (res.age.equals(""))
                    res.age = "Возраст не указан";
                else
                    res.age += " лет";
                res.gender = getValue("<span id='pp-entry-gender-text'>(.*?)</span>", groupBody);
                res.location = getValue("<span id='pp-entry-location-text'>(.*?)</span>", groupBody);
                res.born = getValue("<span id='pp-entry-born-pretext'>Дата рождения:</span> <span id='pp-entry-born-text'>(.*?)</span>", groupBody, "Дата рождения: ");
                if (res.born.equals(""))
                    res.born = "День рождения не указан";

            } else if (group.equals("Interests")) {
                res.interests = Html.fromHtml(getValue("id='pp-personal_statement'>([\\s\\S]*?)</div>", groupBody).trim()).toString();
            } else if (group.equals("Custom Fields")) {
                res.devices = Html.fromHtml(getValue("Описание ваших девайсов:(.*?)</div>", groupBody, "Описание ваших девайсов: "));
                res.city = Html.fromHtml(getValue("Город где вы живёте:(.*?)</div>", groupBody, "Город где вы живёте: "));
                res.device = Device.parse(getValue("Устройство:(.*?)</div>", groupBody));
            } else if (group.equals("Statistics")) {
                m = Pattern.compile("<div class='row\\d' style='padding:6px; margin-bottom:1px; padding-left:10px'>([\\s\\S]*?)</div>").matcher(groupBody);
                int ind = 0;
                while (m.find()) {
                    switch (ind) {
                        case 0:
                            res.registration = m.group(1);
                            break;
                        case 1:
                            res.profileViewsCount = m.group(1);
                            break;
                        case 2:
                            res.lastActivity = Html.fromHtml(m.group(1));
                            break;
                        case 3:
                            res.timeZone = m.group(1);
                            break;
                        case 4:
                            res.messagesCount = m.group(1);
                            break;
                    }
                    ind++;
                }
            } else if (group.equals("Contact Information")) {
                m = Pattern.compile("<span id='pp-entry-contact-entry-(.*?)'>(.*?)</span>").matcher(groupBody);
                while (m.find()) {
                    String contactGroup = m.group(1);
                    if (contactGroup.equals("aim"))
                        res.aim = "Вконтакте: " + Html.fromHtml(m.group(2)).toString();
                    else if (contactGroup.equals("yahoo"))
                        res.yahoo = "Twitter: " + Html.fromHtml(m.group(2)).toString();
                    else if (contactGroup.equals("icq"))
                        res.icq = "ICQ: " + Html.fromHtml(m.group(2)).toString();
                    else if (contactGroup.equals("msn"))
                        res.msn = "Jabber: " + Html.fromHtml(m.group(2)).toString();
                }
            }
        }
    }

    private static void parseMainTable(UserProfile res, String table) {
        res.avatar = getValue("'(http://s.4pda.ru/forum//uploads/av-.*?)'", table);
        res.info=Html.fromHtml(getValue("<div class='pp-contentbox-entry-noheight'>([\\s\\S]*?)<hr class=\"sfr\" />", table));
        res.sign = Html.fromHtml(getValue("<hr class=\"sfr\" />([\\s\\S]*?)</div>", table));
        if (res.sign.toString() == "")
            res.sign = Html.fromHtml("<i>Нет подписи</i>");
        res.Group = getValue("<strong><span.*?>(.*?)</span></strong>", table);
    }

    private static void parseRightTable(UserProfile res, String table) {
        Matcher groupMatcher = Pattern.compile("<!--(.*?)-->([\\s\\S]*?)<!--\\s*/\\s*(\\1)-->").matcher(table);
        Matcher m;
        while (groupMatcher.find()) {
            String group = groupMatcher.group(1).trim();
            String groupBody = groupMatcher.group(2).trim();
            if (group.equals("Recent Visitors")) {
                m = Pattern.compile("href='http://4pda.ru/forum/index.php\\?.*?showuser=(\\d+)'.*?>(.*?)</a></strong>[\\s\\S]*?<font color='.*?'>(.*?)</font>\\s*(.*?)</div>").matcher(groupBody);
                while (m.find()) {
                    User user = new User();
                    user.setMid(m.group(1));
                    user.setNick(m.group(2));
                    user.State = m.group(3);
                    user.LastVisit = m.group(4);
                    res.UserViews.add(user);
                }

            } else if (group.equals("Comments")) {
                m = Pattern.compile("<div class='pp-mini-content-entry-noheight' id='pp-comment-entry-(\\d+)'>[\\s\\S]*?<a href='http://4pda.ru/forum/index.php\\?.*?showuser=(\\d+)'>(.*?)</a></strong>\\s*<br />\\s*([\\s\\S]*?)<br />[\\s\\S]*?<strong>([\\s\\S]*?)</strong>").matcher(groupBody);
                while (m.find()) {
                    Comment item = new Comment();
                    item.Id = m.group(1);
                    item.UserId = m.group(2);
                    item.User = m.group(3);
                    item.Text = Html.fromHtml(m.group(4).trim()).toString().trim();
                    item.DateTime = m.group(5);
                    res.UserComments.add(item);
                }

            } else if (group.equals("Friends")) {
                m = Pattern.compile("<a href='http://4pda.ru/forum/index.php\\?.*?showuser=(\\d+)'>(.*?)</a>[\\s\\S]*?(\\d+) сообщений\\s*<br />\\s*<font color='.*?'>(.*?)</font>([\\s\\S]*?)</div>").matcher(groupBody);
                while (m.find()) {
                    User item = new User();
                    item.setMid(m.group(1));
                    item.setNick(m.group(2));
                    item.MessagesCount = m.group(3);
                    item.State = m.group(4);
                    item.LastVisit = Html.fromHtml(m.group(5)).toString().trim();
                    res.Friends.add(item);
                }

            }
        }
    }

    private void parseFriendsPage(String page) {
        Matcher m = Pattern.compile("href='http://4pda.ru/forum/index.php\\?.*?showuser=(\\d+)'.*?>(.*?)</a></strong>\\s*<div class='pp-tiny-text'>\\s*(.*?)\\s*<br />(\\d+) сообщений\\s*<br />Последнее действие: (.*?)\\s*</div>")
                .matcher(page);

        while (m.find()) {
            User item = new User();
            item.setMid(m.group(1));
            item.setNick(m.group(2));
            item.Group = m.group(3);
            item.MessagesCount = m.group(4);
            item.LastVisit = Html.fromHtml(m.group(5)).toString();
            Friends.add(item);

        }

        // на одну страницу помещается 10 друзей. поэтому, только если больше 9 есть смысл искать страницы
        if (Friends.size() > 9) {
            m = Pattern.compile("st=(\\d+)").matcher(page);
            int max=0;
            while (m.find()) {

                max=Math.max(Integer.parseInt(m.group(1)),max);

            }
            Friends.setFullLength(max);
        }
    }

    private void parseCommentsPage(String page) {
        Matcher m = Pattern.compile("<a href='http://4pda.ru/forum/index.php\\?.*?showuser=(\\d+)'.*?>(.*?)</a></strong>\\s*<br />\\s*([\\s\\S]*?)\\s*<br />\\s*<strong>(.*?)</strong>")
                .matcher(page);


        while (m.find()) {
            Comment item = new Comment();
            item.UserId = m.group(1);
            item.User = m.group(2);
            item.Text = Html.fromHtml(m.group(3)).toString().trim();
            item.DateTime = m.group(4);

            UserComments.add(item);
        }

        // на одну страницу помещается 10 комментов. поэтому, только если больше 9 есть смысл искать страницы
        if (UserComments.size() > 9) {
            m = Pattern.compile("st=(\\d+)").matcher(page);
            int max=0;
            while (m.find()) {

                max=Math.max(Integer.parseInt(m.group(1)),max);

            }
            UserComments.setFullLength(max);
        }

    }


    private static String getValue(String pattern, String input) {

        return getValue(pattern, input, "");
    }

    private static String getValue(String pattern, String input, String prefix) {
        Matcher m = Pattern.compile(pattern).matcher(input);
        if (m.find()) {
            String res = m.group(1);
            if (res == null)
                res = "";
            return prefix + res.trim();
        }
        return "";
    }

    public static UserProfile loadProfile(IHttpClient client, UserProfile userProfile, String userId) throws IOException {
        if (userProfile == null)
            userProfile = new UserProfile();
        if (TextUtils.isEmpty(userProfile.registration)) {
            String page = client.performGet("http://4pda.ru/forum/index.php?showuser=" + userId);
            userProfile.parsePage(page);
        }


        return userProfile;
    }

    public static UserProfile loadProfileFriends(IHttpClient client, UserProfile userProfile, String userId, String md5)
            throws IOException {
        if (userProfile == null)
            userProfile = new UserProfile();
        if (userProfile.Friends.size()<10)
            userProfile.Friends.clear();
        String page = client.performGet("http://4pda.ru/forum/index.php?s=&act=profile&CODE=personal_iframe_friends&member_id="
                + userId + "&md5check=" + md5+ "&st=" + userProfile.Friends.size());
        userProfile.parseFriendsPage(page);


        return userProfile;
    }

    public static UserProfile loadUserProfileComments(IHttpClient client, UserProfile userProfile, String userId, String md5)
            throws IOException {
        if (userProfile == null)
            userProfile = new UserProfile();
        if (userProfile.UserComments.size()<10)
            userProfile.UserComments.clear();
        String page = client.performGet("http://4pda.ru/forum/index.php?s=&act=profile&CODE=personal_iframe_comments&member_id="
                + userId + "&md5check=" + md5 + "&st=" + userProfile.UserComments.size());
        userProfile.parseCommentsPage(page);


        return userProfile;
    }

}
