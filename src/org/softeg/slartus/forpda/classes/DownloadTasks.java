package org.softeg.slartus.forpda.classes;

import android.content.Context;
import org.softeg.slartus.forpda.Client;

import java.util.ArrayList;

/**
 * User: slinkin
 * Date: 12.10.11
 * Time: 11:16
 */
public class DownloadTasks extends ArrayList<DownloadTask> {
    Client.OnProgressPositionChangedListener m_StateListener;

    public void setOnStateListener(Client.OnProgressPositionChangedListener stateListener) {
        m_StateListener = stateListener;
    }
    
    private Context m_Context;

    public Context getContext() {
        return m_Context;
    }

    public void setContext(Context context) {
        this.m_Context = context;
        for(int i=0;i<this.size();i++){
            this.get(i).setContext(m_Context);
        }
    }

    public DownloadTask getById(int id){
        for(int i=0;i<this.size();i++){
            if(this.get(i).getId()==id)
                return this.get(i);
        }
        return null;
    }

    public DownloadTask getByUrl(String url){
        for(int i=0;i<this.size();i++){
            if(this.get(i).getUrl().equals(url))
                return this.get(i);
        }
        return null;
    }

    public DownloadTask getByUrl(String url, int state){
        for(int i=0;i<this.size();i++){
            if(!this.get(i).getUrl().equals(url))continue;
            if(this.get(i).getState()!=state)continue;
            return this.get(i);
        }
        return null;
    }


    public DownloadTask add(String url, int notificationId,Client.OnProgressPositionChangedListener progressChangedListener) {
        DownloadTask downloadTask = new DownloadTask(url,notificationId);

        downloadTask.addStateListener(progressChangedListener);

        downloadTask.addStateListener(new Client.OnProgressPositionChangedListener() {
            public void onProgressChanged(Context context, DownloadTask downloadTask1, Exception ex) {
                if (m_StateListener != null)
                    m_StateListener.onProgressChanged(getContext(),downloadTask1,ex );
            }
        });
        add(downloadTask);
        return downloadTask;
    }


}
