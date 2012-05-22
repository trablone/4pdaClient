package org.softeg.slartus.forpda;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.widget.Toast;
import org.softeg.slartus.forpda.common.Log;

/**
 * User: slinkin
 * Date: 30.09.11
 * Time: 13:31
 */


public class LoginTask extends AsyncTask<String, Void, Boolean> {

    Context mContext;
    private final ProgressDialog dialog;

    public LoginTask(Context context) {
        mContext = context;
        dialog = new ProgressDialog(mContext);
        dialog.setCancelable(false);
    }

    private String m_Login;
    private String m_Password;

    private Boolean m_LoginRemember;
    private Boolean m_AutoLogin;
    private Boolean m_Privacy;

    @Override
    protected Boolean doInBackground(String... params) {
        try {
            m_Login = params[0];
            m_Password = params[1];
            m_Privacy = Boolean.parseBoolean(params[2]);
            m_LoginRemember = Boolean.parseBoolean(params[3]);
            m_AutoLogin = Boolean.parseBoolean(params[4]);
            Client client = Client.INSTANCE;

            return client.login(m_Login, m_Password, m_Privacy);
        } catch (Exception e) {

            ex = e;
            return false;
        }
    }

    // can use UI thread here
    protected void onPreExecute() {
        this.dialog.setMessage("Вход...");
        this.dialog.show();
    }

    protected void onCancelled() {
        Toast.makeText(mContext, "Отменено",
                Toast.LENGTH_SHORT).show();
    }

    private Exception ex;
    private Client.OnUserChangedListener m_OnUserChangedListener = null;

    private void doOnUserChangedListener(String user, Boolean success) {
        if (m_OnUserChangedListener != null) {
            m_OnUserChangedListener.onUserChanged(user, success);
        }
    }

    public void setOnUserChangedListener(Client.OnUserChangedListener p) {
        m_OnUserChangedListener = p;
    }

    // can use UI thread here
    protected void onPostExecute(final Boolean success) {
        if (this.dialog.isShowing()) {
            this.dialog.dismiss();
        }
        doOnUserChangedListener(m_Login, success);
        Client.INSTANCE.doOnUserChangedListener(m_Login, success);
        if (success) {

            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mContext);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean("LoginRemember", m_LoginRemember);
            editor.putBoolean("AutoLogin", m_AutoLogin);
            editor.putBoolean("LoginPrivacy", m_Privacy);
            if (m_LoginRemember) {
                editor.putString("Login", m_Login);

            }

            editor.commit();
            Toast.makeText(mContext, "Вход выполнен",
                    Toast.LENGTH_SHORT).show();
        } else {
            if (ex != null)
                Log.e(mContext, ex);
            else
                 new AlertDialog.Builder(mContext)
                        .setIcon(R.drawable.icon)
                        .setTitle("Ошибка")
                        .setMessage(Client.INSTANCE.getLoginFailedReason())
                        .setPositiveButton(android.R.string.ok, null)
                        .create().show();
//                    Toast.makeText(mContext, Client.INSTANCE.getLoginFailedReason(),
//                            Toast.LENGTH_SHORT).show();
        }
    }

}
