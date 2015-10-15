package com.zLab.K2Pass;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;

import java.util.ArrayList;
import java.util.Random;

public class MainActivity extends Activity {

    int PASSKEY_LENGTH = 10;
    int PASSKEY_LENGTH_NEW = 0;

    Button btnRandMasterKey;
    Button btnAddResources;
    EditText txtMasterKey;
    ListView listResources;
    ArrayList<String> valuesName;
    ArrayList<String> valuesPassword;
    ListViewAdaptor listResourcesAdapter;
    boolean activeDialog = false;

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
                if(!activeDialog){
                    ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText(listResourcesAdapter.getName(position), listResourcesAdapter.getPassword(position));
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(getApplicationContext(), getString(R.string.password_copied, listResourcesAdapter.getName(position)), Toast.LENGTH_SHORT).show();
                }
            }
        });

        listResources.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                activeDialog = true;

                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle(R.string.new_name);

                final EditText input = new EditText(getApplicationContext());
                input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS | InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);
                input.setText(listResourcesAdapter.getName(position));
                builder.setView(input);

                builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        listResourcesAdapter.setName(position, input.getText().toString().toUpperCase());
                        listResourcesAdapter.notifyDataSetChanged();
                        activeDialog=false;
                    }
                });
                builder.setNegativeButton(R.string.Cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        activeDialog=false;
                        dialog.cancel();
                    }
                });
                builder.setNeutralButton(R.string.Delete, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Toast.makeText(MainActivity.this,R.string.no_action, Toast.LENGTH_SHORT).show();
                        activeDialog=false;
                    }});
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
                txtMasterKey.setText(MainActivity.this.getRandomString(PASSKEY_LENGTH));
                refreshAllPassword();
            }
        });

        btnAddResources = (Button) findViewById(R.id.btnAddResource);
        btnAddResources.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addNewItem_setName();
            }
        });
    }

    private void addNewItem_setName(){
        activeDialog = true;

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle(R.string.new_name);

        final EditText input = new EditText(getApplicationContext());
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS | InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);
        input.setText(R.string.new_item_name);
        builder.setView(input);

        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                addNewItem_setLenght(input.getText().toString().toUpperCase());
                activeDialog=false;
            }
        });
        builder.setNegativeButton(R.string.Cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                activeDialog=false;
                dialog.cancel();
            }
        });
        builder.show();
    }
    private void addNewItem_setLenght(final String name){
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle(R.string.resize_password);

        PASSKEY_LENGTH_NEW = 10;

        final NumberPicker np = new NumberPicker(getApplicationContext());
        np.setMinValue(1);
        np.setMaxValue(99);
        np.setValue(PASSKEY_LENGTH_NEW);
        np.setWrapSelectorWheel(true);

        builder.setView(np);

        np.setOnValueChangedListener(new NumberPicker.OnValueChangeListener()
        {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal)
            {
                PASSKEY_LENGTH_NEW = newVal;
            }
        });

        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                PASSKEY_LENGTH = PASSKEY_LENGTH_NEW;
                addNewItem_setPassword(name, PASSKEY_LENGTH);
            }
        });
        builder.setNegativeButton(R.string.Cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }
    private void addNewItem_setPassword(final String name,final int lenght){
        activeDialog = true;

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle(R.string.new_password);

        final ListView paswordList = new ListView(getApplicationContext());
        final ArrayAdapter passwordListAdaptor = new ArrayAdapter<String>(getApplicationContext(), R.layout.passwordrow, R.id.passwordItem);
        paswordList.setAdapter(passwordListAdaptor);

        passwordListAdaptor.add(getRandomString(lenght));
        passwordListAdaptor.add(getRandomString(lenght));
        passwordListAdaptor.add(getRandomString(lenght));
        passwordListAdaptor.add(getRandomString(lenght));
        passwordListAdaptor.add(getRandomString(lenght));

        builder.setView(paswordList);

        builder.setNegativeButton(R.string.Cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                activeDialog=false;
                dialog.cancel();
            }
        });

        final AlertDialog dialog = builder.show();

        paswordList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                addNewItem_makeNewPasswordItem(name, passwordListAdaptor.getItem(position).toString());
                activeDialog=false;
                dialog.dismiss();
            }
        });

    }
    private void addNewItem_makeNewPasswordItem(String name, String password){
        listResourcesAdapter.add(name, password);
        listResourcesAdapter.notifyDataSetChanged();
    }
    private void refreshAllPassword(){
        for(int i=0;i<listResourcesAdapter.getCount();i++){
            listResourcesAdapter.setPassword(i, MainActivity.this.getRandomString(PASSKEY_LENGTH));
        }
        listResourcesAdapter.notifyDataSetChanged();
    }

    private void setPasswordLength(){
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle(R.string.resize_password);

        PASSKEY_LENGTH_NEW = 10;

        final NumberPicker np = new NumberPicker(getApplicationContext());
        np.setMinValue(1);
        np.setMaxValue(99);
        np.setValue(PASSKEY_LENGTH_NEW);
        np.setWrapSelectorWheel(true);

        builder.setView(np);

        np.setOnValueChangedListener(new NumberPicker.OnValueChangeListener()
        {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal)
            {
                PASSKEY_LENGTH_NEW = newVal;
            }
        });

        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                PASSKEY_LENGTH = PASSKEY_LENGTH_NEW;
                refreshAllPassword();
            }
        });
        builder.setNegativeButton(R.string.Cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }

    private String getRandomString(int passkeyLength){
        return new RandomString(passkeyLength).nextString();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_activity_actions, menu);
        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_search:
                setPasswordLength();
                return true;
            case R.id.action_exit:
                this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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
