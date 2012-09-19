package org.softeg.slartus.forpda;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.*;
import android.widget.ListView;
import android.widget.Toast;
import org.softeg.slartus.forpda.Tabs.Tabs;
import org.softeg.slartus.forpda.Tabs.ThemesTab;
import org.softeg.slartus.forpdaapi.NotReportException;

/**
 * User: slinkin
 * Date: 14.11.11
 * Time: 11:48
 */
public class QuickStartActivity extends BaseActivity{
    private ThemesTab themesTab;
    private Handler mHandler = new Handler();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().requestFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

        setContentView(R.layout.empty_activity);
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();

        String template = extras.getString("template");
        themesTab = Tabs.create(this, template, "QuickTab");
        setContentView(themesTab);
        try {
            setTitle(Tabs.getDefaultTemplateName(template));
        } catch (NotReportException e) {

        }
        themesTab.refresh(extras);
    }

    private Boolean m_ExitWarned = false;

    @Override
    public void onBackPressed() {

        if (!themesTab.onParentBackPressed()) {
            if (!m_ExitWarned) {
                Toast.makeText(getApplicationContext(), "Нажмите кнопку НАЗАД снова, чтобы закрыть", Toast.LENGTH_SHORT).show();
                m_ExitWarned = true;
            } else {
                finish();
            }

        } else {
            m_ExitWarned = false;
        }
    }



    public void refresh() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public Boolean refreshed() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public ListView getListView() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo, Handler handler) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void onResume() {
        super.onResume();
        m_ExitWarned = false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add("Обновить")
                .setIcon(R.drawable.ic_menu_refresh)
                .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {

                    public boolean onMenuItemClick(MenuItem item) {

                        themesTab.refresh();


                        return true;
                    }
                });
        menu.add("Закрыть")
                .setIcon(android.R.drawable.ic_menu_close_clear_cancel)
                .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {

                    public boolean onMenuItemClick(MenuItem item) {

                        finish();


                        return true;
                    }
                });
        return true;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, android.view.View v, android.view.ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        themesTab.onCreateContextMenu(menu, v, menuInfo, mHandler);

    }
}
