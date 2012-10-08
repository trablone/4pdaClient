package org.softeg.slartus.forpda.classes;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import net.londatiga.android.ActionItem;
import net.londatiga.android.QuickAction;
import org.softeg.slartus.forpda.Client;
import org.softeg.slartus.forpda.Mail.EditMailActivity;
import org.softeg.slartus.forpda.ProfileActivity;
import org.softeg.slartus.forpda.R;
import org.softeg.slartus.forpda.common.Log;
import org.softeg.slartus.forpda.qms.QmsChatActivity;

import java.io.IOException;

/**
 * User: slinkin
 * Date: 27.09.11
 * Time: 10:44
 */
public class ForumUser {
    private String m_Nick;
    private String m_Group;
    private String m_Id;
    private String m_Reputation;

    public static void showUserMenu(final Activity context, View view, final String userId, final String userNick) {
        // не забыть менять в ThemeActivity
        final QuickAction mQuickAction = new QuickAction(context);

        int insertNickPosition = -1;
//        if (Client.INSTANCE.getLogined()) {
//            ActionItem actionItem = new ActionItem();
//            //actionItem.setIcon(getResources().getDrawable(android.R.drawable.ic_menu_edit));
//            actionItem.setTitle("Вставить ник");
//            insertNickPosition = mQuickAction.addActionItem(actionItem);
//        }

        int sendLSPosition = -1;
        if (Client.INSTANCE.getLogined()) {
            ActionItem actionItem = new ActionItem();
            // actionItem.setIcon(getResources().getDrawable(android.R.drawable.ic_menu_edit));
            actionItem.setTitle("ЛС");

            sendLSPosition = mQuickAction.addActionItem(actionItem);
        }

        int sendQmsPosition = -1;
        if (Client.INSTANCE.getLogined()) {
            ActionItem actionItem = new ActionItem();
            // actionItem.setIcon(getResources().getDrawable(android.R.drawable.ic_menu_edit));
            actionItem.setTitle("QMS");

            sendQmsPosition = mQuickAction.addActionItem(actionItem);
        }

        int showProfilePosition = -1;
        ActionItem actionItem = new ActionItem();
        actionItem.setTitle("Профиль");
        showProfilePosition = mQuickAction.addActionItem(actionItem);

        if (mQuickAction.getItemsCount() == 0) return;

        final int finalInsertNickPosition = insertNickPosition;
        final int finalSendLSPosition = sendLSPosition;
        final int finalSendQmsPosition = sendQmsPosition;
        final int finalShowProfilePosition = showProfilePosition;
        mQuickAction.setOnActionItemClickListener(new QuickAction.OnActionItemClickListener() {
            public void onItemClick(int pos) {
                try {
                    if (pos == finalSendLSPosition) {
                        Intent intent = new Intent(context, EditMailActivity.class);
                        intent.putExtra(EditMailActivity.KEY_PARAMS, "CODE=04&act=Msg&MID=" + userId);
                        intent.putExtra(EditMailActivity.KEY_USER, userNick);
                        intent.putExtra(EditMailActivity.KEY_RETERN_BACK, true);
                        context.startActivity(intent);
                    } else if (pos == finalSendQmsPosition) {
                        QmsChatActivity.openChat(context, userId, userNick);
                    } else if (pos == finalShowProfilePosition) {
                        ProfileActivity.startActivity(context,userId,userNick);
                    }

                } catch (Exception ex) {
                    Log.e(context, ex);
                }
            }
        });

        mQuickAction.show(view);
    }
    
    public static void startChangeRep(final Context context, final android.os.Handler handler, final String userId,
                                      String userNick, final String postId, final String type, String title) {


        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.reputation, null);

        TextView username_view = (TextView) layout.findViewById(R.id.username_view);
        final EditText message_edit = (EditText) layout.findViewById(R.id.message_edit);
        username_view.setText(userNick);
        new AlertDialog.Builder(context)
                .setTitle(title)
                .setView(layout)
                .setPositiveButton("Изменить", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();

                        Toast.makeText(context, "Запрос на изменение репутации отправлен", Toast.LENGTH_SHORT).show();

                        new Thread(new Runnable() {
                            public void run() {
                                Exception ex = null;

                                String res = null;
                                try {
                                    res = Client.INSTANCE.changeReputation(postId, userId, type, message_edit.getText().toString());
                                } catch (IOException e) {
                                    ex = e;
                                }

                                final Exception finalEx = ex;
                                final String finalRes = res;
                                handler.post(new Runnable() {
                                    public void run() {
                                        try {
                                            if (finalEx != null) {
                                                Toast.makeText(context, "Ошибка изменения репутации", Toast.LENGTH_SHORT).show();
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
}
