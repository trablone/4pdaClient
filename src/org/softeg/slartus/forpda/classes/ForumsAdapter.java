package org.softeg.slartus.forpda.classes;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import org.softeg.slartus.forpda.R;

import java.util.ArrayList;
import java.util.Hashtable;

/**
 * Created by IntelliJ IDEA.
 * User: Admin
 * Date: 15.11.11
 * Time: 22:56
 * To change this template use File | Settings | File Templates.
 */
public class ForumsAdapter extends ArrayAdapter<CharSequence> {
    private LayoutInflater m_Inflater;
    private Hashtable<String, CharSequence> m_CheckedIds = new Hashtable<String, CharSequence>();
    private ArrayList<String> m_VisibleIds = new ArrayList<String>();

    public ForumsAdapter(Context context, int textViewResourceId,
                         ArrayList<CharSequence> objects, Hashtable<String, CharSequence> checkedIds, ArrayList<String> visibleIds) {
        super(context, textViewResourceId, objects);
        m_CheckedIds = checkedIds;
        m_VisibleIds = visibleIds;
        m_Inflater = LayoutInflater.from(context);
    }


    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        final ViewHolder holder;

        if (convertView == null) {


            convertView = m_Inflater.inflate(R.layout.search_forum_item, parent, false);


            holder = new ViewHolder();
            holder.chkText = (CheckBox) convertView
                    .findViewById(R.id.chkText);
            holder.chkText.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    int pos = Integer.parseInt(holder.chkText.getTag().toString());
                    String id = m_VisibleIds.get(pos);
                    if (!b)
                        m_CheckedIds.remove(id);
                    else
                        m_CheckedIds.put(id, getItem(pos));
                }
            });

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        CharSequence text=this.getItem(position);
        holder.chkText.setText(text);
        holder.chkText.setTag(position);
        holder.chkText.setChecked(m_CheckedIds.containsKey(m_VisibleIds.get(position)));

//        if(text.toString().startsWith("  "))
//             holder.chkText.setTextColor(R.color.blue);
        return convertView;
    }

    public class ViewHolder {
        CheckBox chkText;

    }
}
