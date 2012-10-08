package org.softeg.slartus.forpda.classes;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.text.Html;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import org.softeg.slartus.forpda.Client;
import org.softeg.slartus.forpda.R;
import org.softeg.slartus.forpda.ThemeActivity;
import org.softeg.slartus.forpda.common.Log;

import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: Admin
 * Date: 21.09.11
 * Time: 0:10
 * To change this template use File | Settings | File Templates.
 */
public class Post {
    private String m_Id;
    private String m_Date;
    private String m_UserNick;
    private String m_UserGroup;
    private String m_UserId;
    private String m_UserState;
    private String m_UserReputation;
    private String m_Body;

    private String m_Number;


    public Post(String id, String date, String author, String body) {

        m_Id = id;
        m_Date = date;
        m_UserNick = author;
        m_Body = body;
    }

    public Post(String id, String date, String number) {

        m_Id = id;
        m_Date = date;
        m_Number = number;

    }

    public String getDate() {
        return m_Date;
    }

    public void setAuthor(String author) {
        m_UserNick = author;
    }

    public void setBody(String value) {
        m_Body = value
                .replaceAll("('|\")/forum/style_images", "$1file:///android_asset/forum/style_images")
                .replaceAll("('|\")style_images", "$1file:///android_asset/forum/style_images")
                .replaceAll("\"http://s.4pda.ru/forum/style_emoticons", "\"file:///android_asset/forum/style_emoticons")
                .replaceAll("\"http://sc.4pda.ru/forum/style_emoticons", "\"file:///android_asset/forum/style_emoticons")
                .replaceAll("(src|href)=('|\")index.php", "$1=$2http://" + Client.SITE + "/forum/index.php")
                .replaceAll("(src|href)=('|\")/forum", "$1=$2http://" + Client.SITE + "/forum");

//        int i = -1;
//        int startDivCount = 0;
//        while ((i = m_Body.indexOf("<div", i + 1)) != -1)
//            startDivCount++;
//        int endDivCount = 0;
//        i = -1;
//        while ((i = m_Body.indexOf("</div>", i + 1)) != -1)
//            endDivCount++;
//
//
//        while (endDivCount > startDivCount) {
//            m_Body = "<div>" + m_Body;
//            startDivCount++;
//        }
//
//        while (endDivCount < startDivCount) {
//            m_Body += "</div>";
//            endDivCount++;
//        }
    }

    public String getNumber() {
        return m_Number;
    }


    public String getBody() {
        return m_Body;
    }


    public static String getQuote(String postId, String date, String userNick, String text) {
        return "[quote name='" + userNick + "' date='" + date + "' post=" + postId + "]" + text + "[/quote]";
    }

    public String getId() {
        return m_Id;
    }

    public String getLink(String topicId) {
        return getLink(topicId, m_Id);
    }

    public static String getLink(String topicId, String postId) {
        return "http://4pda.ru/forum/index.php?showtopic=" + topicId + "&view=findpost&p=" + postId;
    }

    private Spanned m_Spanned;

    public Spanned getSpannedBody() {
        if (m_Body == null) return null;
        if (m_Spanned == null)
            m_Spanned = Html.fromHtml(m_Body);
        return m_Spanned;
    }


    public void setUserId(String value) {
        m_UserId = value;
    }

    public String getUserId() {
        return m_UserId;
    }


    public void setUserState(String value) {
        m_UserState = value;
    }

    public Boolean getUserState() {
        return m_UserState == null ? false : m_UserState.equals("green");
    }


    public void setUserGroup(String value) {
        m_UserGroup = value;
    }
    
    public String getUserGroup(){
        return m_UserGroup;
    }

    public void setUserReputation(String value) {
        m_UserReputation = value;
    }

    @Override
    public String toString() {
        return m_Body;
    }

    public String getNick() {
        return m_UserNick;
    }

    public String getUserReputation() {
        return m_UserReputation;
    }

    private Boolean m_CanEdit = false;

    public void setCanEdit(boolean value) {
        m_CanEdit = value;
    }

    public Boolean getCanEdit() {
        return m_CanEdit;
    }

    private Boolean m_CanDelete = false;

    public void setCanDelete(boolean value) {
        m_CanDelete = value;
    }

    public Boolean getCanDelete() {
        return m_CanDelete;
    }

    public Boolean needShowMenu() {
        return getCanDelete() || getCanEdit();
    }

    public static void delete(String postId, String forumId, String themeId, String authKey) throws IOException {
        Client.INSTANCE.deletePost(forumId, themeId, postId, authKey);
    }

    private Boolean m_CanPlusRep = false;

    public void setCanPlusRep(boolean value) {
        m_CanPlusRep = value;
    }

    public Boolean getCanPlusRep() {
        return m_CanPlusRep;
    }

    private Boolean m_CanMinusRep = false;

    public void setCanMinusRep(boolean value) {
        m_CanMinusRep = value;
    }

    public Boolean getCanMinusRep() {
        return m_CanMinusRep;
    }

    public static void claim(
            final Context context,
            final android.os.Handler handler,
            final String themeId,
            final String postId) {


        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.claim, null);


        final EditText message_edit = (EditText) layout.findViewById(R.id.message_edit);

        new AlertDialog.Builder(context)
                .setTitle("Отправить жалобу модератору на сообщение")
                .setView(layout)
                .setPositiveButton("Отправить жалобу", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();

                        Toast.makeText(context, "Жалоба отправлена", Toast.LENGTH_SHORT).show();

                        new Thread(new Runnable() {
                            public void run() {
                                Exception ex = null;

                                String res = null;
                                try {
                                    res = Client.INSTANCE.claim(themeId, postId, message_edit.getText().toString());
                                } catch (IOException e) {
                                    ex = e;
                                }

                                final Exception finalEx = ex;
                                final String finalRes = res;
                                handler.post(new Runnable() {
                                    public void run() {
                                        try {
                                            if (finalEx != null) {
                                                Toast.makeText(context, "Ошибка отправки жалобы", Toast.LENGTH_LONG).show();
                                                Log.e(context, finalEx);
                                            } else {
                                                Toast.makeText(context, finalRes, Toast.LENGTH_LONG).show();
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

    public static void plusOne(ThemeActivity themeActivity, Handler handler, String postId) {
        changePostReputation(themeActivity,handler,postId,"1");
    }

    public static void minusOne(ThemeActivity themeActivity, Handler handler, String postId) {
        changePostReputation(themeActivity,handler,postId,"-1");
    }

    private static void changePostReputation(final ThemeActivity themeActivity,final Handler handler, final String postId, final String direction) {
        Toast.makeText(themeActivity, "Запрос на изменение репутации отправлен", Toast.LENGTH_SHORT).show();
        // http://s.4pda.ru/forum/jscripts/karma3.js
        new Thread(new Runnable() {
            public void run() {
                Exception ex = null;

                String message=null;
                try {
                    String res = Client.INSTANCE.performGet("http://4pda.ru/forum/zka.php?i=" + postId + "&v=" + direction);

                    if (("ok:"+direction).equals(res))
                        message= "Репутация поста изменена";
                    else if("ok:0".equals(res))
                        message= "Ошибка изменения репутации: Вы уже голосовали за этот пост";
                    else
                        message= "Ошибка изменения репутации: "+res;
                } catch (IOException e) {
                    ex = e;
                }

                final Exception finalEx = ex;

                final String finalMessage = message;
                handler.post(new Runnable() {
                    public void run() {
                        try {
                            if (finalEx != null) {
                                Toast.makeText(themeActivity, "Ошибка изменения репутации поста", Toast.LENGTH_LONG).show();
                                Log.e(themeActivity, finalEx);
                            } else {
                                Toast.makeText(themeActivity, finalMessage, Toast.LENGTH_LONG).show();
                            }
                        } catch (Exception ex) {
                            Log.e(themeActivity, ex);
                        }

                    }
                });
            }
        }).start();
    }
}

