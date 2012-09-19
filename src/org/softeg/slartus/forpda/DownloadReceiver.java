package org.softeg.slartus.forpda;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.webkit.MimeTypeMap;
import android.widget.RemoteViews;
import android.widget.Toast;
import org.softeg.slartus.forpda.classes.DownloadTask;
import org.softeg.slartus.forpda.classes.common.FileUtils;

/**
 * User: slinkin
 * Date: 30.07.12
 * Time: 10:31
 */
public class DownloadReceiver extends ResultReceiver {
    private Handler m_Handler;
    private Context m_Context;
    public DownloadReceiver(Handler handler, Context context) {
        super(handler);
        m_Handler=handler;
        m_Context=context;
    }

    @Override
    protected void onReceiveResult(int resultCode, Bundle resultData) {
        super.onReceiveResult(resultCode, resultData);
        if (resultCode != DownloadsService.UPDATE_PROGRESS) return;
        int notificationId=resultData.getInt("downloadTaskId");

        final DownloadTask downloadTask=Client.INSTANCE.getDownloadTasks().getById(notificationId);


        final Context context=m_Context;
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        switch (downloadTask.getState()) {
            case DownloadTask.STATE_ERROR:
            case DownloadTask.STATE_CANCELED: {
                Intent intent = new Intent(context, DownloadsActivity.class);

                Notification notification = new Notification(R.drawable.icon, "Загрузка прервана", System.currentTimeMillis());
                PendingIntent contentIntent = PendingIntent.getActivity(context, 0, intent, 0);

                notification.setLatestEventInfo(context, downloadTask.getFileName(), DownloadTask.getStateMessage(downloadTask.getState(), downloadTask.getEx()),
                        contentIntent);
                notification.flags = Notification.FLAG_AUTO_CANCEL;
                mNotificationManager.notify(downloadTask.getUrl(), notificationId, notification);
                return;
            }
            case DownloadTask.STATE_SUCCESSFULL: {
                Intent intent = getRunFileIntent(downloadTask.getOutputFile());

                Notification notification = new Notification(R.drawable.icon, "Загрузка завершена", System.currentTimeMillis());
                PendingIntent contentIntent = PendingIntent.getActivity(context, 0, intent, 0);
                notification.setLatestEventInfo(context, downloadTask.getFileName(), DownloadTask.getStateMessage(downloadTask.getState(), downloadTask.getEx()),
                        contentIntent);
                notification.flags = Notification.FLAG_AUTO_CANCEL;
                mNotificationManager.notify(downloadTask.getUrl(), notificationId, notification);

                m_Handler.post(new Runnable() {
                    public void run() {
                        Toast.makeText(context, downloadTask.getFileName() + "\nЗагрузка завершена", Toast.LENGTH_SHORT).show();
                    }
                });
                return;
            }
            default: {
                Notification notification = new Notification(R.drawable.icon, "Скачивание файла", System.currentTimeMillis());
                final RemoteViews notification_view = new RemoteViews(context.getPackageName(), R.layout.download_task_notification);
                notification_view.setImageViewResource(R.id.imgIcon, R.drawable.icon);
                notification_view.setTextViewText(R.id.txtFileName, downloadTask.getFileName());
                notification_view.setProgressBar(R.id.progress, 100, 0, true);
                notification.contentView = notification_view;
                notification.flags = Notification.FLAG_AUTO_CANCEL;

                Intent newIntent = new Intent(context,
                        DownloadsActivity.class);
                PendingIntent contentIntent = PendingIntent.getActivity(context, 0, newIntent, 0);
                notification.contentIntent = contentIntent;

                notification_view.setProgressBar(R.id.progress, 100, downloadTask.getPercents(), false);
                mNotificationManager.notify(downloadTask.getUrl(), notificationId, notification);
            }
        }
    }

    private Intent getRunFileIntent(String filePath) {
        MimeTypeMap myMime = MimeTypeMap.getSingleton();
        Intent newIntent = new Intent(Intent.ACTION_VIEW);
        String mimeType = myMime.getMimeTypeFromExtension(FileUtils.fileExt(filePath).substring(1));
        newIntent.setDataAndType(Uri.parse("file://" + filePath), mimeType);
        newIntent.setFlags(newIntent.FLAG_ACTIVITY_NEW_TASK);
        return newIntent;
    }
}
