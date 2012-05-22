package org.softeg.slartus.forpda.Tabs;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.text.Html;
import android.text.TextUtils;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import org.softeg.slartus.forpda.Client;
import org.softeg.slartus.forpda.DevDbDeviceActivity;
import org.softeg.slartus.forpda.classes.Forum;
import org.softeg.slartus.forpda.classes.Topic;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * User: slinkin
 * Date: 24.11.11
 * Time: 9:56
 */
public class DevicesTab extends TreeTab {
    public DevicesTab(Context context, String tabTag) {
        super(context, tabTag);
    }

    public static final String TEMPLATE = Tabs.TAB_DEVDB;
    public static final String TITLE = "DevDB.ru";


    public String getTemplate() {
        return TEMPLATE;
    }

    @Override
    public String getTitle() {
        return TITLE;
    }

    @Override
    public Boolean onParentBackPressed() {
        if (m_CurrentItem == null || m_CurrentItem.getParent() == null) {
            return false;
        }
        m_CurrentItem.clearChildren();
        showForum(m_CurrentItem.getParent());
        return true;
    }

    @Override
    protected void loadForum(Forum forum, Client.OnProgressChangedListener progressChangedListener) throws IOException {
        switch (forum.level) {
            case 0:
                parseDevicesTypes(forum, progressChangedListener);
                break;
            case 1:
                parseDevicesBrands(forum, progressChangedListener);
                break;
            case 2:
                parseModels(forum, progressChangedListener);
                break;

        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo, Handler handler) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        if (info.id == -1) return;
        final Topic topic = m_ThemeAdapter.getItem((int) info.id);
        if (TextUtils.isEmpty(topic.getId())) return;

        menu.add("Открыть в браузере").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem menuItem) {
                Intent marketIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://devdb.ru/" + topic.getId()));
                getContext().startActivity(Intent.createChooser(marketIntent, "Выберите"));
                return true;
            }
        });
    }



    @Override
    protected void getThemes(Client.OnProgressChangedListener progressChangedListener) throws IOException {

    }

    @Override
    protected void listItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        l = ListViewMethodsBridge.getItemId(getContext(), i, l);
        if (l < 0||m_ForumsAdapter.getCount()<=l) return;
        if ("ForumsAdapter".equals(m_CurrentAdapter)) {
            Forum f = m_ForumsAdapter.getItem((int) l);
            if (f.hasChildForums())
                showForum(f);
            else
                loadForums(f);
        } else {
            Topic topic = m_ThemeAdapter.getItem((int) l);
            Intent intent = new Intent(getContext(), DevDbDeviceActivity.class);
            intent.putExtra(DevDbDeviceActivity.DEVICE_ID_KEY, topic.getId());

            getContext().startActivity(intent);

        }
    }

    public void parseDevicesTypes(Forum forum, Client.OnProgressChangedListener progressChangedListener) throws IOException {
        String pageBody = performGet("http://devdb.ru", progressChangedListener);

        Pattern pattern = Pattern.compile("<a href=\"http://devdb.ru/(.*?)/\">.*?<br /><br />(.*?)</a></p>");
        Matcher m = pattern.matcher(pageBody);
        while (m.find()) {
            Forum f = new Forum(m.group(1), Html.fromHtml(m.group(2)).toString());
            forum.addForum(f);
        }
    }

    public void parseDevicesBrands(Forum forum, Client.OnProgressChangedListener progressChangedListener) throws IOException {
        String pageBody = performGet("http://devdb.ru/" + forum.getId(), progressChangedListener);

        Pattern pattern = Pattern.compile("<li><a href=\"http://devdb.ru/(.*?)\">(.*?)</a></li>");
        Matcher m = pattern.matcher(pageBody);
        while (m.find()) {
            Forum f = new Forum(m.group(1), m.group(2));
            forum.addForum(f);
        }
    }

    public void parseModels(Forum forum, Client.OnProgressChangedListener progressChangedListener) throws IOException {
        String pageBody = performGet("http://devdb.ru/" + forum.getId(), progressChangedListener);

        Pattern pattern = Pattern.compile("<li><a href=\"http://devdb.ru/(.*?)\">(.*?)</a></li>");
        Matcher m = pattern.matcher(pageBody);
        while (m.find()) {
            Topic f = new Topic(m.group(1), m.group(2));
            forum.addTheme(f);
        }
    }

    public static String performGet(String url, Client.OnProgressChangedListener progressChangedListener) throws IOException {
        progressChangedListener.onProgressChanged("Получение данных...");
        String pageBody = Client.INSTANCE.performGet(url);
        progressChangedListener.onProgressChanged("Обработка данных...");

        return pageBody;
    }
}
