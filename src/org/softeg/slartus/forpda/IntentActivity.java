package org.softeg.slartus.forpda;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import org.softeg.slartus.forpda.Mail.MailActivity;
import org.softeg.slartus.forpdaapi.NotReportException;
import org.softeg.slartus.forpda.common.Log;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by IntelliJ IDEA.
 * User: slartus
 * Date: 17.01.12
 * Time: 13:26
 * To change this template use File | Settings | File Templates.
 */
public class IntentActivity extends Activity {
    private Uri m_Data = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public Boolean isAppUrl(String url) {
        return isTheme(url) || isFileOrImage(url) || isNews(url) || isDevDb(url);
    }

    public static boolean isFileOrImage(String url) {
        Pattern filePattern = Pattern.compile("http://4pda.ru/forum/dl/post/\\d+/.*");
        Pattern stFilePattern = Pattern.compile("http://st.4pda.ru/wp-content/uploads/.*");
        return filePattern.matcher(url).find() || stFilePattern.matcher(url).find();
    }

    public static Boolean isNews(String url) {
        final Pattern pattern = Pattern.compile("http://4pda.ru/\\d{4}/\\d{2}/\\d{2}/\\d+/");
        final Pattern pattern1 = Pattern.compile("http://4pda.ru/(\\w+)/(older|newer)/\\d+");

        return pattern.matcher(url).find()|| pattern1.matcher(url).find();
    }

    public static boolean isTheme(String url) {
        Pattern p = Pattern.compile("http://" + Client.SITE + "/forum/index.php\\?((.*)?showtopic=.*)");
        Pattern p1 = Pattern.compile("http://" + Client.SITE + "/forum/index.php\\?((.*)?act=findpost&pid=\\d+(.*)?)");
        Matcher m = p.matcher(url);
        Matcher m1 = p1.matcher(url);
        return m.find() || m1.find();
    }

    public static boolean isMail(String url) {


        Pattern p = Pattern.compile("http://4pda.ru/forum/index.php\\?act=Msg&CODE=03&VID=in&MSID=(\\d+)");

        Matcher m = p.matcher(url);


        return m.find() ;
    }

    public static boolean isDevDb(String url) {
        Pattern p = Pattern.compile("http://devdb.ru");

        Matcher m = p.matcher(url);

        return m.find();
    }


    public static Boolean tryShowUrl(Activity context, Handler handler, String url, Boolean showInDefaultBrowser,
                                     final Boolean finishActivity) {
        if (isTheme(url)) {
            Intent themeIntent = new Intent(context, ThemeActivity.class);
            themeIntent.setData(Uri.parse(url));
            context.startActivity(themeIntent);

            context.finish();
            return true;
        }

        if (isMail(url)) {
            Intent themeIntent = new Intent(context, MailActivity.class);
            themeIntent.setData(Uri.parse(url));
            context.startActivity(themeIntent);

            context.finish();
            return true;
        }

        if (tryShowFile(context, handler, url, finishActivity)) {
            return true;
        }
        if (showInDefaultBrowser)
            showInDefaultBrowser(context, url);
        if (finishActivity)
            context.finish();
        return false;
    }

    private static boolean tryShowFile(final Activity activity, final Handler handler, final String url, final Boolean finish) {
        Pattern filePattern = Pattern.compile("http://4pda.ru/forum/dl/post/\\d+/.*");
        Pattern stFilePattern = Pattern.compile("http://st.4pda.ru/wp-content/uploads/.*");
        final Pattern imagePattern = Pattern.compile("http://.*?\\.(png|jpg|jpeg|gif)");
        if (filePattern.matcher(url).find() || stFilePattern.matcher(url).find()) {
            if (!Client.INSTANCE.getLogined() && !Client.INSTANCE.hasLoginCookies()) {
                Client.INSTANCE.showLoginForm(activity, new Client.OnUserChangedListener() {
                    public void onUserChanged(String user, Boolean success) {
                        if (success) {
                            if (imagePattern.matcher(url).find()) {
                                showImage(activity, url);
                                if (finish)
                                    activity.finish();
                            } else
                                downloadFileStart(activity, handler, url, finish);
                        } else if (finish)
                            activity.finish();

                    }
                });
            } else {
                if (imagePattern.matcher(url).find()) {
                    showImage(activity, url);
                    if (finish)
                        activity.finish();
                } else
                    downloadFileStart(activity, handler, url, finish);
            }

            return true;
        }
        if (imagePattern.matcher(url).find()) {
            showImage(activity, url);
            if (finish)
                activity.finish();
            return true;
        }
        return false;
    }

    private static void showImage(Context context, String url) {
        Intent intent = new Intent(context, ImageViewActivity.class);
        intent.putExtra(ImageViewActivity.URL_KEY, url);

        context.startActivity(intent);
    }

    public static void downloadFileStart(final Activity activity, final Handler handler, final String url, final Boolean finish) {
        if (PreferenceManager.getDefaultSharedPreferences(activity.getApplicationContext()).getBoolean("files.ConfirmDownload", true)) {
            new AlertDialog.Builder(activity)
                    .setTitle("Уверены?")
                    .setMessage("Начать закачку файла?")
                    .setPositiveButton("ОК", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialogInterface, int i) {
                            DownloadsActivity.download(activity, url);
                            dialogInterface.dismiss();
                            if (finish)
                                activity.finish();
                        }
                    })
                    .setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialogInterface, int i) {
                            if (finish)
                                activity.finish();
                        }
                    })
                    .create().show();
        } else {
            DownloadsActivity.download(activity, url);
            if (finish)
                activity.finish();
        }

    }

    public static void showInDefaultBrowser(Context context, String url) {
        try {
            Intent marketIntent = new Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(url));
            context.startActivity(marketIntent);
        } catch (Exception ex) {
            Log.e(context, new NotReportException("Не найдено ни одно приложение для ссылки: " + url));
        }
    }
}