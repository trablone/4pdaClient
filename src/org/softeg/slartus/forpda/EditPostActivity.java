package org.softeg.slartus.forpda;

/**
 * Created by IntelliJ IDEA.
 * User: Admin
 * Date: 20.10.11
 * Time: 19:24
 * To change this template use File | Settings | File Templates.
 */
public class EditPostActivity{


    public static void addStyleSheetLink(StringBuilder sb) {
        String cssFile = MyApp.INSTANCE. getThemeCssFileName();

        sb.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"file://" + cssFile + "\" />\n");
    }


    public static String getPostForm(Boolean enableSig,Boolean enableemo, Boolean isModerator) {
        return getPostForm(enableSig, enableemo,"", isModerator);
    }

    public static String getPostForm(Boolean enableSig,Boolean enableemo, String msg, Boolean isModerator) {
        String theme = MyApp.INSTANCE.getCurrentThemeName();
        return "<div class=\"quick_editor\">" +
                "<div class=\"post_header_editor\">Ответить</div>\n" +
                "<form name=\"f1\" action=\"\" method=\"post\" enctype=\"multipart/form-data\"><table border=0 align=\"center\">\n" +
                " <tr><td>\n" +
                "  <script type=\"text/javascript\">\n" +
                "   showIcons (" + isModerator + ",'" + theme + "'); \n" +
                "  </script><br />\n" +
                "  </td></tr>\n" +
                (isModerator ? ("<tr><td>" + getModeratorTags() + "</td></tr>") : "") +
                "  <tr><td width=\"100%\" id=\"txtinput\">\n" +
                "   <textarea name=\"Post\" id=\"Post\" class=\"button\" rows=\"8\" cols=\"44\" >" + msg.trim() +
                "</textarea></td></tr>" +
                "  <tr><td>" +
                "&nbsp;<img src=\"file:///android_asset/forum/style_images/1/folder_editor_buttons_" + theme + "/area_plus.png\" border=\"0\" onClick=\"javascript: areaPlus();\" />" +
                "&nbsp;<img src=\"file:///android_asset/forum/style_images/1/folder_editor_buttons_" + theme + "/area_minus.png\" border=\"0\" onClick=\"javascript: areaMinus();\" /><br />" +
                "</td></tr>" +
                "</table>" +
                "<center><input class=\"mod_chk\" type=\"checkbox\" name=\"enableemo\" id=\"enableemo\" " + (enableemo ? "checked=\"checked\"" : "") + " onClick=\"javascript: window.HTMLOUT.enableemo();\"/>&nbsp;Включить смайлики</center>\n" +
                "<center><input class=\"mod_chk\" type=\"checkbox\" name=\"enablesig\" id=\"enablesig\" " + (enableSig ? "checked=\"checked\"" : "") + " onClick=\"javascript: window.HTMLOUT.enablesig();\"/>&nbsp;Добавить подпись</center>\n" +
                "<div class=\"post_footer\"><center><input type=\"button\" value=\" Отправить \" onclick=\"preparePost()\" /></center></div>\n" +
                "<div class=\"post_footer\"><center><input type=\"button\" value=\" Расширенная форма \" onclick=\"advPost()\" /></center></div>\n" +
                "</form>" +
                "</div>";
    }

    private static String getModeratorTags() {
        return "<select size=\"1\" style=\"width:98%;\" onchange=\"insertText(this.value);\">\n" +
                "\n" +
                "\t\t\t\t\t<option value=\"\" selected=\"selected\">Модераторские теги</option>\n" +
                "\n" +
                "\t\t\t\t\t<option value=\"[EX] [IMG]http://4pda.ru/forum/html/emoticons/rtfm.gif[/IMG] [b][URL=http://4pda.ru/forum/index.php?act=boardrules]Правила Форума[/URL][/b][/EX]\">Правила форума.</option>\n" +
                "\n" +
                "\t\t\t\t\t<option value=\"[MOD] [IMG]http://4pda.ru/forum/style_images/1/atb_search.gif[/IMG] [b][URL=http://4pda.ru/forum/index.php?act=Search&amp;f=]Поиск[/URL][/b][/MOD]\">Поиск.</option>\n" +
                "\n" +
                "\t\t\t\t\t<option value=\"[EX][IMG]http://4pda.ru/forum/html/emoticons/rtfm.gif[/IMG] [b]Ознакомьтесь с [URL=http://4pda.ru/forum/index.php?act=boardrules]Правилами Форума![/URL][/b] [/EX]\">Ознакомьтесь с Правилами форума!</option>\n" +
                "\n" +
                "\t\t\t\t\t<option value=\"[MOD] [b]Воспользуйтесь [URL=http://4pda.ru/forum/index.php?act=Search&amp;f=][IMG]http://4pda.ru/forum/style_images/1/atb_search.gif[/IMG] поиском по форуму.[/URL][/b][/MOD]\">Воспользуйтесь поиском по форуму.</option>\n" +
                "\n" +
                "\t\t\t\t\t<option value=\"[MOD] [b]Воспользуйтесь [URL=http://4pda.ru/sr.php][IMG]http://4pda.ru/forum/style_images/1/atb_search.gif[/IMG] поиском по сайту.[/URL][/b][/MOD]\">Воспользуйтесь поиском по сайту.</option>\n" +
                "\n" +
                "\t\t\t\t\t<option value=\"[MOD]Тема закрыта.[/MOD]\">Тема закрыта.</option>\n" +
                "\n" +
                "\t\t\t\t\t<option value=\"[MOD]Тема перенесена в раздел «»[/MOD]\">Тема перенесена в раздел...</option>\n" +
                "\n" +
                "\t\t\t\t\t<option value=\"[EX]Прошу не флудить.[/EX]\">Прошу не флудить!</option>\n" +
                "\n" +
                "\t\t\t\t\t<option value=\"[EX][b][URL=http://4pda.ru/forum/index.php?act=announce&amp;f=212&amp;id=126][color=red]Разработчики - Warez которых запрещен на форуме.[/color][/URL][/b][/EX]\">Разработчики - Warez которых запрещен на форуме.</option>\n" +
                "\n" +
                "\t\t\t\t\t<option value=\"[EX]Внимательно читаем правила данного раздела![/EX]\">Внимательно читаем правила данного раздела!</option>\n" +
                "\n" +
                "\t\t\t\t\t<option value=\"[EX]Устное предупреждение![/EX]\">Устное предупреждение!</option>\n" +
                "\n" +
                "\t\t\t\t\t<option value=\"[EX]Здесь говорят на русском языке. 'Албанизмы' запрещены.[/EX]\">Здесь говорят на русском языке. 'Албанизмы' запрещены.</option>\n" +
                "\n" +
                "\t\t\t\t\t<option value=\"[EX]Запрещается подъем темы.[/EX]\">Запрещается подъем темы.</option>\n" +
                "\n" +
                "\t\t\t\t\t<option value=\"[EX]Запрещено поднимать объявления чаще чем один раз в 48 часов.[/EX]\">Запрещено поднимать объявления чаще чем один раз в 48 часов.</option>\n" +
                "\n" +
                "\t\t\t\t\t</select>";
    }

}
