package com.zLab.K2Pass;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.*;

import java.util.ArrayList;
import java.util.Random;

public class MainActivity extends Activity {

    int PASSKEY_LENGTH = 16;

    Button btnRandMasterKey;
    Button btnAddResources;
    EditText txtMasterKey;
    ListView listResources;
    String NEW_ITEM_HEADER = "NONAME";
    ArrayList<String> valuesName;
    ArrayList<String> valuesPassword;
    ListViewAdaptor listResourcesAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        valuesName = new ArrayList<String>();
        valuesPassword = new ArrayList<String>();

        listResources = (ListView) findViewById(R.id.listResources);
        listResourcesAdapter = new ListViewAdaptor(this, valuesName, valuesPassword);
        listResources.setAdapter(listResourcesAdapter);

        listResources.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText(listResourcesAdapter.getName(position), listResourcesAdapter.getPassword(position));
                clipboard.setPrimaryClip(clip);
                Toast.makeText(getApplicationContext(), "Пароль от "+ listResourcesAdapter.getName(position) +" скопирован в буфер обмена.", Toast.LENGTH_LONG).show();
            }
        });

        listResources.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Введите новое имя:");

                final EditText input = new EditText(getApplicationContext());
                input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS | InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);
                input.setText(listResourcesAdapter.getName(position));
                builder.setView(input);

                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        listResourcesAdapter.setName(position, input.getText().toString());
                        listResourcesAdapter.notifyDataSetChanged();
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();
                return false;
            }
        });

        txtMasterKey = (EditText) findViewById(R.id.txtMasterKey);
        txtMasterKey.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);

        btnRandMasterKey = (Button) findViewById(R.id.btnRandMasterKey);
        btnRandMasterKey.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                txtMasterKey.setText(MainActivity.this.getRandomString());
                for(int i=0;i<listResourcesAdapter.getCount();i++){
                    listResourcesAdapter.setPassword(i, MainActivity.this.getRandomString());
                }
                listResourcesAdapter.notifyDataSetChanged();
            }
        });

        btnAddResources = (Button) findViewById(R.id.btnAddResource);
        btnAddResources.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //listResourcesAdapter.add(getRandomString());
                listResourcesAdapter.add(NEW_ITEM_HEADER, getRandomString());
                listResourcesAdapter.notifyDataSetChanged();
            }
        });
    }

    private String getRandomString(){
        return new RandomString(PASSKEY_LENGTH).nextString();
    }
}

class RandomString {
    private static final char[] symbols;
    static {
        StringBuilder tmp = new StringBuilder();
        for (char ch = '0'; ch <= '9'; ++ch)
            tmp.append(ch);
        for (char ch = 'a'; ch <= 'z'; ++ch)
            tmp.append(ch);
        for (char ch = 'A'; ch <= 'Z'; ++ch)
            tmp.append(ch);
        symbols = tmp.toString().toCharArray();
    }

    private final Random random = new Random();
    private final char[] buf;

    public RandomString(int length) {
        if (length < 1)
            throw new IllegalArgumentException("length < 1: " + length);
        buf = new char[length];
    }

    public String nextString() {
        for (int idx = 0; idx < buf.length; ++idx)
            buf[idx] = symbols[random.nextInt(symbols.length)];
        return new String(buf);
    }
}
