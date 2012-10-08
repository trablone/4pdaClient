package org.softeg.slartus.forpda;

import android.app.*;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.*;
import org.softeg.slartus.forpda.classes.DownloadTask;
import org.softeg.slartus.forpda.classes.common.FileUtils;
import org.softeg.slartus.forpda.classes.common.Functions;
import org.softeg.slartus.forpda.common.Log;
import org.softeg.slartus.forpdaapi.NotReportException;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;

/**
 * User: slinkin
 * Date: 12.10.11
 * Time: 11:01
 */
public class DownloadsActivity extends BaseActivity {
    ListView m_ListView;
    private android.os.Handler mHandler = new android.os.Handler();


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.downloads_list_activity);

        m_ListView = (ListView) findViewById(R.id.lstTree);
        m_Adapter = new DownloadTasksAdapter(this, R.layout.download_task_item, Client.INSTANCE.getDownloadTasks());
        m_Adapter.sort(new Comparator<DownloadTask>() {
            public int compare(DownloadTask downloadTask, DownloadTask downloadTask1) {
                return -1 * downloadTask.getCreateDate().compareTo(downloadTask1.getCreateDate());
            }
        });
        getListView().setAdapter(m_Adapter);
        getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                try {
                    if (l < 0) return;
                    final DownloadTask downloadTask = m_Adapter.getItem((int) l);
                    switch (downloadTask.getState()) {
                        case DownloadTask.STATE_PENDING:
                        case DownloadTask.STATE_CONNECTING:
                        case DownloadTask.STATE_DOWNLOADING:
                            new AlertDialog.Builder(DownloadsActivity.this)
                                    .setTitle("Действие")
                                    .setMessage("Отменить загрузку?")
                                    .setCancelable(true)
                                    .setPositiveButton("Да", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            downloadTask.cancel();
                                            dialogInterface.dismiss();
                                        }
                                    })
                                    .setNegativeButton("Нет", null)
                                    .create().show();
                            break;
                        case DownloadTask.STATE_ERROR:
                        case DownloadTask.STATE_CANCELED:
                            CharSequence[] items=new CharSequence[]{"Повторить загрузку", "Докачать файл"};
                            new AlertDialog.Builder(DownloadsActivity.this)
                                    .setTitle("Выберите действие")
                                    .setSingleChoiceItems(items, 0, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            switch (i) {
                                                case 0: // Повторить загрузку
                                                    dialogInterface.dismiss();
                                                    Client.INSTANCE.getDownloadTasks().remove(downloadTask);
                                                    download(DownloadsActivity.this, downloadTask.getUrl());

                                                    mHandler.post(new Runnable() {
                                                        public void run() {
                                                            m_Adapter.notifyDataSetChanged();
                                                        }
                                                    });
                                                    break;
                                                case 1: // Докачать файл
                                                    dialogInterface.dismiss();
                                                    Client.INSTANCE.getDownloadTasks().remove(downloadTask);
                                                    download(DownloadsActivity.this, downloadTask.getUrl(),
                                                            downloadTask.getDownloadingFilePath(),downloadTask.getId());

                                                    mHandler.post(new Runnable() {
                                                        public void run() {
                                                            m_Adapter.notifyDataSetChanged();
                                                        }
                                                    });

                                                    break;
                                            }
                                        }
                                    })
                                    .setCancelable(true)
                                    .setNegativeButton("Отмена", null)
                                    .create().show();
                            break;
                        case DownloadTask.STATE_SUCCESSFULL:
                            runFile(downloadTask.getOutputFile());

                            break;
                    }
                } catch (Exception ex) {
                    Log.e(DownloadsActivity.this, ex);
                }


            }
        });
        Client.INSTANCE.getDownloadTasks().setOnStateListener(new Client.OnProgressPositionChangedListener() {
            public void onProgressChanged(Context context, DownloadTask downloadTask, Exception ex) {
                mHandler.post(new Runnable() {
                    public void run() {
                        m_Adapter.notifyDataSetChanged();
                    }
                });

            }
        });
    }

    private void runFile(String filePath) {
        try {

            startActivity(getRunFileIntent(filePath));
        } catch (ActivityNotFoundException e) {
            Log.e(this, new NotReportException("Не найдено сопоставление для типа файла!"));
        }
    }

    private static Intent getRunFileIntent(String filePath) {
        MimeTypeMap myMime = MimeTypeMap.getSingleton();
        Intent newIntent = new Intent(Intent.ACTION_VIEW);
        String mimeType = myMime.getMimeTypeFromExtension(FileUtils.fileExt(filePath).substring(1));
        newIntent.setDataAndType(Uri.parse("file://" + filePath), mimeType);
        newIntent.setFlags(newIntent.FLAG_ACTIVITY_NEW_TASK);
        return newIntent;
    }

    private ListView getListView() {
        return m_ListView;
    }


    @Override
    public void onStart() {
        super.onStart();

    }

    private ArrayAdapter<DownloadTask> m_Adapter;

    public class DownloadTasksAdapter extends ArrayAdapter<DownloadTask> {
        private LayoutInflater m_Inflater;

        public DownloadTasksAdapter(Context context, int textViewResourceId, ArrayList<DownloadTask> objects) {
            super(context, textViewResourceId, objects);
            m_Inflater = LayoutInflater.from(context);
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            final ViewHolder holder;

            if (convertView == null) {


                convertView = m_Inflater.inflate(R.layout.download_task_item, parent, false);


                holder = new ViewHolder();
                holder.txtFileName = (TextView) convertView
                        .findViewById(R.id.txtFileName);
                holder.txtDescription = (TextView) convertView
                        .findViewById(R.id.txtDescription);
                holder.txtResult = (TextView) convertView
                        .findViewById(R.id.txtResult);
                holder.progress = (ProgressBar) convertView
                        .findViewById(R.id.progress);

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            DownloadTask downloadTask = this.getItem(position);

            holder.txtFileName.setText(downloadTask.getFileName());
            holder.txtDescription.setText("Загружено " + downloadTask.getPercents() + "%("
                    + Functions.getSizeText(downloadTask.getDownloadedSize()) + "/"
                    + Functions.getSizeText(downloadTask.getContentLength()) + ")");
            int state = downloadTask.getState();

            Boolean processing = state == DownloadTask.STATE_CONNECTING || state == DownloadTask.STATE_DOWNLOADING
                    || state == DownloadTask.STATE_PENDING;
            holder.progress.setIndeterminate(state == downloadTask.STATE_CONNECTING);
            holder.progress.setProgress(downloadTask.getPercents());

            holder.progress.setVisibility(processing ? View.VISIBLE : View.GONE);
            holder.txtResult.setVisibility(processing ? View.GONE : View.VISIBLE);
            holder.txtResult.setText(downloadTask.getStateMessage());

            return convertView;
        }

        public class ViewHolder {
            //  TextView txtPostsCount;
            TextView txtFileName;
            TextView txtDescription;
            TextView txtResult;
            ProgressBar progress;
        }

    }

    public static void download(final Activity context1, final String url){
        download(context1,url,null,-1);
    }

    public static void download(final Activity context1, final String url, final String tempFilePath, final int notificationId) {
        final String fileName = FileUtils.getFileNameFromUrl(url);
        
        if(TextUtils.isEmpty(tempFilePath)){
            final String filePath=FileUtils.combine(DownloadsService.getDownloadDir(context1), FileUtils.getFileNameFromUrl(url)+"_download");
            final File file= new File(filePath);
            if(file.exists()){
                new AlertDialog.Builder(context1)
                        .setTitle("Внимание!")
                        .setMessage("Имеется недокачанный файл с таким же названием.\nДокачать?")
                        .setPositiveButton("Докачать",new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                                startDownload(context1, url, filePath, notificationId, fileName);
                            }
                        })
                        .setNegativeButton("Перекачать",new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                                file.delete();
                                startDownload(context1, url, null, notificationId, fileName);
                            }
                        }).create().show();
                return;
            }
        }

        startDownload(context1, url, tempFilePath, notificationId, fileName);
    }

    private static void startDownload(Context context1, String url, String tempFilePath, int notificationId, String fileName) {
        Toast.makeText(context1, "Загрузка начата", Toast.LENGTH_SHORT).show();
        if(notificationId==-1)
            notificationId = Functions.getUniqueDateInt();
        final Notification notification = new Notification(R.drawable.icon, "Скачивание файла", System.currentTimeMillis());
        final RemoteViews notification_view = new RemoteViews(context1.getPackageName(), R.layout.download_task_notification);
        notification_view.setImageViewResource(R.id.imgIcon, R.drawable.icon);
        notification_view.setTextViewText(R.id.txtFileName, fileName);
        notification_view.setProgressBar(R.id.progress, 100, 0, true);
        notification.contentView = notification_view;


        Intent newIntent = new Intent(context1,
                DownloadsActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(context1, 0, newIntent, 0);
        notification.contentIntent = contentIntent;
        notification.flags = Notification.FLAG_AUTO_CANCEL;

        NotificationManager nm = (NotificationManager) context1.getSystemService(NOTIFICATION_SERVICE);


        nm.notify(url, notificationId, notification);


        Client.INSTANCE.downloadFile(context1, url, notificationId, tempFilePath);
    }

}



