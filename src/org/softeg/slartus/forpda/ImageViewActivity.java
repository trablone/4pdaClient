package org.softeg.slartus.forpda;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import org.softeg.slartus.forpda.classes.TouchImage.TouchImageView;
import org.softeg.slartus.forpda.common.Log;

import java.io.IOException;

/**
 * User: slinkin
 * Date: 28.11.11
 * Time: 14:04
 */
public class ImageViewActivity extends BaseActivity {
    private static final int COMPLETE = 0;
    private static final int FAILED = 1;
    public static final String URL_KEY = "url";
    private TouchImageView mImage;
    private ProgressBar mSpinner;
    private Drawable mDrawable;
    private String mUrl;
    private Handler mHandler = new Handler();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.image_view_activity);

        mSpinner = (ProgressBar) findViewById(R.id.progress);

        mImage = (TouchImageView) findViewById(R.id.image);
        mImage.setClickable(true);


        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        mUrl = extras.getString(URL_KEY);

        setImageDrawable(mUrl);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add("Скачать")
                .setIcon(android.R.drawable.ic_menu_view)
                .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {

                    public boolean onMenuItemClick(MenuItem item) {

                        DownloadsActivity.download( ImageViewActivity.this, mUrl);
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


    /**
     * Callback that is received once the image has been downloaded
     */
    private final Handler imageLoadedHandler = new Handler(new Handler.Callback() {

        public boolean handleMessage(Message msg) {
            try {
                switch (msg.what) {
                    case COMPLETE:
                        Display display = getWindowManager().getDefaultDisplay();
                       int width = display.getWidth();
                      int height = display.getHeight();
                        mImage.setDrawable(mDrawable,width, height);

                        mImage.setVisibility(View.VISIBLE);
                        mSpinner.setVisibility(View.GONE);
                        break;
                    case FAILED:
                        mSpinner.setVisibility(View.GONE);
                        Bundle data=msg.getData();
                        Log.e(ImageViewActivity.this,data.getString("message"), (Throwable)data.getSerializable("exception"));
                    default:
                        // Could change image here to a 'failed' image
                        // otherwise will just keep on spinning
                        break;
                }
            } catch (Exception ex) {
                Log.e(ImageViewActivity.this.getBaseContext(),"Ошибка загрузки изображения по адресу: "+mUrl, ex);
            }

            return true;
        }
    });

    /**
     * Set's the view's drawable, this uses the internet to retrieve the image
     * don't forget to add the correct permissions to your manifest
     *
     * @param imageUrl the url of the image you wish to load
     */
    private void setImageDrawable(final String imageUrl) {
        mDrawable = null;
        mSpinner.setVisibility(View.VISIBLE);
        mImage.setVisibility(View.GONE);
        new Thread() {
            public void run() {
                HttpHelper httpHelper=new HttpHelper();
                try {

                    mDrawable = Drawable.createFromStream(httpHelper.getImageStream(imageUrl), "name");

                    imageLoadedHandler.sendEmptyMessage(COMPLETE);

                }catch (OutOfMemoryError e) {
                    Bundle data=new Bundle();
                    data.putSerializable("exception",e);
                    data.putString("message","Нехватка памяти: "+mUrl);
                    Message message=new Message();
                    message.what=FAILED;
                    message.setData(data);
                    imageLoadedHandler.sendMessage(message);
                }
                catch (Exception e) {
                    Bundle data=new Bundle();
                    data.putSerializable("exception",e);
                    data.putString("message","Ошибка загрузки изображения по адресу: "+mUrl);
                    Message message=new Message();
                    message.what=FAILED;
                    message.setData(data);
                    imageLoadedHandler.sendMessage(message);

                } finally{
                    httpHelper.close();
                }
            }


        }.start();
    }

    private static Drawable getDrawableFromUrl(final String url) throws IOException {
        HttpHelper httpHelper=new HttpHelper();
        return Drawable.createFromStream(httpHelper.getImageStream(url), "name");
    }

}
