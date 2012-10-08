package org.softeg.slartus.forpda;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.os.ResultReceiver;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import org.apache.http.HttpEntity;
import org.softeg.slartus.forpda.classes.DownloadTask;
import org.softeg.slartus.forpda.classes.common.FileUtils;
import org.softeg.slartus.forpda.common.Log;
import org.softeg.slartus.forpdaapi.NotReportException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * User: slinkin
 * Date: 16.07.12
 * Time: 9:58
 */
public class DownloadsService extends IntentService {

    public static final String DOWNLOAD_FILE_ID_KEY = "DownloadFileIdKey";

    public static final String DOWNLOAD_FILE_TEMP_NAME_KEY = "DownloadFileTempNameKey";

    public static final int UPDATE_PROGRESS = 8344;

    public DownloadsService() {
        super("DownloadsService");
    }

    public static String getDownloadDir(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString("downloads.path", getDefaultDownloadPath());
    }

    public static String getDefaultDownloadPath() {
        return Environment.getExternalStorageDirectory() + "/download/4pda/".replace("/", File.separator);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        int notificationId = intent.getExtras().getInt(DOWNLOAD_FILE_ID_KEY);
        ResultReceiver receiver = (ResultReceiver) intent.getParcelableExtra("receiver");
        String tempFilePath = intent.getStringExtra(DOWNLOAD_FILE_TEMP_NAME_KEY);
        downloadFile(receiver, notificationId, tempFilePath);
    }


    public void downloadFile(ResultReceiver receiver, int notificationId, String tempFilePath) {
        DownloadTask downloadTask =null;
        try {

            String dirPath = getDownloadDir(getApplicationContext());
             downloadTask = Client.INSTANCE.getDownloadTasks().getById(notificationId);

            if (downloadTask.getState() == DownloadTask.STATE_CANCELED) {
                return;
            }

            String url = downloadTask.getUrl();

            url= FileUtils.getDirPath(url)+"/"+ URLEncoder.encode(FileUtils.getFileNameFromUrl(url)) ;
            HttpHelper httpHelper = new HttpHelper();

            try {

                String fileName = TextUtils.isEmpty(tempFilePath) ? FileUtils.getFileNameFromUrl(url) : FileUtils.getFileNameFromUrl(tempFilePath.replace("_download", ""));
                String saveDir = dirPath;

                String filePath =  TextUtils.isEmpty(tempFilePath) ? FileUtils.getUniqueFilePath(saveDir, fileName): FileUtils.combine(saveDir,fileName);
                downloadTask.setOutputFile(filePath);
                String downloadingFilePath = filePath + "_download";
                downloadTask.setDownloadingFilePath(downloadingFilePath);


                FileUtils.mkDirs(downloadingFilePath);
                // new File(downloadingFilePath).createNewFile();

                long total = TextUtils.isEmpty(tempFilePath) ? 0 : DownloadTask.getRange(tempFilePath);

                url=FileUtils.getDirPath(url)+"/"+ URLEncoder.encode(FileUtils.getFileNameFromUrl(url)) ;
                HttpEntity entity = httpHelper.getDownloadResponse(url, total);

                long fileLength = entity.getContentLength() + total;

                downloadTask.setProgressState(total, fileLength);
                sendDownloadProgressState(receiver, notificationId);


                int count;
                int percent = 0;
                int prevPercent = 0;

                Date lastUpdateTime = new Date();
                Boolean first = true;

                InputStream in = entity.getContent();
                FileOutputStream output = new FileOutputStream(downloadingFilePath, true);

                byte data[] = new byte[1024];
                try {
                    while ((count = in.read(data)) != -1) {
                        if (downloadTask.getState() == DownloadTask.STATE_CANCELED) {
                            sendDownloadProgressState(receiver, notificationId);
                            return;
                        }

                        output.write(data, 0, count);
                        total += count;

                        percent = (int) ((float) total / fileLength * 100);

                        long diffInMs = new Date().getTime() - lastUpdateTime.getTime();
                        long diffInSec = TimeUnit.MILLISECONDS.toSeconds(diffInMs);

                        if ((percent != prevPercent && diffInSec > 1) || first) {
                            lastUpdateTime = new Date();
                            downloadTask.setProgressState(total, fileLength);
                            sendDownloadProgressState(receiver, notificationId);
                            first = false;
                        }
                        prevPercent = percent;
                    }
                    downloadTask.setProgressState(fileLength, fileLength);
                    sendDownloadProgressState(receiver, notificationId);
                } finally {
                    output.flush();
                    output.close();
                    in.close();
                }
                File downloadingFile = new File(downloadingFilePath);
                File downloadedFile = new File(filePath);
                if (!downloadingFile.renameTo(downloadedFile)) {
                    throw new NotReportException("Не могу переименовать файл: " + downloadingFilePath + " в " + filePath);
                }
                downloadTask.setState(downloadTask.STATE_SUCCESSFULL);
                sendDownloadProgressState(receiver, notificationId);

            } finally {
                httpHelper.close();
            }
        } catch (Exception ex) {
            if(downloadTask!=null){
                downloadTask.setEx(ex);
                downloadTask.setState(downloadTask.STATE_ERROR);
                sendDownloadProgressState(receiver, notificationId);
            }

            Log.e(null, ex);
        }


    }

    public static void sendDownloadProgressState(ResultReceiver receiver, int downloadTaskId) {
        Bundle resultData = new Bundle();
        resultData.putInt("downloadTaskId", downloadTaskId);
        receiver.send(UPDATE_PROGRESS, resultData);
    }

}
