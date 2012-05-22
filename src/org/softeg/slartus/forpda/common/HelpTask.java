package org.softeg.slartus.forpda.common;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import java.io.IOException;

/**
 * User: slinkin
 * Date: 10.10.11
 * Time: 9:09
 */
public class HelpTask extends AsyncTask<HelpTask.OnMethodListener, String, Boolean> {

    public interface OnMethodListener {
        Object onMethod(Object param) throws IOException;
    }

    Context mContext;
    private final ProgressDialog dialog;
    private String m_ProcessMessage = "Загрузка...";

    public HelpTask(Context context,  String processMessage) {
        mContext = context;

        m_ProcessMessage=processMessage;
        dialog = new ProgressDialog(mContext);
        dialog.setCancelable(false);
    }

    @Override
    protected void onProgressUpdate(String... progress) {
        this.dialog.setMessage(progress[0]);
    }

    public void progressUpdate(String... progress) {
        publishProgress(progress);
    }

    private OnMethodListener m_OnPostMethod;

    public void setOnPostMethod(OnMethodListener onPostMethod) {
        m_OnPostMethod = onPostMethod;
    }

    public Boolean Success;
    public Object Result;

    @Override
    protected Boolean doInBackground(HelpTask.OnMethodListener... params) {
        try {
            Result = params[0].onMethod(null);
            return true;
        } catch (Exception e) {
            Log.e(mContext, e);
            ex = e;
            return false;
        }
    }


    protected void onPreExecute() {
        this.dialog.setCancelable(false);
        this.dialog.setMessage(m_ProcessMessage);
        this.dialog.show();
    }

    protected void onCancelled() {
        Toast.makeText(mContext, "Отменено",
                Toast.LENGTH_SHORT).show();
    }

    public Exception ex;


    protected void onPostExecute(final Boolean success) {
        if (this.dialog.isShowing()) {
            this.dialog.dismiss();
        }
        Success = success;
        try {
            m_OnPostMethod.onMethod(Result);
        } catch (Exception ex) {
            Log.e(mContext, ex);
        }
    }

}