package org.softeg.slartus.forpda.Tabs;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.*;
import android.widget.*;
import org.softeg.slartus.forpda.Client;
import org.softeg.slartus.forpda.QuickStartActivity;
import org.softeg.slartus.forpda.R;
import org.softeg.slartus.forpda.classes.Topic;
import org.softeg.slartus.forpda.classes.common.ExtPreferences;
import org.softeg.slartus.forpda.common.HelpTask;
import org.softeg.slartus.forpda.common.Log;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: Admin
 * Date: 21.09.11
 * Time: 0:03
 * To change this template use File | Settings | File Templates.
 */
public class ThemeAdapter extends ArrayAdapter<Topic> {
    private LayoutInflater m_Inflater;

    private int m_ThemeTitleSize = 13;
    private int m_TopTextSize = 10;
    private int m_BottomTextSize = 11;
    private int m_FlagTextSize = 12;

    public ThemeAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);

        m_Inflater = LayoutInflater.from(context);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        m_ThemeTitleSize = ExtPreferences.parseInt(prefs,
                "interface.themeslist.title.font.size", 13);
        m_TopTextSize = (int) Math.floor(10.0 / 13 * m_ThemeTitleSize);
        m_BottomTextSize = (int) Math.floor(11.0 / 13 * m_ThemeTitleSize);
        m_FlagTextSize= (int) Math.floor(12.0 / 13 * m_ThemeTitleSize);

    }

    public ThemeAdapter(Context context, int textViewResourceId, ArrayList<Topic> objects) {
        super(context, textViewResourceId, objects);

        m_Inflater = LayoutInflater.from(context);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        m_ThemeTitleSize = ExtPreferences.parseInt(prefs,
                "interface.themeslist.title.font.size", 13);
        m_TopTextSize = (int) Math.floor(10.0 / 13 * m_ThemeTitleSize);
        m_BottomTextSize = (int) Math.floor(11.0 / 13 * m_ThemeTitleSize);
        m_FlagTextSize= (int) Math.floor(12.0 / 13 * m_ThemeTitleSize);

    }

    private String params;

    public String getParams() {
        return params;
    }

    public void setParams(String params) {
        this.params = params;
    }

    private Boolean m_ShowForumTitle = false;

    public void showForumTitle(Boolean isShow) {
        m_ShowForumTitle = isShow;
    }


    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        final ViewHolder holder;

        if (convertView == null) {


            convertView = m_Inflater.inflate(R.layout.theme_item, parent, false);


            holder = new ViewHolder();
            holder.txtIsNew = (ImageView) convertView
                    .findViewById(R.id.txtIsNew);
            holder.usericon=convertView.findViewById(R.id.usericon);
            //holder.txtIsNew.setTextSize(m_FlagTextSize);

            holder.txtAuthor = (TextView) convertView
                    .findViewById(R.id.txtAuthor);
            holder.txtAuthor.setTextSize(m_TopTextSize);

            holder.txtLastMessageDate = (TextView) convertView
                    .findViewById(R.id.txtLastMessageDate);
            holder.txtLastMessageDate.setTextSize(m_TopTextSize);

            holder.txtTitle = (TextView) convertView
                    .findViewById(R.id.txtTitle);
            holder.txtTitle.setTextSize(m_ThemeTitleSize);

            holder.txtDescription = (TextView) convertView
                    .findViewById(R.id.txtDescription);
            holder.txtDescription.setTextSize(m_BottomTextSize);
//            holder.txtPostsCount=(TextView) convertView
//                    .findViewById(R.id.txtPostsCount);

            if (m_ShowForumTitle) {
                holder.txtForumTitle = (TextView) convertView
                        .findViewById(R.id.txtForumTitle);
                holder.txtForumTitle.setVisibility(View.VISIBLE);
                holder.txtForumTitle.setTextSize(m_BottomTextSize);
            }
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Topic topic = this.getItem(position);

        holder.txtAuthor.setText(topic.getLastMessageAuthor());
        holder.txtLastMessageDate.setText(topic.getLastMessageDateStr());
        holder.txtTitle.setText(topic.getTitle());
        holder.txtDescription.setText(topic.getDescription());
        //holder.txtPostsCount.setText(topic.getPostsCount());
        if (m_ShowForumTitle && !TextUtils.isEmpty(topic.getForumTitle())) {
            holder.txtForumTitle.setText("@" + topic.getForumTitle());
        }

        if (topic.getIsNew()) {
            holder.txtIsNew.setImageResource(R.drawable.new_flag);
        } else if (topic.getIsOld()) {
            holder.txtIsNew.setImageResource(R.drawable.old_flag);
        } else {
            holder.txtIsNew.setImageBitmap(null);
        }


        return convertView;
    }

    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo,
                                    Boolean addFavorites, Handler handler) {
        try {

            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
            if (info.id == -1) return;
            final Topic topic = getItem((int) info.id);
            if (TextUtils.isEmpty(topic.getId())) return;
            menu.add("К первому").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                public boolean onMenuItemClick(MenuItem menuItem) {
                    topic.showActivity(getContext());
                    topic.setIsNew(false);
                    notifyDataSetChanged();
                    return true;
                }
            });
            menu.add("К последнему").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                public boolean onMenuItemClick(MenuItem menuItem) {

                    topic.showActivity(getContext(), "view=getlastpost");
                    topic.setIsNew(false);
                    notifyDataSetChanged();
                    return true;
                }
            });
            menu.add("К первому непрочитанному").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                public boolean onMenuItemClick(MenuItem menuItem) {

                    topic.showActivity(getContext(), "view=getnewpost");
                    topic.setIsNew(false);
                    notifyDataSetChanged();
                    return true;
                }
            });
            menu.add("Открыть в браузере").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                public boolean onMenuItemClick(MenuItem menuItem) {

                    topic.showBrowser(getContext(), params);
                    topic.setIsNew(false);
                    notifyDataSetChanged();
                    return true;
                }
            });
            addOptionsMenu(getContext(), handler, menu, topic, addFavorites, null);
//            menu.add("Профиль автора").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
//                public boolean onMenuItemClick(MenuItem menuItem) {
//
//
//                    topic.setIsNew(false);
//                    notifyDataSetChanged();
//                    return true;
//                }
//            });

        } catch (Exception ex) {
            Log.e(this.getContext(), ex);
        }
    }

    public static SubMenu addOptionsMenu(final Context context, final Handler mHandler, Menu menu, final Topic topic,
                                         Boolean addFavorites, final String shareItUrl) {
        SubMenu optionsMenu = menu.addSubMenu("Опции").setIcon(android.R.drawable.ic_menu_more);

        configureOptionsMenu(context, mHandler, optionsMenu, topic, addFavorites, shareItUrl);
        return optionsMenu;
    }

    public static void configureOptionsMenu(final Context context, final Handler mHandler, SubMenu optionsMenu, final Topic topic,
                                            Boolean addFavorites, final String shareItUrl) {
        optionsMenu.clear();

        if (Client.INSTANCE.getLogined()) {

            if (addFavorites) {
                optionsMenu.add("Добавить в избранное").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem menuItem) {

                        final HelpTask helpTask = new HelpTask(context, "Добавление в избранное");
                        helpTask.setOnPostMethod(new HelpTask.OnMethodListener() {
                            public Object onMethod(Object param) {
                                if (helpTask.Success)
                                    Toast.makeText(context, (String) param, Toast.LENGTH_SHORT).show();
                                else
                                    Log.e(context, helpTask.ex);
                                return null;
                            }
                        });
                        helpTask.execute(new HelpTask.OnMethodListener() {
                            public Object onMethod(Object param) throws IOException {
                                return topic.addToFavorites();  //To change body of implemented methods use File | Settings | File Templates.
                            }
                        }
                        );

                        return true;
                    }
                });

                optionsMenu.add("Удалить из избранного").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem menuItem) {

                        final HelpTask helpTask = new HelpTask(context, "Удаление из избранного");
                        helpTask.setOnPostMethod(new HelpTask.OnMethodListener() {
                            public Object onMethod(Object param) {
                                if (helpTask.Success)
                                    Toast.makeText(context, (String) param, Toast.LENGTH_SHORT).show();
                                else
                                    Log.e(context, helpTask.ex);
                                return null;
                            }
                        });
                        helpTask.execute(new HelpTask.OnMethodListener() {
                            public Object onMethod(Object param) throws IOException {
                                return topic.removeFromFavorites();  //To change body of implemented methods use File | Settings | File Templates.
                            }
                        }
                        );

                        return true;
                    }
                });


                optionsMenu.add("Подписаться").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem menuItem) {

                        topic.startSubscribe(context, mHandler);

                        return true;
                    }
                });

                optionsMenu.add("Отписаться").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem menuItem) {

                        topic.unSubscribe(context, mHandler);

                        return true;
                    }
                });

                optionsMenu.add("Открыть форум темы").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem menuItem) {

                        Intent intent = new Intent(context, QuickStartActivity.class);

                        intent.putExtra("template", Tabs.TAB_FORUMS);
                        intent.putExtra(ForumTreeTab.KEY_FORUM_ID, topic.getForumId());
                        intent.putExtra(ForumTreeTab.KEY_FORUM_TITLE, topic.getForumTitle());
                        intent.putExtra(ForumTreeTab.KEY_TOPIC_ID, topic.getId());
                        context.startActivity(intent);

                        return true;
                    }
                });
            }


        }
        optionsMenu.add("Поделиться ссылкой").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem menuItem) {

                try {
                    Intent sendMailIntent = new Intent(Intent.ACTION_SEND);
                    sendMailIntent.putExtra(Intent.EXTRA_SUBJECT, topic.getTitle());
                    sendMailIntent.putExtra(Intent.EXTRA_TEXT, TextUtils.isEmpty(shareItUrl) ? ("http://4pda.ru/forum/index.php?showtopic=" + topic.getId()) : shareItUrl);
                    sendMailIntent.setType("text/plain");

                    context.startActivity(Intent.createChooser(sendMailIntent, "Поделиться ссылкой"));
                } catch (Exception ex) {
                    return false;
                }
                return true;
            }
        });
        //return optionsMenu;
    }


    public class ViewHolder {
        View usericon;
        ImageView txtIsNew;
        TextView txtAuthor;
        TextView txtLastMessageDate;
        TextView txtTitle;
        TextView txtDescription;
        TextView txtForumTitle;

    }

}
