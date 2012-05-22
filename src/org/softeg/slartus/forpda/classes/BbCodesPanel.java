package org.softeg.slartus.forpda.classes;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import org.softeg.slartus.forpda.MyApp;
import org.softeg.slartus.forpda.common.Log;

import java.io.IOException;
import java.io.InputStream;

/**
 * User: slinkin
 * Date: 16.03.12
 * Time: 9:46
 */
public class BbCodesPanel {
    private Context mContext;
    private LinearLayout lnrBbCodes;
    private EditText txtPost;
    public BbCodesPanel(Context context, LinearLayout bbCodesLinearLayout, EditText editText){
        mContext=context;
        lnrBbCodes=bbCodesLinearLayout;
        txtPost=editText;
        mNotClosedCodes = new int[mBbCodes.length];
        try{
            createBbCodeButtons(); 
        }catch (Exception ex){
           Log.e(mContext,ex);
        }
        
    }
    
    String[] mBbCodes = new String[]{"B", "I", "U", "S", "SUB", "SUP", "LEFT", "CENTER", "RIGHT", "URL", "QUOTE", "OFFTOP", "CODE", "SPOILER", "HIDE", "LIST", "NUMLIST"};
    int[] mNotClosedCodes;

    private void createBbCodeButtons() throws IOException {
        String style = MyApp.INSTANCE.getCurrentThemeName();
        LinearLayout.LayoutParams layoutParams
                = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        float d = mContext.getResources().getDisplayMetrics().density;
        layoutParams.setMargins((int) (5 * d), (int) (2 * d), (int) (5 * d), (int) (2 * d));
        int ind = 0;
        for (String bbCode : mBbCodes) {
            ImageButton imgBtn = new ImageButton(mContext);

            imgBtn.setBackgroundColor(Color.BLACK);

            imgBtn.setImageBitmap(getBitmapFromAsset("forum/style_images/1/folder_editor_buttons_" + style + "/" + bbCode.toLowerCase() + ".png"));
            imgBtn.setTag(ind++);
            if (bbCode.equals("LIST")) {
                imgBtn.setOnClickListener(getListBbCodeOnClickListener(""));
            } else if (bbCode.equals("NUMLIST")) {
                imgBtn.setOnClickListener(getListBbCodeOnClickListener("=1"));
            } else if (bbCode.equals("URL")) {
                imgBtn.setOnClickListener(getUrlBbCodeOnClickListener());
            } else {
                imgBtn.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View view) {
                        bbCodeClick(Integer.parseInt(view.getTag().toString()));
                    }
                });
            }

            lnrBbCodes.addView(imgBtn, layoutParams);
        }
    }

    private View.OnClickListener getUrlBbCodeOnClickListener() {
        return new View.OnClickListener() {
            public void onClick(View view) {
                int selectionStart = txtPost.getSelectionStart();
                int selectionEnd = txtPost.getSelectionEnd();
                if (selectionEnd < selectionStart && selectionEnd != -1) {
                    int c = selectionStart;
                    selectionStart = selectionEnd;
                    selectionEnd = c;
                }
                String urlText = null;
                if (selectionStart != -1 && selectionStart != selectionEnd) {
                    urlText = txtPost.getText().toString()
                            .substring(selectionStart, selectionEnd);
                }

                createUrlDialog(null, urlText, "Пожалуйста, введите полный URL адрес", "http://");
            }
        };
    }

    private void createUrlDialog(final String url, final String urlText, String captionText, String editText) {

        AlertDialog.Builder alert = new AlertDialog.Builder(mContext);

        LinearLayout layout = new LinearLayout(mContext);
        layout.setPadding(5, 5, 5, 5);
        layout.setOrientation(LinearLayout.VERTICAL);

        final TextView tx = new TextView(mContext);
        tx.setText(captionText);
        layout.addView(tx);

        // Set an EditText view to get user input
        final EditText input = new EditText(mContext);
        input.setText(editText);
        input.requestFocus();
        layout.addView(input);

        alert.setView(layout)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        String tempUrlText = urlText;
                        String tempUrl = url;
                        if (!TextUtils.isEmpty(url)) {
                            tempUrlText = input.getText().toString();
                        } else {
                            tempUrl = input.getText().toString();
                        }

                        if (TextUtils.isEmpty(tempUrlText)) {
                            createUrlDialog(input.getText().toString(), null, "Пожалуйста, введите заголовок", "Посетить мою домашнюю страницу");
                            return;
                        }
                        int selectionStart = txtPost.getSelectionStart();
                        int selectionEnd = txtPost.getSelectionEnd();
                        if (selectionEnd < selectionStart && selectionEnd != -1) {
                            int c = selectionStart;
                            selectionStart = selectionEnd;
                            selectionEnd = c;
                        }
                        txtPost.getText().replace(selectionStart, selectionEnd, "[URL=" + (tempUrl == null ? "" : tempUrl) + "]" + tempUrlText + "[/URL]");
                    }
                })
                .setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();

                        return;

                    }
                });
        alert.show();
    }

    private View.OnClickListener getListBbCodeOnClickListener(final String listTagPostFix) throws IOException {
        return new View.OnClickListener() {
            public void onClick(View view) {
                int selectionStart = txtPost.getSelectionStart();
                int selectionEnd = txtPost.getSelectionEnd();
                if (selectionEnd < selectionStart && selectionEnd != -1) {
                    int c = selectionStart;
                    selectionStart = selectionEnd;
                    selectionEnd = c;
                }
                if (selectionStart != -1 && selectionStart != selectionEnd) {
                    String selectedText = txtPost.getText().toString()
                            .substring(selectionStart, selectionEnd);
                    while (selectedText.indexOf("\n\n") != -1) {
                        selectedText = selectedText.replace("\n\n", "\n");
                    }
                    String modifiedText = "[LIST" + listTagPostFix + "]"
                            + selectedText
                            .replaceAll("^", "[*]")
                            .replace("\n", "\n[*]")
                            + "[/LIST]";
                    txtPost.getText().replace(selectionStart, selectionEnd, modifiedText);
                    return;
                }
                StringBuilder sb = new StringBuilder();
                createListDialog(1, sb, listTagPostFix);
            }
        };
    }

    private void createListDialog(final int ind, final StringBuilder sb, final String listTagPostFix) {

        AlertDialog.Builder alert = new AlertDialog.Builder(mContext);

        LinearLayout layout = new LinearLayout(mContext);
        layout.setPadding(5, 5, 5, 5);
        layout.setOrientation(LinearLayout.VERTICAL);

        final TextView tx = new TextView(mContext);
        tx.setText("Введите содержание " + ind + " пункта списка");
        layout.addView(tx);

        // Set an EditText view to get user input
        final EditText input = new EditText(mContext);
        input.requestFocus();
        layout.addView(input);

        alert.setView(layout)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        if (input.getText().toString().isEmpty()) {
                            tryInsertListText(sb, listTagPostFix);
                            return;
                        }
                        sb.append("[*]" + input.getText().toString() + "\n");
                        createListDialog(ind + 1, sb, listTagPostFix);
                    }
                })
                .setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();

                        tryInsertListText(sb, listTagPostFix);
                        return;

                    }
                });
        alert.show();
    }

    private void tryInsertListText(StringBuilder sb, final String listTagPostFix) {
        String text = sb.toString().trim();
        if (text.isEmpty()) return;

        int selectionStart = txtPost.getSelectionStart();
        txtPost.getText().insert(selectionStart, "[LIST" + listTagPostFix + "]" + text + "[/LIST]");
    }

    private void bbCodeClick(int tagIndex) {
        String tag = mBbCodes[tagIndex];

        int selectionStart = txtPost.getSelectionStart();
        int selectionEnd = txtPost.getSelectionEnd();
        if (selectionEnd < selectionStart && selectionEnd != -1) {
            int c = selectionStart;
            selectionStart = selectionEnd;
            selectionEnd = c;
        }
        if (selectionStart != -1 && selectionStart != selectionEnd) {
            txtPost.getText().insert(selectionStart, "[" + tag + "]");
            txtPost.getText().insert(selectionEnd + tag.length() + 2, "[/" + tag + "]");
            return;
        }

        if (mNotClosedCodes[tagIndex] > 0) {
            txtPost.getText().insert(selectionStart, "[/" + tag + "]");
            mNotClosedCodes[tagIndex]--;
        } else {
            txtPost.getText().insert(selectionStart, "[" + tag + "]");
            mNotClosedCodes[tagIndex]++;
        }
    }

    /**
     * Helper Functions
     *
     * @throws IOException
     */
    private Bitmap getBitmapFromAsset(String strName) throws IOException {
        AssetManager assetManager = mContext.getAssets();
        // BufferedInputStream buf = new BufferedInputStream(assetManager.open(strName));
        InputStream istr = assetManager.open(strName);
        Bitmap bitmap = BitmapFactory.decodeStream(istr);

        return bitmap;
    }
}
