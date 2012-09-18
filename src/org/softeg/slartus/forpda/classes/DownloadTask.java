package org.softeg.slartus.forpda.classes;

import android.content.Context;
import android.text.TextUtils;
import org.softeg.slartus.forpda.Client;
import org.softeg.slartus.forpda.classes.common.FileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;

/**
 * User: slinkin
 * Date: 12.10.11
 * Time: 11:12
 */
public class DownloadTask {

    public static final int STATE_PENDING = 5;
    public static final int STATE_CONNECTING = 0;
    public static final int STATE_DOWNLOADING = 1;
    public static final int STATE_SUCCESSFULL = 2;
    public static final int STATE_ERROR = 3;
    public static final int STATE_CANCELED = 4;

    private ArrayList<Client.OnProgressPositionChangedListener> m_OnStateListeners = new ArrayList<Client.OnProgressPositionChangedListener>();
    private String m_Url;
    private String outputFile;
    private int m_State = STATE_PENDING;
    private Exception ex;
    private int m_Percents;
    private long downloadedSize;
    private long contentLength;
    private Date m_CreateDate;
     private Date m_StateChangedDate;
    private int m_NotificationId;
    private long m_Range;
    private String m_DownloadingFilePath;

    public DownloadTask(String url,int notificationId) {
        m_Url = url;
        m_NotificationId=notificationId;
        m_CreateDate=new Date();
        m_StateChangedDate=new Date();
    }
    
    public int getId(){
        return m_NotificationId;
    }

    public String getOutputFile() {
        return outputFile;
    }

    public void setOutputFile(String outputFile) {
        this.outputFile = outputFile;
    }

    public int getState() {
        return m_State;
    }

    public String getStateMessage(){
        return  getStateMessage(m_State,ex);
    }

    public static String getStateMessage(int state, Exception downloadTaskException) {
        switch (state) {
            case STATE_PENDING:
            case STATE_CONNECTING:
                return "Подключение";
            case STATE_DOWNLOADING:
                return "Загрузка";
            case STATE_SUCCESSFULL:
                return "Загрузка завершена";
            case STATE_CANCELED:
                return "Загрузка отменена";
            case STATE_ERROR:
                return "Ошибка загрузки: " + (downloadTaskException==null?"Неизвестная ошибка":downloadTaskException.getMessage());
        }
        return "Неизвестно";
    }

    public void setState(int state) {
        this.m_State = state;
        m_StateChangedDate=new Date();
        doStateChanged();
    }

    public void setEx(Exception ex) {
        m_State = STATE_ERROR;
        this.ex = ex;
    }
    
    public Exception getEx(){
        return ex;
    }

    public void addStateListener(Client.OnProgressPositionChangedListener stateListener) {
        if (stateListener != null) {
            m_OnStateListeners.add(stateListener);
        }
    }

    private Context m_Context;

    public Context getContext() {
        return m_Context;
    }

    public void setContext(Context context) {
        this.m_Context = context;

    }

    private void doStateChanged() {
        for (Client.OnProgressPositionChangedListener stateListener : m_OnStateListeners) {
            stateListener.onProgressChanged(m_Context, this, ex);
        }

    }

    public void setProgressState( long downloadedSize, long contentLength) {
        m_Percents = (int) ((float) downloadedSize / contentLength * 100);
        this.downloadedSize = downloadedSize;
        this.contentLength = contentLength;
        setState(STATE_DOWNLOADING);
    }

    public String getUrl() {
        return m_Url;
    }

    public String getFileName() {
        if(TextUtils.isEmpty(outputFile) )
            return FileUtils.getFileNameFromUrl( m_Url);
        return FileUtils.getFileNameFromUrl(outputFile);
    }

    public int getPercents() {
        return m_Percents;
    }

    public long getContentLength() {
        return contentLength;
    }

    public void setContentLength(long value) {
        contentLength=value;
    }


    public long getDownloadedSize() {
        return downloadedSize;
    }

    public void cancel() {
        if (m_State == STATE_CONNECTING || m_State == STATE_DOWNLOADING||m_State==STATE_PENDING) {
            m_State = STATE_CANCELED;
            doStateChanged();
        }
    }

    public Date getCreateDate() {
        return m_CreateDate;
    }

    public Date getStateChangedDate() {
        return m_StateChangedDate;
    }

    public void setStateChangedDate(Date m_StateChangedDate) {
        this.m_StateChangedDate = m_StateChangedDate;
    }

    public static long getRange(String filePath) {
        File file=new File(filePath);
        if(!file.exists())
            return 0;
        return file.length();
    }

    public void setDownloadingFilePath(String downloadingFilePath) {
        m_DownloadingFilePath=downloadingFilePath;
    }

    public String getDownloadingFilePath() {
        return m_DownloadingFilePath;
    }
}
