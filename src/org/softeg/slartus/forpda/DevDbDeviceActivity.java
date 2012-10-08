package org.softeg.slartus.forpda;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import android.widget.TableRow.LayoutParams;
import org.softeg.slartus.forpda.Tabs.DevicesTab;
import org.softeg.slartus.forpda.classes.DevDbDevice;
import org.softeg.slartus.forpda.classes.LazyGallery.LazyAdapter;
import org.softeg.slartus.forpda.common.Log;
import org.softeg.slartus.forpdaapi.OnProgressChangedListener;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * User: slinkin
 * Date: 25.11.11
 * Time: 13:05
 */
public class DevDbDeviceActivity extends BaseActivity {
    private Handler mHandler = new Handler();
    private String m_DeviceId;
    private Gallery gallery;
    private LazyAdapter adapter;
    private TableLayout infoTable;
    private DevDbDevice m_DevDbDevice;
    private TextView txtModel;
    private LinearLayout pnlRating;
    public static final String DEVICE_ID_KEY = "DeviceId";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.dev_db_device_activity);
        pnlRating = (LinearLayout) findViewById(R.id.pnlRating);
        gallery = (Gallery) findViewById(R.id.gallery);
        gallery.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String imgUrl = adapterView.getItemAtPosition(i).toString();
                ImageViewActivity.showImageUrl(DevDbDeviceActivity.this,imgUrl.replace("p.jpg", "n.jpg"));
            }
        });
        txtModel = (TextView) findViewById(R.id.txtModel);
        infoTable = (TableLayout) findViewById(R.id.infoTable);
        Intent intent = getIntent();
//        if (intent != null && intent.getData() != null) {
//            if (checkIsTheme(intent.getData().toString())) {
//                return;
//            }
//            checkIsFile(intent.getData().toString());
//
//            finish();
//            return;
//        }
        Bundle extras = intent.getExtras();

        m_DeviceId = extras.getString(DEVICE_ID_KEY);
        loadPage();
    }


    public void loadPage() {
        new LoadPageTask(this).execute();


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
         menu.add("Браузер")
                .setIcon(R.drawable.ic_menu_goto)
                .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {

                    public boolean onMenuItemClick(MenuItem item) {

                        Intent marketIntent = new Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("http://devdb.ru/" + m_DeviceId));
                        DevDbDeviceActivity.this.startActivity(Intent.createChooser(marketIntent, "Выберите"));
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
    public void onDestroy() {
        if (gallery != null)
            gallery.setAdapter(null);
        super.onDestroy();
    }

    private void fill() {
        adapter = new LazyAdapter(DevDbDeviceActivity.this,
                m_DevDbDevice.getScreenshotUrls().toArray(new String[m_DevDbDevice.getScreenshotUrls().size()]));
        gallery.setAdapter(adapter);

        txtModel.setText(m_DevDbDevice.getInfo().Model);
        fillRating();
        TableLayout.LayoutParams rowparams = new TableLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT);

        LayoutParams textviewparams = new LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT);

        for (DevDbDevice.InfoGroup group : m_DevDbDevice.getInfo()) {
            if (group.size() == 0) continue;
            TableRow row = new TableRow(this);

            TextView textView = createStyledTextView();
            textView.setText(group.Title.trim());
            textView.setTypeface(Typeface.DEFAULT_BOLD);
            row.addView(textView, textviewparams);
            row.addView(new TextView(this), textviewparams);
            infoTable.addView(row, rowparams);

            for (final DevDbDevice.InfoItem item : group) {
                TableRow row1 = new TableRow(this);

                TextView textView1 = createStyledTextView();
                textView1.setText(Html.fromHtml(item.Title.trim()));
                textView1.setTextSize(10);


                row1.addView(textView1, textviewparams);

                TextView textView2 = createStyledTextView();
                textView2.setText(Html.fromHtml(item.Value));
                textView2.setTextSize(10);
                textView2.setEllipsize(null);
                textView2.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View view) {
                        Pattern p = Pattern.compile("http://(.*?)\"");
                        Matcher m = p.matcher(item.Value);
                        if (m.find()) {
                            String url="http://" + m.group(1);
                            IntentActivity.tryShowUrl(DevDbDeviceActivity.this,mHandler,url,true,false);
                        }
                    }
                });

                textView2.setTypeface(Typeface.DEFAULT_BOLD);
                row1.addView(textView2, textviewparams);

                infoTable.addView(row1, rowparams);
            }
        }

        if (!TextUtils.isEmpty(m_DevDbDevice.getInfo().Price)) {
            TableRow row = new TableRow(this);


            TextView textView = createStyledTextView();
            textView.setText("Средняя цена:");
            textView.setTypeface(Typeface.DEFAULT_BOLD);
            row.addView(textView, textviewparams);

            textView = createStyledTextView();
            textView.setText(m_DevDbDevice.getInfo().Price);
            textView.setTypeface(Typeface.DEFAULT_BOLD);
            row.addView(textView, textviewparams);

            infoTable.addView(row, rowparams);
        }

    }

    private TextView createStyledTextView(){
         return (TextView)getLayoutInflater().inflate(R.layout.themed_textview,null);

    }



    private void fillRating() {
        double rating = m_DevDbDevice.getInfo().Rating;
        if (rating == -1) return;
        for (int i = 0; i < 5; i++) {
            ImageView img = new ImageView(this);
            if (rating >= (i + 1)) {
                img.setImageResource(R.drawable.star_full);
            } else if (rating > i)
                img.setImageResource(R.drawable.star_half);
            else
                img.setImageResource(R.drawable.star_none);
            pnlRating.addView(img);
        }
    }


    public class LoadPageTask extends AsyncTask<String, String, Boolean> {

        Context mContext;
        private final ProgressDialog dialog;

        public LoadPageTask(Context context) {
            mContext = context;
            dialog = new ProgressDialog(mContext);
            dialog.setCancelable(false);
        }

        @Override
        protected void onProgressUpdate(String... progress) {
            this.dialog.setMessage(progress[0]);
        }

        private Exception ex;

        @Override
        protected Boolean doInBackground(String... params) {
            try {
                m_DevDbDevice = new DevDbDevice();
                String pageBody = DevicesTab.performGet("http://devdb.ru/" + m_DeviceId, new OnProgressChangedListener() {
                    public void onProgressChanged(String state) {
                        publishProgress(state);
                    }
                });
                m_DevDbDevice.parse(pageBody);
                return true;
            } catch (Exception e) {

                ex = e;
                return false;
            }
        }

        protected void onPreExecute() {
            try {
                this.dialog.setMessage("Загрузка...");
                this.dialog.show();
            } catch (Exception ex) {
                Log.e(null, ex);
                this.cancel(true);
            }
        }

        protected void onCancelled() {
            super.onCancelled();

        }


        // can use UI thread here
        protected void onPostExecute(final Boolean success) {
            if (this.dialog.isShowing()) {
                this.dialog.dismiss();
            }

            if (success) {
                fill();
            } else {
                if (ex != null)
                    Log.e(mContext, ex);
            }
        }

    }

}
