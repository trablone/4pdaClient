package org.softeg.slartus.forpda;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.text.Html;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;
import android.widget.Toast;
import com.actionbarsherlock.app.SherlockPreferenceActivity;
import org.softeg.slartus.forpda.Tabs.Tabs;
import org.softeg.slartus.forpda.classes.Exceptions.NotReportException;
import org.softeg.slartus.forpda.classes.ForumUser;
import org.softeg.slartus.forpda.classes.common.FileUtils;
import org.softeg.slartus.forpda.classes.common.StringUtils;
import org.softeg.slartus.forpda.common.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;

/**
 * User: slinkin
 * Date: 03.10.11
 * Time: 10:47
 */
public class PreferencesActivity extends SherlockPreferenceActivity {
    private Handler mHandler = new Handler();

//    private static String getFilesDir(Context context){
//        return  context.getApplicationContext().getFilesDir().toString();
//    }

    public static String getCookieFilePath(Context context) {

        String defaultFile = context.getApplicationContext().getFilesDir()+"/4pda_cookies";

//        if (Build.MANUFACTURER.equals("LGE") && Build.MODEL.equals("GT540"))
//            defaultFile = Environment.getExternalStorageDirectory()
//                    + "/4pdaClient/4pda_cookies";
//        if (Build.MANUFACTURER.equals("TOSHIBA") && Build.MODEL.equals("TOSHIBA_AC_AND_AZ"))
//            defaultFile = Environment.getExternalStorageDirectory()
//                    + "/4pdaClient/4pda_cookies";
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String res = prefs.getString("cookies.path", defaultFile);
        if (TextUtils.isEmpty(res))
            defaultFile = context.getApplicationContext().getFilesDir()+"/4pda_cookies";
        res = defaultFile;
        return res.replace("/", File.separator);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        addPreferencesFromResource(R.xml.preferences);
        Preference aboutAppVersion = findPreference("About.AppVersion");
        aboutAppVersion.setTitle(getProgramFullName(this));
        aboutAppVersion.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                showAbout();
                return true;
            }
        });

//        EditTextPreference editTextPreference = (EditTextPreference) findPreference("Additional.CookiesFile");
//        if (TextUtils.isEmpty(editTextPreference.getEditText().getText()))
//            editTextPreference.setText(getCookieFilePath(this));
//        editTextPreference.setSummary(getCookieFilePath(this));
//        editTextPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
//            public boolean onPreferenceChange(Preference preference, Object o) {
//                try {
//                    String filePath = ((EditTextPreference) preference).getText().replace("/", File.separator);
//                    File file = new File(filePath);
//
//
//                    if (!Functions.mkDirs(file.getAbsolutePath()))
//                        throw new NotReportException("Не могу создать указанную директорию!");
//                    if (!file.createNewFile() && !file.exists())
//                        throw new NotReportException("Не могу создать указанный файл!");
//
//                } catch (Exception ex) {
//                    String text = ex.getMessage();
//                    if (TextUtils.isEmpty(text))
//                        text = ex.toString();
//                    Log.e(PreferencesActivity.this, new NotReportException(text));
//                    return false;
//                }
//                Toast.makeText(PreferencesActivity.this, "Файл успешно создан!", Toast.LENGTH_SHORT).show();
//                return true;
//
//            }
//        });

        findPreference("About.History").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                StringBuilder sb = new StringBuilder();
                try {

                    BufferedReader br = new BufferedReader(new InputStreamReader(getAssets().open("history.txt"), "UTF-8"));
                    String line;
                    while ((line = br.readLine()) != null) {
                        sb.append(line + "\n");
                    }

                } catch (IOException e) {
                    Log.e(PreferencesActivity.this, e);
                }
                AlertDialog dialog = new AlertDialog.Builder(PreferencesActivity.this)
                        .setIcon(R.drawable.icon)
                        .setTitle("История изменений")
                        .setMessage(sb)
                        .setPositiveButton(android.R.string.ok, null)
                        .create();
                dialog.show();
                TextView textView = (TextView) dialog.findViewById(android.R.id.message);
                textView.setTextSize(12);
                return true;
            }
        });

        findPreference("About.ShareIt").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                Intent sendMailIntent = new Intent(Intent.ACTION_SEND);
                sendMailIntent.putExtra(Intent.EXTRA_SUBJECT, "Рекомендую установить программу 4pda");
                sendMailIntent.putExtra(Intent.EXTRA_TEXT, "Привет, я использую 4pda, это первый android-клиент для лучшего сайта о мобильных устройствах 4PDA." +
                        "Ты можешь найти его через поиск в Android Market по слову \"4pda\" или жми на ссылку http://goo.gl/jJp6m");
                sendMailIntent.setType("text/plain");

                startActivity(Intent.createChooser(sendMailIntent, "Отправить через..."));
                return true;
            }
        });

        findPreference("About.SendFeedback").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                Intent marketIntent = new Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("http://market.android.com/details?id=" + getPackageName()));
                PreferencesActivity.this.startActivity(marketIntent);
                return true;
            }
        });

        findPreference("About.AddRep").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                if (!Client.INSTANCE.getLogined()) {
                    Toast.makeText(PreferencesActivity.this, "Необходимо залогиниться!", Toast.LENGTH_SHORT).show();
                    return true;
                }
                ForumUser.startChangeRep(PreferencesActivity.this, mHandler, "236113", "slartus", "0", "add", "Поднять репутацию");
                return true;
            }
        });

        findPreference("About.ShowTheme").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent(PreferencesActivity.this, ThemeActivity.class);
                intent.putExtra("ThemeUrl", "271502");

                PreferencesActivity.this.startActivity(intent);
                return true;
            }
        });

        findPreference("appstyle").setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object o) {
                Toast.makeText(PreferencesActivity.this, "Необходимо перезапустить программу для применения темы!", Toast.LENGTH_SHORT).show();
                return true;
            }
        });

        Preference downloadsPathPreference=findPreference("downloads.path");
        downloadsPathPreference.setSummary(DownloadsService.getDownloadDir(getApplicationContext()));
        ((EditTextPreference)downloadsPathPreference)
                .setText(DownloadsService.getDownloadDir(getApplicationContext()));
        downloadsPathPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object o) {
                try{
                    String dirPath=o.toString();
                    if(!dirPath.endsWith(File.separator))
                        dirPath+=File.separator;
                    File dir=new File(dirPath);
                    File file=new File(FileUtils.getUniqueFilePath(dirPath,"4pda.tmp"));
                    
                    if(!dir.exists()&&!dir.mkdirs())
                        throw new NotReportException("Не удалось создать папку по указанному пути");

                    if(!file.createNewFile())
                        throw new NotReportException("Не удалось создать файл по указанному пути");
                    file.delete();
                    return true;
                }catch (Exception ex){
                    Log.e(PreferencesActivity.this, new NotReportException(ex.toString()));
                }
                return false;
            }
        });

        findPreference("Yandex.money").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                StringUtils.copyToClipboard(PreferencesActivity.this, preference.getSummary().toString());
                Toast.makeText(PreferencesActivity.this, "Номер счёта скопирован в буфер обмена", Toast.LENGTH_SHORT).show();
                return true;
            }
        });

        findPreference("WebMoney.money").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                StringUtils.copyToClipboard(PreferencesActivity.this, preference.getSummary().toString());
                Toast.makeText(PreferencesActivity.this, "Номера кошельков скопированы в буфер обмена", Toast.LENGTH_SHORT).show();
                return true;
            }
        });

        findPreference("Paypal.money").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                StringUtils.copyToClipboard(PreferencesActivity.this, preference.getSummary().toString());
                Toast.makeText(PreferencesActivity.this, "Адрес получателя скопирован в буфер обмена", Toast.LENGTH_SHORT).show();
                return true;
            }
        });

        try {
            setTabsThemeActionText();

            Tabs.configTabsData(this);
        } catch (Exception ex) {
            Log.e(this, ex);
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        try {
            setTabsThemeActionText();

            Tabs.configTabsData(this);
        } catch (Exception ex) {
            Log.e(this, ex);
        }
    }

    private void setTabsThemeActionText() {
        ArrayList<String> values = new ArrayList();
        Collections.addAll(values, getResources().getStringArray(R.array.ThemeActionsValues));

        ArrayList<String> captions = new ArrayList();
        Collections.addAll(captions, getResources().getStringArray(R.array.ThemeActionsArray));

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        for (int i = 1; i <= getResources().getStringArray(R.array.tabsArray).length; i++) {
            setTabThemeActionText(prefs, "Tab" + i, captions, values);
        }
    }

    private void setTabThemeActionText(SharedPreferences prefs, String tabId
            , ArrayList<String> captions, ArrayList<String> values) {
        String action = prefs.getString("tabs." + tabId + ".Action", "getlastpost");
        if (TextUtils.isEmpty(action))
            action = "getlastpost";

        findPreference("tabs." + tabId + ".Action").setSummary(captions.get(values.indexOf(action)));

    }

    private void showAbout() {

        String text = "Неофициальный клиент для сайта <a href=\"http://www.4pda.ru\">4pda.ru</a><br/><br/>\n" +
                "<b>Автор: </b> Артём Слинкин aka slartus<br/>\n" +
                "<b>E-mail:</b> <a href=\"mailto:4pda.android@gmail.com\">4pda.android@gmail.com</a><br/><br/>\n" +
                "<b>Благодарности: </b> <br/>\n" +
                "* <b><a href=\"http://4pda.ru/forum/index.php?showuser=474658\">zlodey.82</a></b> иконка программы<br/>\n" +
                "* <b><a href=\"http://4pda.ru/forum/index.php?showuser=680839\">SPIDER3220</a></b> (иконки, баннеры)<br/>\n" +
                "* <b><a href=\"http://4pda.ru/forum/index.php?showuser=1392892\">ssmax2015</a></b> (иконки, баннеры)<br/>\n" +
                "* <b><a href=\"http://4pda.ru/forum/index.php?showuser=2523\">e202</a></b> (иконки сообщения для черной темы)<br/>\n" +
                "* <b><a href=\"http://4pda.ru/forum/index.php?showuser=2040700\">Remie-l</a></b> (новые стили для топиков)<br/>\n" +
                "* <b><a href=\"http://www.4pda.ru\">пользователям 4pda</a></b> (тестирование, идеи, поддержка)\n" +
                "<br/>";

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setIcon(R.drawable.icon)
                .setTitle(getProgramFullName(this))
                .setMessage(Html.fromHtml(text))
                .setPositiveButton(android.R.string.ok, null)
                .create();
        dialog.show();
        TextView textView = (TextView) dialog.findViewById(android.R.id.message);
        textView.setTextSize(12);

        textView.setMovementMethod(LinkMovementMethod.getInstance());
    }

    public static String getProgramFullName(Context context) {
        String programName = context.getString(R.string.app_name);
        try {
            String packageName = context.getPackageName();
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(
                    packageName, PackageManager.GET_META_DATA);

            programName += " v" + pInfo.versionName;

        } catch (PackageManager.NameNotFoundException e1) {
            Log.e(context, e1);
        }
        return programName;
    }


}
