package org.softeg.slartus.forpda.Mail.classes;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import org.softeg.slartus.forpda.R;

/**
 * User: slinkin
 * Date: 13.03.12
 * Time: 12:04
 */
public class MailsAdapter extends ArrayAdapter<Mail> {
    private LayoutInflater m_Inflater;


    public MailsAdapter(Context context) {
        super(context, R.layout.theme_item);


        m_Inflater = LayoutInflater.from(context);


    }

    public void setData(Mails data) {
        if (getCount() > 0)
            clear();
        if (data != null) {
            for (Mail item : data) {
                add(item);
            }
        }
    }

    private Boolean m_SelectionMode = false;

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        final ViewHolder holder;

        if (convertView == null) {


            convertView = m_Inflater.inflate(R.layout.mail_item, parent, false);
//            convertView.setOnClickListener(new View.OnClickListener() {
//                public void onClick(View view) {
//                    try {
//                        Mail mail = ((ViewHolder) view.getTag()).mail;
//                        if (m_SelectionMode) {
//                            mail.IsChecked = !mail.IsChecked;
//                        } else {
//                            Intent intent = new Intent(getContext(), MailActivity.class);
//                            intent.putExtra("MailId", mail.getId());
//                            getContext().startActivity(intent);
//                            mail.setIsNew(false);
//                        }
//
//
//                    } catch (Exception e) {
//                        Log.e(getContext(), e);
//                    }
//                }
//            });
//
//            convertView.setOnLongClickListener(new View.OnLongClickListener() {
//                public boolean onLongClick(View view) {
//
//                    m_SelectionMode = !m_SelectionMode;
//                    if (m_SelectionMode) {
//                        for (int i = 0; i < getCount(); i++) {
//                            getItem(position).IsChecked = false;
//                        }
//                        Mail mail = ((ViewHolder) view.getTag()).mail;
//                        mail.IsChecked = true;
//
//                        MailsAdapter.this.notifyDataSetInvalidated();
//                    }
//                    return true;
//                }
//            });

            holder = new ViewHolder();
            holder.check = (ImageView) convertView
                    .findViewById(R.id.check);

            holder.check.setVisibility(m_SelectionMode ? View.VISIBLE : View.GONE);
            holder.txtIsNew = (TextView) convertView
                    .findViewById(R.id.txtIsNew);

            holder.topictime = (TextView) convertView
                    .findViewById(R.id.topictime);

            holder.topicauthor = (TextView) convertView
                    .findViewById(R.id.topicauthor);

            holder.topictitle = (TextView) convertView
                    .findViewById(R.id.topictitle);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Mail mail = this.getItem(position);
        if (m_SelectionMode) {
            holder.check.setImageResource(mail.IsChecked ? R.drawable.btn_check_buttonless_on : R.drawable.btn_check_buttonless_off);
        }
        if (mail.getIsNew()) {
            holder.txtIsNew.setBackgroundResource(R.color.newtheme);
            holder.txtIsNew.setTextColor(getContext().getResources().getColor(R.color.newtheme));
            holder.txtIsNew.setText("*");
        } else {
            holder.txtIsNew.setBackgroundColor(android.R.color.transparent);
            holder.txtIsNew.setTextColor(android.R.color.transparent);
            holder.txtIsNew.setText("");
        }
        holder.topictime.setText(mail.getDate());
        holder.topictitle.setText(mail.getTheme());
        holder.topicauthor.setText(mail.getUser());
        holder.mail = mail;

        return convertView;
    }


    public class ViewHolder {
        ImageView check;
        TextView txtIsNew;
        TextView topictime;
        TextView topicauthor;
        TextView topictitle;
        Mail mail;

    }
}
