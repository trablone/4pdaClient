package org.softeg.slartus.forpda;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.View;
import android.widget.*;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import org.softeg.slartus.forpda.Mail.EditMailActivity;
import org.softeg.slartus.forpda.common.Log;
import org.softeg.slartus.forpda.qms.QmsChatActivity;
import org.softeg.slartus.forpdaapi.UserProfile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * User: slinkin
 * Date: 21.11.11
 * Time: 14:44
 */
public class ProfileActivity extends BaseFragmentActivity {
    private String m_UserId="";
    private UserProfile m_UserProfile;
    private ImageView mImageAvatar;
    private ProgressBar mSpinnerAvatar;
    private Drawable mDrawableAvatar;
    private String mUrlAvatar;

    private ImageView mImagePhoto;
    private ProgressBar mSpinnerPhoto;
    private Drawable mDrawablePhoto;
    private String mUrlPhoto;
    private  MenuFragment mFragment1;

    public static final String USER_ID_KEY = "UserIdKey";
    String[] groups = new String[]{"Основное","О себе", "Личная информация", "Интересы", "Другая информация", "Статистика",
            "Контактная информация"};
    // коллекция для групп
    ArrayList<Map<String, String>> groupData;

    // коллекция для элементов одной группы
    ArrayList<Map<String, String>> childDataItem;

    // общая коллекция для коллекций элементов
    ArrayList<ArrayList<Map<String, String>>> childData;

    // список аттрибутов группы или элемента
    Map<String, String> m;

    ExpandableListView elvMain;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.profile_activity);


        mSpinnerPhoto = (ProgressBar) findViewById(R.id.pgsPhoto);
        mImagePhoto = (ImageView) findViewById(R.id.imgPhoto);
        mSpinnerAvatar = (ProgressBar) findViewById(R.id.pgsAvatar);
        mImageAvatar = (ImageView) findViewById(R.id.imgAvatar);

        elvMain = (ExpandableListView) findViewById(R.id.elvMain);

        elvMain.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            public boolean onChildClick(ExpandableListView parent, View v,
                                        int groupPosition,   int childPosition, long id) {

                return false;
            }
        });

        Intent intent = getIntent();
        if (intent != null) {
            Bundle extras = intent.getExtras();

            if (extras != null) {
                m_UserId = extras.getString(USER_ID_KEY);

                LoadTask loadTask = new LoadTask(this, m_UserId);
                loadTask.execute();

            }
        }
        createActionMenu();
    }

    private void createActionMenu() {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        mFragment1 = (MenuFragment) fm.findFragmentByTag("f1");
        if (mFragment1 == null) {
            mFragment1 = new MenuFragment();
            ft.add(mFragment1, "f1");
        }
        ft.commit();
    }

    public String getUserId(){
        return m_UserId;
    }

    public String getUserNick(){
        return m_UserProfile.login;
    }

    private void fillData(UserProfile userProfile) {
        m_UserProfile=userProfile;
        setTitle(userProfile.login+": Просмотр профиля");
        setImageDrawablePhoto(userProfile.personalPhoto);
        setImageDrawableAvatar(userProfile.avatar);
        // заполняем коллекцию групп из массива с названиями групп
        groupData = new ArrayList<Map<String, String>>();
        for (String group : groups) {
            // заполняем список аттрибутов для каждой группы
            m = new HashMap<String, String>();
            m.put("groupName", group); // имя компании
            groupData.add(m);
        }

        // список аттрибутов групп для чтения
        String groupFrom[] = new String[] {"groupName"};
        // список ID view-элементов, в которые будет помещены аттрибуты групп
        int groupTo[] = new int[] {android.R.id.text1};


        // создаем коллекцию для коллекций элементов
        childData = new ArrayList<ArrayList<Map<String, String>>>();

        fillGroups(userProfile);

        // список аттрибутов элементов для чтения
        String childFrom[] = new String[] {"itemName"};
        // список ID view-элементов, в которые будет помещены аттрибуты элементов
        int childTo[] = new int[] {android.R.id.text1};

        SimpleExpandableListAdapter adapter = new SimpleExpandableListAdapter(
                this,
                groupData,
                android.R.layout.simple_expandable_list_item_1,
                groupFrom,
                groupTo,
                childData,
                android.R.layout.simple_list_item_1,
                childFrom,
                childTo);
        elvMain.setAdapter(adapter);
        elvMain.expandGroup(0);

    }
    
    private void fillGroups(UserProfile userProfile){

        childDataItem = new ArrayList<Map<String, String>>();
        for (String item : userProfile.getMain()) {
            m = new HashMap<String, String>();
            m.put("itemName", item);
            childDataItem.add(m);
        }
        childData.add(childDataItem);


        childDataItem = new ArrayList<Map<String, String>>();
        for (String item : userProfile.getAbout()) {
            m = new HashMap<String, String>();
            m.put("itemName", item);
            childDataItem.add(m);
        }
        childData.add(childDataItem);

        childDataItem = new ArrayList<Map<String, String>>();
        for (String item : userProfile.getPrivateInfo()) {
            m = new HashMap<String, String>();
            m.put("itemName", item);
            childDataItem.add(m);
        }
        childData.add(childDataItem);

        childDataItem = new ArrayList<Map<String, String>>();
        for (String item : userProfile.getInterests()) {
            m = new HashMap<String, String>();
            m.put("itemName", item);
            childDataItem.add(m);
        }
        childData.add(childDataItem);

        childDataItem = new ArrayList<Map<String, String>>();
        for (String item : userProfile.getOtherInfo()) {
            m = new HashMap<String, String>();
            m.put("itemName", item);
            childDataItem.add(m);
        }
        childData.add(childDataItem);

        childDataItem = new ArrayList<Map<String, String>>();
        for (String item : userProfile.getStatistic()) {
            m = new HashMap<String, String>();
            m.put("itemName", item);
            childDataItem.add(m);
        }
        childData.add(childDataItem);

        childDataItem = new ArrayList<Map<String, String>>();
        for (String item : userProfile.getContactInfo()) {
            m = new HashMap<String, String>();
            m.put("itemName", item);
            childDataItem.add(m);
        }
        childData.add(childDataItem);
    }

    private static final int COMPLETE = 0;
    private static final int FAILED = 1;

    private void setImageDrawablePhoto(final String imageUrl) {
        if(TextUtils.isEmpty(imageUrl)){
            mImagePhoto.setVisibility(View.VISIBLE);
            mSpinnerPhoto.setVisibility(View.GONE);
            return;
        }
        mDrawablePhoto = null;
        mSpinnerPhoto.setVisibility(View.VISIBLE);
        mImagePhoto.setVisibility(View.GONE);
        mUrlPhoto=imageUrl;
        new Thread() {
            public void run() {
                HttpHelper httpHelper=new HttpHelper();
                try {

                    mDrawablePhoto = Drawable.createFromStream(httpHelper.getImageStream(imageUrl), "name");

                    imageLoadedHandlerPhoto.sendEmptyMessage(COMPLETE);

                }catch (OutOfMemoryError e) {
                    Bundle data=new Bundle();
                    data.putSerializable("exception",e);
                    data.putString("message","Нехватка памяти: "+mUrlPhoto);
                    Message message=new Message();
                    message.what=FAILED;
                    message.setData(data);
                    imageLoadedHandlerPhoto.sendMessage(message);
                }
                catch (Exception e) {
                    Bundle data=new Bundle();
                    data.putSerializable("exception",e);
                    data.putString("message","Ошибка загрузки изображения по адресу: "+mUrlPhoto);
                    Message message=new Message();
                    message.what=FAILED;
                    message.setData(data);
                    imageLoadedHandlerPhoto.sendMessage(message);

                } finally{
                    httpHelper.close();
                }
            }


        }.start();
    }

    /**
     * Callback that is received once the image has been downloaded
     */
    private final Handler imageLoadedHandlerPhoto = new Handler(new Handler.Callback() {

        public boolean handleMessage(Message msg) {
            try {
                switch (msg.what) {
                    case COMPLETE:
                        mImagePhoto.setImageDrawable(mDrawablePhoto);

                        mImagePhoto.setVisibility(View.VISIBLE);
                        mSpinnerPhoto.setVisibility(View.GONE);
                        break;
                    case FAILED:
                        mSpinnerPhoto.setVisibility(View.GONE);
                        Bundle data=msg.getData();
                        Log.e(ProfileActivity.this,data.getString("message"), (Throwable)data.getSerializable("exception"));
                    default:
                        // Could change image here to a 'failed' image
                        // otherwise will just keep on spinning
                        break;
                }
            } catch (Exception ex) {
                Log.e(ProfileActivity.this.getBaseContext(),"Ошибка загрузки изображения по адресу: "+mUrlPhoto, ex);
            }

            return true;
        }
    });


    private void setImageDrawableAvatar(final String imageUrl) {
        if(TextUtils.isEmpty(imageUrl)){
            mImageAvatar.setVisibility(View.VISIBLE);
            mSpinnerAvatar.setVisibility(View.GONE);
            return;
        }
        mDrawableAvatar = null;
        mSpinnerAvatar.setVisibility(View.VISIBLE);
        mImageAvatar.setVisibility(View.GONE);
        mUrlAvatar=imageUrl;
        new Thread() {
            public void run() {
                HttpHelper httpHelper=new HttpHelper();
                try {

                    mDrawableAvatar = Drawable.createFromStream(httpHelper.getImageStream(imageUrl), "name");

                    imageLoadedHandlerAvatar.sendEmptyMessage(COMPLETE);

                }catch (OutOfMemoryError e) {
                    Bundle data=new Bundle();
                    data.putSerializable("exception",e);
                    data.putString("message","Нехватка памяти: "+mUrlAvatar);
                    Message message=new Message();
                    message.what=FAILED;
                    message.setData(data);
                    imageLoadedHandlerAvatar.sendMessage(message);
                }
                catch (Exception e) {
                    Bundle data=new Bundle();
                    data.putSerializable("exception",e);
                    data.putString("message","Ошибка загрузки изображения по адресу: "+mUrlAvatar);
                    Message message=new Message();
                    message.what=FAILED;
                    message.setData(data);
                    imageLoadedHandlerAvatar.sendMessage(message);

                } finally{
                    httpHelper.close();
                }
            }


        }.start();
    }

    /**
     * Callback that is received once the image has been downloaded
     */
    private final Handler imageLoadedHandlerAvatar = new Handler(new Handler.Callback() {

        public boolean handleMessage(Message msg) {
            try {
                switch (msg.what) {
                    case COMPLETE:

                        mImageAvatar.setImageDrawable(mDrawableAvatar);

                        mImageAvatar.setVisibility(View.VISIBLE);
                        mSpinnerAvatar.setVisibility(View.GONE);
                        break;
                    case FAILED:
                        mSpinnerAvatar.setVisibility(View.GONE);
                        Bundle data=msg.getData();
                        Log.e(ProfileActivity.this,data.getString("message"), (Throwable)data.getSerializable("exception"));
                    default:
                        // Could change image here to a 'failed' image
                        // otherwise will just keep on spinning
                        break;
                }
            } catch (Exception ex) {
                Log.e(ProfileActivity.this.getBaseContext(),"Ошибка загрузки изображения по адресу: "+mUrlAvatar, ex);
            }

            return true;
        }
    });

    private class LoadTask extends AsyncTask<String, Void, Boolean> {

        Context mContext;
        private final ProgressDialog dialog;

        private String m_UserId;
        private UserProfile userProfile;

        public LoadTask(Context context,
                        String userId) {
            mContext = context;
            m_UserId = userId;
            dialog = new ProgressDialog(mContext);
        }

        @Override
        protected Boolean doInBackground(String... params) {
            try {
                userProfile = Client.INSTANCE.loadUserProfile(m_UserId);

                return true;
            } catch (Exception e) {

                ex = e;
                return false;
            }
        }

        // can use UI thread here
        protected void onPreExecute() {
            this.dialog.setMessage("Загрузка...");
            this.dialog.show();
        }

        private Exception ex;

        // can use UI thread here
        protected void onPostExecute(final Boolean success) {
            if (this.dialog.isShowing()) {
                this.dialog.dismiss();
            }

            if (success) {
                fillData(userProfile);
            } else {
                if (ex != null)
                    Log.e(ProfileActivity.this, ex);
                else
                    Toast.makeText(mContext, "Неизвестная ошибка",
                            Toast.LENGTH_SHORT).show();

            }
        }

    }

    public static final class MenuFragment extends SherlockFragment {
        public MenuFragment() {

        }
        
        private ProfileActivity getProfileActivity(){
            
            return (ProfileActivity)getActivity();
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setHasOptionsMenu(true);
        }

        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
            com.actionbarsherlock.view.MenuItem item;

            if(Client.INSTANCE.getLogined() &&!getProfileActivity().getUserId().equals(Client.INSTANCE.UserId)){
                item = menu.add("Отправить сообщение").setIcon(android.R.drawable.ic_menu_send);
                item.setOnMenuItemClickListener(new com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(com.actionbarsherlock.view.MenuItem menuItem) {

                        EditMailActivity.sendMessage(getActivity(), "CODE=04&act=Msg&MID="+getProfileActivity().getUserId(),getProfileActivity().getUserNick(),true);
                        return true;
                    }
                });
                item.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);

                item = menu.add("Связаться через QMS").setIcon(android.R.drawable.ic_menu_send);
                item.setOnMenuItemClickListener(new com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(com.actionbarsherlock.view.MenuItem menuItem) {
                        QmsChatActivity.openChat(getActivity(), getProfileActivity().getUserId(), getProfileActivity().getUserNick());
                        return true;
                    }
                });
                item.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
            }


            item = menu.add("Репутация").setIcon(android.R.drawable.ic_menu_view);
            item.setOnMenuItemClickListener(new com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener() {
                public boolean onMenuItemClick(com.actionbarsherlock.view.MenuItem menuItem) {
                    ReputationActivity.showRep(getActivity(), getProfileActivity().getUserId());
                    return true;
                }
            });
            item.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        }
    }
}
