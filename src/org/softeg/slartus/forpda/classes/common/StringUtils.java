package org.softeg.slartus.forpda.classes.common;

import android.content.Context;
import android.os.Build;
import android.widget.Toast;

import java.util.List;

public class StringUtils {

	public static String join(List<String> values, String string) {
		// TODO Auto-generated method stub
		StringBuilder sb=new StringBuilder();
		int c=values.size();
		for(String val:values){
			if(c-->1)
				sb.append(val+string);
			else
				sb.append(val);
			
		}
		return sb.toString();
	}

    public static void copyLinkToClipboard(Context context, String link) {
        copyToClipboard(context, link);
        Toast.makeText(context, "Ссылка скопирована в буфер обмена", Toast.LENGTH_SHORT).show();
    }

    public static void copyToClipboard(Context context, String link) {
        int sdk = new Integer(Build.VERSION.SDK).intValue();
        if(sdk< 11){
            android.text.ClipboardManager clipboard = (android.text.ClipboardManager) context.getSystemService(context.CLIPBOARD_SERVICE);
            clipboard.setText(link);
        } else{
            android.content.ClipboardManager clipboard = (android.content.ClipboardManager) context.getSystemService(context.CLIPBOARD_SERVICE);
            clipboard.setText(link);
        }
    }

    
    public static void translite(CharSequence str){
        String res="";
         for (int i=0;i<str.length();i++){
             char ch=str.charAt(i);
             
             if(ch<'А'&&ch>'я'){
                 res+=ch;
                 continue;
             }
             switch (ch){
                 case 'А':
                 case'а':
                     res+='a';

             }
         }
    }
    
    
}
