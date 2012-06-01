package org.softeg.slartus.forpda.common;


import android.R;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.text.TextUtils;
import android.widget.Toast;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.HttpHostConnectException;
import org.softeg.slartus.forpda.MyApp;
import org.softeg.slartus.forpda.classes.Exceptions.AdditionalInfoException;
import org.softeg.slartus.forpda.classes.Exceptions.NotReportException;

import java.io.*;
import java.lang.reflect.Field;

public final class Log {
    public static final String EMAIL="slartus+4pda@gmail.com";
    public static final String EMAIL_SUBJECT="4pdaClient: Отчёт об ошибке";
    public static String TAG = "org.softeg.slartus.forpda.LOG";

    public static void i(Context context, Exception ex) {
        android.util.Log.i(TAG, getLocation() + ex);
        if (ex.getClass() == java.net.UnknownHostException.class ||
                ex.getClass() == HttpHostConnectException.class ||
                ex.getClass() == ClientProtocolException.class) {
            new AlertDialog.Builder(context)
                    .setTitle("Проверьте подключение")
                    .setMessage("Сервер недоступен")
                    .setPositiveButton("ОК", null)
                    .create().show();
        } else if (ex.getClass() == ConnectTimeoutException.class) {
            new AlertDialog.Builder(context)
                    .setTitle("Проверьте подключение")
                    .setMessage("Превышен таймаут ожидания")
                    .setPositiveButton("ОК", null)
                    .create().show();
        } else
            Toast.makeText(context, ex.getMessage(), Toast.LENGTH_SHORT).show();
    }

    public static void d(String msg) {
        if (MyApp.getIsDebugMode())
            android.util.Log.d(TAG, getLocation() + msg);
    }

    public static void e(String msg) {
        android.util.Log.e(TAG, getLocation() + msg);
    }

    public static void w(String msg) {
        android.util.Log.w(TAG, getLocation() + msg);
    }

    public static void e(Context context, Exception ex) {
        e(context, ex, true);
    }

    public static void e(Context context, String message, Throwable ex) {
        e(context, message, ex, true);
    }

    public static void e(Context context, Exception ex, Boolean sendReport) {
        e(context, ex.getMessage(), ex, sendReport);
    }

    public static void e(Context context, String message, Throwable ex, Boolean sendReport) {
        if (ex.getClass() == java.net.UnknownHostException.class ||
                ex.getClass() == HttpHostConnectException.class ||
                ex.getClass() == ClientProtocolException.class) {
            new AlertDialog.Builder(context)
                    .setTitle("Проверьте подключение")
                    .setMessage("Сервер недоступен")
                    .setPositiveButton("ОК", null)
                    .create().show();
            return;
        } else if (ex.getClass() == ConnectTimeoutException.class) {
            new AlertDialog.Builder(context)
                    .setTitle("Проверьте подключение")
                    .setMessage("Превышен таймаут ожидания")
                    .setPositiveButton("ОК", null)
                    .create().show();
            return;
        }
        
        android.util.Log.e(TAG, getLocation() + ex);
        if (TextUtils.isEmpty(message))
            message = ex.getMessage();
        if (TextUtils.isEmpty(message))
            message = ex.toString();
        if (context == null) {
//            context = MyApp.getContext();
//            if (context != null)
//                Toast.makeText(context, "4PDA_" + ex.getMessage() + ": " + ex.toString(),
//                        Toast.LENGTH_LONG).show();
            return;
        }
        try {
            if (ex.getClass() == NotReportException.class) {
                new AlertDialog.Builder(context)
                        .setTitle("Ошибка")
                        .setMessage(message)
                        .setPositiveButton("ОК", null)
                        .create().show();
            } else if (sendReport) {
                sendReportDialog(context, message, message + "\n " + getLocation() + ex, ex);
            }
        } catch (Exception e) {
            e(null, e, false);
        }

    }

    private static void sendReportDialog(final Context context, final String message, final String fullExceptionText, final Throwable ex) {
        try {

            new AlertDialog.Builder(context)
                    .setTitle("Ошибка")
                    .setMessage(message)
                    .setIcon(R.drawable.ic_dialog_alert)

                    .setPositiveButton("Послать отчёт", new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialogInterface, int i) {
                            sendReport(context, fullExceptionText, ex);
                        }
                    })
                    .setNegativeButton(R.string.cancel, null)
                    .create()
                    .show();
        } catch (Exception e) {
            e(null, e, false);
        }
    }
    
    public static void sendMail(final Handler handler,final Context context, final String theme, final String body, final String attachText){


        Thread th = new Thread(new Runnable() {
            public void run() {
                final Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_EMAIL, new String[]{EMAIL});
                intent.putExtra(Intent.EXTRA_SUBJECT, theme);
                intent.putExtra(Intent.EXTRA_TEXT, body.toString());
                intent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + createLogFile(context,attachText)));
                handler.post(new Runnable() {
                    public void run() {
                        context.startActivity(Intent.createChooser(intent, "Отправка сообщения"));
                    }
                });
            }
        });
        th.start();
    }

    public static void sendReport(final Context context, final String fullExceptionText, final Throwable ex) {
        final Handler transThreadHandler = new Handler();

        Thread th = new Thread(new Runnable() {
            public void run() {
                final Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_EMAIL, new String[]{EMAIL});
                intent.putExtra(Intent.EXTRA_SUBJECT, EMAIL_SUBJECT);
                StringBuffer body = purchaseOrder(context, fullExceptionText);

                String addFileBody = null;
                if (ex.getClass() == AdditionalInfoException.class) {
                    AdditionalInfoException additionalInfoException = (AdditionalInfoException) ex;
                    Bundle args = additionalInfoException.getArgs();

                    Boolean addStarted = false;
                    for (String key : args.keySet()) {
                        if (AdditionalInfoException.ARG_ATTACH_BODY.equals(key)) {
                            addFileBody = "**" + key + "**\n" + args.getString(key);
                            continue;
                        }
                        if (!addStarted) {
                            body.append("**Дополнительные сведения**").append('\n');
                            addStarted = true;
                        }
                        body.append(key + ": " + args.get(key)).append('\n');
                    }
                }

                String logCatPath = createLogFile(context,addFileBody);

                intent.putExtra(Intent.EXTRA_TEXT, body.toString());
                intent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + logCatPath));
                transThreadHandler.post(new Runnable() {
                    public void run() {
                        context.startActivity(Intent.createChooser(intent, "sending mail"));
                    }
                });
            }
        });
        th.start();
    }

    private static StringBuffer purchaseOrder(Context context, final String fullExceptionText) {
        StringBuffer sb = new StringBuffer();
        String packageName = context.getPackageName();
        String version = "unknown";
        try {
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(
                    packageName, PackageManager.GET_META_DATA);
            version = pInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e(context, e, false);
        }


        sb.append("**Опишите действия, приведшие к ошибке**").append('\n')
                .append('\n')
                .append('\n')
                .append("**Приложение**").append('\n')
                .append("app.package=").append(packageName).append('\n')
                .append("app.version=").append(version).append('\n')
                .append('\n')
                .append("**Устройство**").append('\n')
                .append("Build.ID=").append(Build.ID).append('\n')
                .append("Build.DISPLAY=").append(Build.DISPLAY).append('\n')
                .append("Build.PRODUCT=").append(Build.PRODUCT).append('\n')
                .append("Build.DEVICE=").append(Build.DEVICE).append('\n')
                .append("Build.BOARD=").append(Build.BOARD).append('\n')
                .append("Build.MANUFACTURER=").append(Build.MANUFACTURER).append('\n')
                .append("Build.BRAND=").append(Build.BRAND).append('\n')
                .append("Build.MODEL=").append(Build.MODEL).append('\n')
                .append("Build.HARDWARE=").append(Build.HARDWARE).append('\n')
                .append("Build.TYPE=").append(Build.TYPE).append('\n')
                .append("Build.VERSION.INCREMENTAL=").append(Build.VERSION.INCREMENTAL).append('\n')
                .append("Build.VERSION.RELEASE=").append(Build.VERSION.RELEASE).append('\n')
                .append("Build.VERSION.SDK=").append(Build.VERSION.SDK).append('\n')
                .append("Build.VERSION.CODENAME=").append(Build.VERSION.CODENAME).append('\n')
        ;

        sb.append("**Ошибка**").append('\n').append(fullExceptionText).append('\n');
        return sb;
    }


    static int getAPILevel() {
        int apiLevel;
        try {
            // This field has been added in Android 1.6
            Field SDK_INT = Build.VERSION.class.getField("SDK_INT");
            apiLevel = SDK_INT.getInt(null);
        } catch (SecurityException e) {
            apiLevel = Integer.parseInt(Build.VERSION.SDK);
        } catch (NoSuchFieldException e) {
            apiLevel = Integer.parseInt(Build.VERSION.SDK);
        } catch (IllegalArgumentException e) {
            apiLevel = Integer.parseInt(Build.VERSION.SDK);
        } catch (IllegalAccessException e) {
            apiLevel = Integer.parseInt(Build.VERSION.SDK);
        }

        return apiLevel;
    }

    public static String createLogFile(Context context, String addInfo) {

        try {
            String filePath = Environment.getExternalStorageDirectory() + "/logcat.txt";
            FileWriter fw = new FileWriter(filePath);
            //copyPrefsOnSd(context, fw);
            try {
                fw.write("**LOGCAT**\n\n");


                String[] logcatArguments = {"logcat", "-d", "-v", "time"};


                Process process = Runtime.getRuntime().exec(logcatArguments);
                BufferedReader bufferedReader = new BufferedReader(
                        new InputStreamReader(process.getInputStream()));

                String line;
                int linesCount = 300;

                while ((line = bufferedReader.readLine()) != null) {
                    //  fos.write(line.getBytes());
                    fw.append(line).append('\n');

                    linesCount--;
                    if (linesCount == 0) break;
                }

                if (addInfo != null)
                    fw.write(addInfo);

            } finally {
                fw.close();
            }


            return filePath;
        } catch (Exception e) {
            Log.e(context, e, false);
            return e.getMessage();
        }


    }

    private static String getLocation() {
        final String className = Log.class.getName();
        final StackTraceElement[] traces = Thread.currentThread()
                .getStackTrace();
        boolean found = false;

        for (int i = 0; i < traces.length; i++) {
            StackTraceElement trace = traces[i];

            try {
                if (found) {
                    if (!trace.getClassName().startsWith(className)) {
                        Class<?> clazz = Class.forName(trace.getClassName());
                        return "[" + getClassName(clazz) + ":"
                                + trace.getMethodName() + ":"
                                + trace.getLineNumber() + "]: ";
                    }
                } else if (trace.getClassName().startsWith(className)) {
                    found = true;
                    continue;
                }
            } catch (ClassNotFoundException e) {
            }
        }

        return "[]: ";
    }

    private static void copyPrefsOnSd(Context context, FileWriter fw) {
        try {

            fw.write("**PREFERENCES**\n");
            File sharedPrefsDir = new File(context.getFilesDir(), "../shared_prefs");


            String packageName = context.getPackageName();
            File inputFile = new File(sharedPrefsDir, packageName + "_preferences.xml");


            FileReader input = new FileReader(inputFile);
            BufferedReader bufferedReader = new BufferedReader(
                    new FileReader(inputFile));

            String line;
            while ((line = bufferedReader.readLine()) != null) {
                fw.append(line).append('\n');
            }
            input.close();


        } catch (Exception ex) {
            e(context, ex, false);
        }

    }


    private static String getClassName(Class<?> clazz) {
        if (clazz != null) {
            if (!TextUtils.isEmpty(clazz.getSimpleName())) {
                return clazz.getSimpleName();
            }

            return getClassName(clazz.getEnclosingClass());
        }

        return "";
    }

    public static void v(String tableName, String message, Exception e) {
        android.util.Log.e(TAG, getLocation() + " table: " + tableName
                + message + e);

    }


}
