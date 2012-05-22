package org.softeg.slartus.forpda.classes;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import org.softeg.slartus.forpda.Client;
import org.softeg.slartus.forpda.R;
import org.softeg.slartus.forpda.ThemeActivity;
import org.softeg.slartus.forpda.classes.common.Functions;
import org.softeg.slartus.forpda.common.Log;

import java.io.IOException;
import java.net.URI;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by IntelliJ IDEA.
 * User: Admin
 * Date: 19.09.11
 * Time: 20:46
 * To change this template use File | Settings | File Templates.
 */
public class Topic extends ForumItem {


    private Messages m_Messages = new Messages();
    private String lastMessageAuthor;
    private Date lastMessageDate;
    private String description;
    private String forumTitle;
    private String lastMessageAuthorId;
    private String forumId;
    private String authKey;
    private String state;
    private boolean isNew = false;
    private boolean mIsOld = false;

    private Spanned m_SpannedTitle;

    public Topic(String id, String title) {
        m_Id = id;
        m_SpannedTitle = Html.fromHtml(title);

    }

    public void setId(String value) {
        m_Id = value;
    }


    public Spanned getTitle() {
        return m_SpannedTitle;
    }


    private int m_PagesCount = 1;

    public int getPagesCount() {
        return m_PagesCount;
    }

    public void setPagesCount(String value) {
        m_PagesCount = Integer.parseInt(value) + 1;
    }

    public int getPostsPerPageCount(String m_LastUrl) {
        String lastUrl = m_LastUrl;
        URI redirectUri = Client.INSTANCE.getRedirectUri();
        if (redirectUri != null)
            lastUrl = redirectUri.toString();
        Pattern p = Pattern.compile("st=(\\d+)");
        Matcher m = p.matcher(lastUrl);
        if (m.find())
            m_LastPageStartCount = Math.max(Integer.parseInt(m.group(1)), m_LastPageStartCount);

        return m_LastPageStartCount / (m_PagesCount - 1);
    }

    private int m_LastPageStartCount = 0;

    public int getLastPageStartCount() {
        return m_LastPageStartCount;
    }

    public void setLastPageStartCount(String value) {
        m_LastPageStartCount = Math.max(Integer.parseInt(value), m_LastPageStartCount);
    }

    public void addMessage(Post post) {
        m_Messages.add(post);
    }

    public Messages getMessages() {
        return m_Messages;
    }

    private int m_CurrentPage = 0;

    public void setCurrentPage(String value) {
        m_CurrentPage = Integer.parseInt(value);
    }

    public int getCurrentPage() {
        return m_CurrentPage;
    }

    public void setTitle(String value) {
        if (value != null)
            m_SpannedTitle = Html.fromHtml(value);
    }

    private String m_PostsCount;

    public void setPostsCount(String postsCount) {
        m_PostsCount = postsCount;
    }

    private Spanned m_SpannedLastMessageAuthor;

    public Spanned getLastMessageAuthor() {
        return m_SpannedLastMessageAuthor;
    }

    public void setLastMessageAuthor(String lastMessageAuthor) {
        if (lastMessageAuthor != null)
            m_SpannedLastMessageAuthor = Html.fromHtml(lastMessageAuthor);
    }

    private String m_lastMessageDateStr = null;

    public CharSequence getLastMessageDateStr() {
        return m_lastMessageDateStr;
    }

    public Date getLastMessageDate() {
        return lastMessageDate;
    }

    public void setLastMessageDate(Date lastMessageDate) {
        this.lastMessageDate = lastMessageDate;
        if (lastMessageDate == null) {
            lastMessageDate = new Date();
        }
        m_lastMessageDateStr = Functions.getForumDateTime(lastMessageDate);
    }

    private Spanned m_SpannedDescription;

    public Spanned getDescription() {
        return m_SpannedDescription;
    }

    public void setDescription(String description) {
        if (description != null)
            m_SpannedDescription = Html.fromHtml(description);
    }

    public void setForumTitle(String forumTitle) {

        this.forumTitle = forumTitle;
        getForumTitle();
    }

    public void setLastMessageAuthorId(String lastMessageAuthorId) {

        this.lastMessageAuthorId = lastMessageAuthorId;
    }

    public void setForumId(String forumId) {

        this.forumId = forumId;
    }

    public void showActivity(Context context) {
        Intent intent = new Intent(context, ThemeActivity.class);
        intent.putExtra("ThemeUrl", m_Id);

        context.startActivity(intent);
    }

    public void showActivity(Context context, String params) {
        showActivity(context, m_Id, params);
    }

    public static void showActivity(Context context, String themeId, String params) {
        Intent intent = new Intent(context, ThemeActivity.class);
        intent.putExtra("ThemeUrl", themeId);
        intent.putExtra("Params", params);
        context.startActivity(intent);
    }

    public void showBrowser(Context context, String params) {
        Intent marketIntent = new Intent(
                Intent.ACTION_VIEW,
                Uri.parse("http://" + Client.SITE + "/forum/index.php?showtopic=" + m_Id + (TextUtils.isEmpty(params) ? "" : ("&" + params))));
        context.startActivity(Intent.createChooser(marketIntent,"Выберите"));
    }

    public String getAuthKey() {
        return authKey;
    }

    public void setAuthKey(String authKey) {
        this.authKey = authKey;
    }

    public String getForumId() {
        return forumId;
    }

    public String getForumTitle() {
        return forumTitle;
    }

    public String addToFavorites() throws IOException {
        return Client.INSTANCE.addToFavorites(this);
    }

    public String removeFromFavorites() throws IOException {
        return Client.INSTANCE.removeFromFavorites(this);
    }

    public void checkForBrowsing() throws ThemeParseException {
        if (TextUtils.isEmpty(forumId))
            throw new ThemeParseException("Ошибка разбора страницы: не найден идентификатор форума. themeid: " + m_Id);
        if (TextUtils.isEmpty(authKey) && Client.INSTANCE.getLogined())
            throw new ThemeParseException("Ошибка разбора страницы: не найден ключ идентификации. themeid: " + m_Id);
        if (m_CurrentPage == 0)
            throw new ThemeParseException("Ошибка разбора страницы: не найдена текущая страница. themeid: " + m_Id);
        if (TextUtils.isEmpty(m_SpannedTitle))
            throw new ThemeParseException("Ошибка разбора страницы: не найден заголовок. themeid: " + m_Id);
        if (m_Messages.size() == 0)
            throw new ThemeParseException("Ошибка разбора страницы: не найдены сообщения. themeid: " + m_Id);

    }

    public void setIsNew(boolean aNew) {
        isNew = aNew;
    }

    public boolean getIsNew() {
        return isNew;
    }

    public void setIsOld(boolean old) {
        mIsOld = old;
    }

    public boolean getIsOld() {
        return mIsOld;
    }


    public void startSubscribe(final Context context, final android.os.Handler handler) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.subscribe_dialog, null);

        final RadioButton emailnone_radio = (RadioButton) layout.findViewById(R.id.emailnone_radio);
        final RadioButton emaildelayed_radio = (RadioButton) layout.findViewById(R.id.emaildelayed_radio);
        final RadioButton emailimmediate_radio = (RadioButton) layout.findViewById(R.id.emailimmediate_radio);
        final RadioButton emaildaily_radio = (RadioButton) layout.findViewById(R.id.emaildaily_radio);
        final RadioButton emailweekly_radio = (RadioButton) layout.findViewById(R.id.emailweekly_radio);
        final RadioGroup emailtype_radio = (RadioGroup) layout.findViewById(R.id.emailtype_radio);
        new AlertDialog.Builder(context)
                .setTitle("Подписка на тему")
                .setView(layout)
                .setPositiveButton("Подписаться", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();

                        View v = emailtype_radio.findViewById(emailtype_radio.getCheckedRadioButtonId());
                        String emailtype = "delayed";
                        if (v == emailnone_radio) {
                            emailtype = "none";
                        } else if (v == emaildelayed_radio) {
                            emailtype = "delayed";
                        } else if (v == emailimmediate_radio) {
                            emailtype = "immediate";
                        } else if (v == emaildaily_radio) {
                            emailtype = "daily";
                        } else if (v == emailweekly_radio) {
                            emailtype = "weekly";
                        }

                        Toast.makeText(context, "Запрос на подписку отправлен", Toast.LENGTH_SHORT).show();


                        final String finalEmailtype = emailtype;
                        new Thread(new Runnable() {
                            public void run() {

                                Exception ex = null;

                                String res = null;
                                try {
                                    res = Client.INSTANCE.themeSubscribe(Topic.this, finalEmailtype);
                                } catch (Exception e) {
                                    ex = e;
                                }

                                final Exception finalEx = ex;
                                final String finalRes = res;
                                handler.post(new Runnable() {
                                    public void run() {
                                        try {
                                            if (finalEx != null) {
                                                Toast.makeText(context, "Ошибка подписки", Toast.LENGTH_SHORT).show();
                                                Log.e(context, finalEx);
                                            } else {
                                                Toast.makeText(context, finalRes, Toast.LENGTH_SHORT).show();
                                            }
                                        } catch (Exception ex) {
                                            Log.e(context, ex);
                                        }

                                    }
                                });
                            }
                        }).start();

                    }
                })
                .setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                })
                .create().show();
    }

    public void unSubscribe(final Context context, final android.os.Handler handler) {
        Toast.makeText(context, "Запрос на отписку отправлен", Toast.LENGTH_SHORT).show();
        new Thread(new Runnable() {
            public void run() {

                Exception ex = null;

                String res = null;
                try {
                    res = Client.INSTANCE.unSubscribe(Topic.this);
                } catch (Exception e) {
                    ex = e;
                }

                final Exception finalEx = ex;

                final String finalRes = res;
                handler.post(new Runnable() {
                    public void run() {
                        try {
                            if (finalEx != null) {
                                Log.e(context, finalEx);
                            } else {
                                Toast.makeText(context, finalRes, Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception ex) {
                            Log.e(context, ex);
                        }

                    }
                });
            }
        }).start();


    }

    private Boolean m_IsModerator = false;


    public boolean isModerator() {
        return m_IsModerator;
    }

    public void dispose() {
        if (m_Messages != null)
            m_Messages.clear();
    }

    public class ThemeParseException extends Exception {
        public ThemeParseException(String message) {
            super(message);
        }
    }
}
