package org.softeg.slartus.forpda.Tabs;

import android.content.Context;
import android.text.Html;
import org.softeg.slartus.forpda.Client;
import org.softeg.slartus.forpda.classes.Forum;
import org.softeg.slartus.forpda.classes.Topic;
import org.softeg.slartus.forpdaapi.OnProgressChangedListener;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * User: slinkin
 * Date: 27.10.11
 * Time: 8:31
 */
public class DigestTab extends TreeTab {
    private final String APP_CATALOG_ID="127361";
    private final String GAME_CATALOG_ID="381335";
    public String getTitle() {
        return "Дайджест";
    }

    public String getTemplate() {
        return Tabs.TAB_FORUMS;
    }


    public DigestTab(Context context, String tabTag) {
        super(context, tabTag);
    }

    @Override
    protected void loadForum(Forum forum, OnProgressChangedListener progressChangedListener) throws Exception {

        loadDigest(forum, progressChangedListener);

    }

    @Override
    protected void showThemes(Forum forum) {

        m_ForumForLoadThemes = forum;
        m_Themes.clear();
        if (m_ForumForLoadThemes.getParent() != null && m_ForumForLoadThemes.getParent().getId().equals(m_ForumForLoadThemes.getId()))
            m_ForumForLoadThemes.getParent().getAllThemes(m_Themes);
        else
            m_ForumForLoadThemes.getAllThemes(m_Themes);
        Collections.sort(m_Themes, new Comparator<Topic>() {
            public int compare(Topic topic, Topic topic1) {
                return topic.getTitle().toString().toUpperCase().compareTo(topic1.getTitle().toString().toUpperCase());
            }
        });
        loadLatest();

    }


    @Override
    protected void getThemes(OnProgressChangedListener progressChangedListener) throws IOException {

        if (m_ForumForLoadThemes.getTag().equals("app")) {
            if (m_ForumForLoadThemes.getParent().getId().equals(m_ForumForLoadThemes.getId()))
                getAppDigestCategoryThemes(m_ForumForLoadThemes, m_ForumForLoadThemes.getParent().getParent().getTitle(), m_ForumForLoadThemes.getParent().getTitle(), progressChangedListener);
            else
                getAppDigestSubCategoryThemes(m_ForumForLoadThemes, m_ForumForLoadThemes.getParent().getParent().getTitle(), m_ForumForLoadThemes.getParent().getTitle(), progressChangedListener);
        } else {
            getGameDigestCategoryThemes(m_ForumForLoadThemes, m_ForumForLoadThemes.getParent().getTitle(), progressChangedListener);
        }
        m_Themes = m_ForumForLoadThemes.getThemes();
    }


    public void loadDigest(final Forum digest, OnProgressChangedListener progressChangedListener) throws Exception {
        digest.clearChildren();
        Exception appException = null;
        try {
            Client.INSTANCE.doOnOnProgressChanged(progressChangedListener, "Получение данных...");
            String body = Client.INSTANCE.performGet("http://4pda.ru/forum/index.php?showtopic="+APP_CATALOG_ID);
            Client.INSTANCE.doOnOnProgressChanged(progressChangedListener, "Обработка данных...");
            Client.INSTANCE.checkLogin(body);
            Forum appsDigestForum = new Forum(Integer.toString(127361), "Программы");
            getDigest(appsDigestForum, body, "app");
            digest.addForum(appsDigestForum);
        } catch (Exception ex) {
            appException = ex;
        }

        try {
            Client.INSTANCE.doOnOnProgressChanged(progressChangedListener, "Получение данных...");
            String bodyGames = Client.INSTANCE.performGet("http://4pda.ru/forum/index.php?showtopic="+GAME_CATALOG_ID);
            Client.INSTANCE.doOnOnProgressChanged(progressChangedListener, "Обработка данных...");
            Forum gamesDigestForum = new Forum(Integer.toString(131725), "Игры");
            getDigest(gamesDigestForum, bodyGames, "game");
            digest.addForum(gamesDigestForum);
        } catch (Exception ex) {
            throw new Exception("Дайджест игр: "+ ex.getMessage(), ex);
        }
        if(appException!=null)
            throw new Exception("Дайджест программ: "+ appException.getMessage(), appException);
    }

    private void getDigest(final Forum digest, String body, String tag) {
        Boolean app = tag.equals("app");
        final Pattern digestPieceOfPiecePattern = Pattern.compile("<!--coloro:coral--><span style=\"color:coral\"><!--/coloro--><b>(.*?)</b>");
        final Pattern digestPiecePattern = Pattern.compile("<b><!--coloro:royalblue--><span style=\"color:royalblue\"><!--/coloro-->(.*)?:<!--colorc--></span><!--/colorc--></b></div>");
        final Pattern digestPieceGamePattern = Pattern.compile("<span style=\"color:.*?\"><!--/coloro-->((Обновление|Новые).*?)<!--colorc--></span>");
        final Pattern datePattern = Pattern.compile("<span style=\"color:royalblue\"><!--/coloro--><b><!--sizeo:4--><span style=\"font-size:14pt;line-height:100%\"><!--/sizeo-->(.*?)<!--sizec--></span>");

        final String[] messages = body.split("<!--Begin Msg Number");
        body = null;

        final Forum parentForum = digest.setTag(tag);


        final int[] msgId = {messages.length};

        final int[] fId = {0};

        while (true) {
            String msg;


            msgId[0]--;

            if (msgId[0] < 0) return;
            msg = messages[msgId[0]];

            Matcher m = datePattern.matcher(msg);// дата дайджеста
            if (!m.find()) continue;

            Forum forum = new Forum(Integer.toString(fId[0]++), m.group(1));
            forum.setTag(tag);
            // forum.addForum(new Forum(forum.getId(), forum.getTitle() + " @ темы").setTag(tag));
            parentForum.addForum(forum);

            String newOldDelimiter = "<b><!--coloro:.*?-->";
            if (app)
                newOldDelimiter = "<div align='CENTER'>";
            String[] digestPieces = msg.split(newOldDelimiter);
            for (String digestPiece : digestPieces) {
                if (app)
                    m = digestPiecePattern.matcher(digestPiece);
                else
                    m = digestPieceGamePattern.matcher(digestPiece);
                if (!m.find()) continue;

                Forum piece = new Forum(Integer.toString(fId[0]++), m.group(1)).setTag(tag);


                if (app) {
                    piece.addForum(new Forum(piece.getId(), piece.getTitle() + " @ темы").setTag(tag));
                    String[] digestPiecesOfPiece = digestPiece.split("<ol type='1'>");// разбиваем на категории
                    for (String digestPieceOfPiece : digestPiecesOfPiece) {
                        m = digestPieceOfPiecePattern.matcher(digestPieceOfPiece);
                        while (m.find()) {

                            Forum pieceOfPiece = new Forum(Integer.toString(fId[0]++), m.group(1)).setTag(tag);


                            piece.addForum(pieceOfPiece);
                        }
                    }
                }

                forum.addForum(piece);
            }
        }


    }

    private String getAppDigestMsgSubCategory(String dateTitle, String newOldTitle, OnProgressChangedListener progressChangedListener) throws IOException {

        String body = Client.INSTANCE.loadPageAndCheckLogin("http://4pda.ru/forum/index.php?showtopic="+APP_CATALOG_ID, progressChangedListener);

        Pattern msgPattern = Pattern.compile("<div class=\"post_body\"><div align='center'><!--coloro:royalblue--><span style=\"color:royalblue\"><!--/coloro--><b><!--sizeo:4--><span style=\"font-size:14pt;line-height:100%\"><!--/sizeo-->" + Pattern.quote(dateTitle) + "<([\\s\\S]*?)((<!--Begin Msg Number)|(<!-- TABLE FOOTER -->))");
        final Pattern digestPiecePattern = Pattern.compile("<b><!--coloro:royalblue--><span style=\"color:royalblue\"><!--/coloro-->" + Pattern.quote(newOldTitle) + ":<([\\s\\S]*?)((<div align='CENTER'><b><!--coloro:royalblue--><span style=\"color:royalblue\"><!--/coloro-->)|(</div>\\Z))");

        Matcher m = msgPattern.matcher(body);
        if (!m.find()) return null;
        String msg = m.group(1);


        m = digestPiecePattern.matcher(msg);
        if (!m.find()) return null;


        return m.group(1);
    }

    // новые/обновления
    public void getAppDigestCategoryThemes(final Forum category, String dateTitle, String newOldTitle, OnProgressChangedListener progressChangedListener) throws IOException {
        String msgSubCategory = getAppDigestMsgSubCategory(dateTitle, newOldTitle, progressChangedListener);
        if (msgSubCategory == null) return;

        final Pattern digestPieceOfPiecePattern = Pattern.compile("<!--coloro:coral--><span style=\"color:coral\"><!--/coloro--><b>(.*?)</b>");
        final Pattern themePattern = Pattern.compile("<li>(.*?<a href=\"http://4pda.ru/forum/index.php\\?showtopic=(\\d+)\"(.*?))</li>");
        final Pattern prPattern = Pattern.compile("(.*?)\\[(.*?)\\](.*?)$");
        String[] digestPiecesOfPiece = msgSubCategory.split("<!--coloro:coral--><span style=\"color:coral\"><!--/coloro--><b>");// разбиваем на категории
        for (String digestPieceOfPiece : digestPiecesOfPiece) {
            Matcher m = digestPieceOfPiecePattern.matcher("<!--coloro:coral--><span style=\"color:coral\"><!--/coloro--><b>" + digestPieceOfPiece);
            if (!m.find()) continue;
            String subCategoryTitle = m.group(1);


            m = themePattern.matcher(digestPieceOfPiece + "</li>");
            while (m.find()) {
                Matcher m1 = prPattern.matcher(Html.fromHtml(m.group(1)).toString().trim());
                if (!m1.find()) continue;
                Topic topic = new Topic(m.group(2), m1.group(1));
                topic.setLastMessageAuthor(m1.group(2));
                topic.setDescription(m1.group(3));
                topic.setForumTitle(subCategoryTitle);
                category.addTheme(topic);
            }


        }
    }

    public void getAppDigestSubCategoryThemes(final Forum subCategory, String dateTitle, String newOldTitle, OnProgressChangedListener progressChangedListener) throws IOException {
        String msgSubCategory = getAppDigestMsgSubCategory(dateTitle, newOldTitle, progressChangedListener);
        if (msgSubCategory == null) return;

        final Pattern digestPieceOfPiecePattern = Pattern.compile("<!--coloro:.*?--><span style=\"color:coral\"><!--/coloro--><b>(.*?)</b>");
        final Pattern themePattern = Pattern.compile("<li>(.*?<a href=\"http://4pda.ru/forum/index.php\\?showtopic=(\\d+)\"(.*?))</li>");
        final Pattern prPattern = Pattern.compile("(.*?)\\[(.*?)\\](.*?)$");
        String[] digestPiecesOfPiece = msgSubCategory.split("<!--coloro:coral--><span style=\"color:coral\"><!--/coloro--><b>");// разбиваем на категории
        for (String digestPieceOfPiece : digestPiecesOfPiece) {
            Matcher m = digestPieceOfPiecePattern.matcher("<!--coloro:coral--><span style=\"color:coral\"><!--/coloro--><b>" + digestPieceOfPiece);
            if (!m.find()) continue;
            String subCategoryTitle = m.group(1);
            if (!subCategoryTitle.equals(subCategory.getTitle())) continue;

            m = themePattern.matcher(digestPieceOfPiece + "</li>");
            while (m.find()) {
                Matcher m1 = prPattern.matcher(Html.fromHtml(m.group(1)).toString().trim());
                if (!m1.find()) continue;
                Topic topic = new Topic(m.group(2), m1.group(1));
                topic.setLastMessageAuthor(m1.group(2));
                topic.setDescription(m1.group(3));
                topic.setForumTitle(subCategoryTitle);
                subCategory.addTheme(topic);
            }


        }
    }

    private String getGameDigestMsgSubCategory(String dateTitle, String newOldTitle, OnProgressChangedListener progressChangedListener) throws IOException {

        String body = Client.INSTANCE.loadPageAndCheckLogin("http://4pda.ru/forum/index.php?showtopic="+GAME_CATALOG_ID, progressChangedListener);

        Pattern msgPattern = Pattern.compile("<div class=\"post_body\"><div align='center'><!--coloro:royalblue--><span style=\"color:royalblue\"><!--/coloro--><b><!--sizeo:4--><span style=\"font-size:14pt;line-height:100%\"><!--/sizeo-->" + Pattern.quote(dateTitle) + "<([\\s\\S]*?)((<!--Begin Msg Number)|(<!-- TABLE FOOTER -->))");
        final Pattern digestPiecePattern = Pattern.compile("<b><!--coloro:.*?--><span style=\"color:.*?\"><!--/coloro-->" + Pattern.quote(newOldTitle) + "<([\\s\\S]*?)((<b><!--coloro:.*?--><span style=\"color:.*?\"><!--/coloro-->)|(</div>\\Z))");

        Matcher m = msgPattern.matcher(body);
        if (!m.find()) return null;
        String msg = m.group(1);


        m = digestPiecePattern.matcher(msg);
        if (!m.find()) return null;


        return m.group(1);
    }

    public void getGameDigestCategoryThemes(final Forum category, String dateTitle, OnProgressChangedListener progressChangedListener) throws IOException {
        String msgSubCategory = getGameDigestMsgSubCategory(dateTitle, category.getTitle(), progressChangedListener);
        if (msgSubCategory == null) return;


        // final Pattern themePattern = Pattern.compile("<li>.*?<a href=\"http://4pda.ru/forum/index.php\\?showtopic=(\\d+)\" target=\"_blank\">(.*?)</a>.*?<span style=\"color:royalblue\"><!--/coloro-->\\[(.*?)\\].*?> - (.*?)<");
        final Pattern themePattern = Pattern.compile("<li>(.*?<a href=\"http://4pda.ru/forum/index.php\\?showtopic=(\\d+)\"(.*?))</li>");
        final Pattern prPattern = Pattern.compile("(.*?)\\[(.*)](.*?)$");
        String[] digestPiecesOfPiece = msgSubCategory.split("<ol type='1'>");// разбиваем на категории
        for (String digestPieceOfPiece : digestPiecesOfPiece) {


            Matcher m = themePattern.matcher(digestPieceOfPiece);
            while (m.find()) {
                Matcher m1 = prPattern.matcher(Html.fromHtml(m.group(1)).toString().trim());
                if (!m1.find()) continue;
                Topic topic = new Topic(m.group(2), m1.group(1));
                String version = m1.group(2);
                int scIndex = version.indexOf('[');
                if (scIndex != -1)
                    version = version.substring(scIndex + 1);
                topic.setLastMessageAuthor(version);
                topic.setDescription(m1.group(3));

                category.addTheme(topic);
            }


        }
    }


}