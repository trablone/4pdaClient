package org.softeg.slartus.forpda.Tabs;

import android.os.Handler;
import android.view.ContextMenu;
import android.widget.ListView;

/**
 * User: slinkin
 * Date: 27.09.11
 * Time: 16:32
 */
public interface ITab {

    Boolean onParentBackPressed();
    void refresh();
    Boolean refreshed();
    ListView getListView();
    void onCreateContextMenu(ContextMenu menu, android.view.View v, android.view.ContextMenu.ContextMenuInfo menuInfo, Handler handler);
}
