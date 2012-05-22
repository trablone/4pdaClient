package org.softeg.slartus.forpda.Tabs;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import org.softeg.slartus.forpda.Client;
import org.softeg.slartus.forpda.NewsActivity;
import org.softeg.slartus.forpda.classes.Exceptions.NotReportException;
import org.softeg.slartus.forpda.classes.Topic;
import org.softeg.slartus.forpda.common.Log;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: Admin
 * Date: 06.12.11
 * Time: 16:08
 * To change this template use File | Settings | File Templates.
 */
public class NewsTab extends ThemesTab {


    public NewsTab(Context context, String tabTag) {
        super(context, tabTag);

    }

    public String getTemplate() {
        return TEMPLATE;
    }

    public static final String TEMPLATE = Tabs.TAB_NEWS;
    public static final String TITLE = "Новости";

    @Override
    public void refresh() {
        super.refresh();
    }

    @Override
    public String getTitle() {
        return TITLE;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo, Handler handler) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        if (info.id == -1) return;
        final Topic topic = m_ThemeAdapter.getItem((int) info.id);
        if (TextUtils.isEmpty(topic.getId())) return;

        menu.add("Открыть в браузере").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem menuItem) {
                Intent marketIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(topic.getId()));
                getContext().startActivity(Intent.createChooser(marketIntent, "Выберите"));
                return true;
            }
        });
    }


    @Override
    public void getThemes(Client.OnProgressChangedListener progressChangedListener) throws Exception {
        getRssItems(progressChangedListener);
    }
    
    private String normalizeRss(String body){
        return body.replaceAll("&(?!amp;)","&amp;");
    }

    private void getRssItems(Client.OnProgressChangedListener progressChangedListener) throws Exception {
        try {
            Client.INSTANCE.doOnOnProgressChanged(progressChangedListener, "Получение данных...");
            Client.INSTANCE.checkLogin(Client.INSTANCE.performGet("http://4pda.ru/forum/"));
            String body = Client.INSTANCE.performGet("http://4pda.ru/feed/");
            if(TextUtils.isEmpty(body))
                throw new NotReportException("Сервер вернул пустую страницу!");
            Client.INSTANCE.doOnOnProgressChanged(progressChangedListener, "Обработка данных...");

            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

            DocumentBuilder db = dbf.newDocumentBuilder();

            body=normalizeRss(body);

            Document document = db.parse(new InputSource(new StringReader(body)));

            Element element = document.getDocumentElement();

            NodeList nodeList = element.getElementsByTagName("item");

            if (nodeList.getLength() > 0) {

                for (int i = 0; i < nodeList.getLength(); i++) {

                        Element entry = (Element) nodeList.item(i);

                        Element _titleE = (Element) entry.getElementsByTagName("title").item(0);

                        Element _descriptionE = (Element) entry.getElementsByTagName("description").item(0);

                        Element _pubDateE = (Element) entry.getElementsByTagName("pubDate").item(0);

                        Element _linkE = (Element) entry.getElementsByTagName("link").item(0);


                        StringBuilder _title = new StringBuilder();
                        for (int c = 0; c < _titleE.getChildNodes().getLength(); c++) {
                            _title.append(_titleE.getChildNodes().item(c).getNodeValue());
                        }


                        String _description = _descriptionE.getFirstChild().getNodeValue();

                        Date _pubDate = new Date(_pubDateE.getFirstChild().getNodeValue());

                        String _link = _linkE.getFirstChild().getNodeValue();

                        String author = entry.getElementsByTagName("dc:creator").item(0).getChildNodes().item(0).getNodeValue();

                        Topic topic = new Topic(_link, _title.toString());
                        topic.setLastMessageDate(_pubDate);
                        topic.setLastMessageAuthor(author);
                        topic.setDescription(_description);

                        m_Themes.add(topic);

                }

            }
        } catch (Exception ex) {
            Log.e(getContext(),"Ошибка разбора rss",ex);
          //  throw new NotReportException("Ошибка разбора rss");
        }

    }

    protected void listItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        l = ListViewMethodsBridge.getItemId(getContext(),i, l);
        if (l < 0||m_ThemeAdapter.getCount()<=l) return;
        if (m_ThemeAdapter == null) return;
        Topic topic = m_ThemeAdapter.getItem((int) l);
        if (TextUtils.isEmpty(topic.getId())) return;
        topic.setIsNew(false);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        if (prefs.getString("tabs." + m_TabId + ".Action", "getlastpost").equals("browser"))
            showNewsBrowser(topic.getId());
        else
            showNewsActivity(topic.getId());

        m_ThemeAdapter.notifyDataSetChanged();
    }

    private void showNewsActivity(String url) {
        Intent intent = new Intent(getContext(), NewsActivity.class);
        intent.putExtra(NewsActivity.URL_KEY, url);

        getContext().startActivity(intent);
    }

    private void showNewsBrowser(String url) {
        Intent marketIntent = new Intent(
                Intent.ACTION_VIEW,
                Uri.parse(url));
        getContext().startActivity(Intent.createChooser(marketIntent, "Выберите"));
    }
}



