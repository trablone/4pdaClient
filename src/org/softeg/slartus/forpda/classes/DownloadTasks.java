package org.softeg.slartus.forpda.classes;

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

    public DownloadTask add(String url, Client.OnProgressPositionChangedListener progressChangedListener) {
        DownloadTask downloadTask = new DownloadTask(url);
        downloadTask.addStateListener(progressChangedListener);

        downloadTask.addStateListener(new Client.OnProgressPositionChangedListener() {
            public void onProgressChanged(int state, Exception ex) {
                if (m_StateListener != null)
                    m_StateListener.onProgressChanged(state,ex );
            }
        });
        add(downloadTask);
        return downloadTask;
    }
}
