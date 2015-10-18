package com.zLab.K2Pass;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.*;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.InputType;
import android.view.*;
import android.widget.*;
import org.apache.commons.codec.binary.Hex;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Random;

public class MainActivity extends Activity {

    int PASSKEY_LENGTH = 10;
    int PASSKEY_LENGTH_NEW = 0;
    private String MASTER_KEY = "";

    Button btnRandMasterKey;
    Button btnAddResources;
    EditText txtMasterKey;
    ListView listResources;
    ArrayList<String> valuesName;
    ArrayList<String> valuesPassword;
    ListViewAdaptor listResourcesAdapter;
    hmacController hmac;
    SharedPreferences prefs;
    JSONObject jsonObj;
    JSONArray jsonArray;
    String CONFName = "items";
    String JSONArrayName = "list";

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
                itemEdit(position);
                return false;
            }
        });

        btnAddResources = (Button) findViewById(R.id.btnAddResource);
        btnAddResources.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newPass_setName();
            }
        });
        hmac = new hmacController();
        initMasterKey();
    }
    /** App start **/
    private void initMasterKey(){
        activeDialog = true;

        View masterPasswordLayout = LayoutInflater.from(MainActivity.this).inflate(R.layout.masterpasswordinput, null);

        txtMasterKey = (EditText) masterPasswordLayout.findViewById(R.id.masterPasswordInput);
        txtMasterKey.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);

        btnRandMasterKey = (Button) masterPasswordLayout.findViewById(R.id.masterPasswordRandKey);
        btnRandMasterKey.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                txtMasterKey.setText(MainActivity.this.getRandomMasterKey());
                refreshAllPassword();
            }
        });

        final AlertDialog d = new AlertDialog.Builder(MainActivity.this)
                .setView(masterPasswordLayout)
                .setTitle(R.string.master_key)
                .setPositiveButton(R.string.ok, null)
                .setNegativeButton(R.string.Cancel, null)
                .create();

        d.setOnShowListener(new DialogInterface.OnShowListener() {

            @Override
            public void onShow(DialogInterface dialog) {

                Button bp = d.getButton(AlertDialog.BUTTON_POSITIVE);
                bp.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        if(!txtMasterKey.getText().toString().equals("")){
                            MASTER_KEY = txtMasterKey.getText().toString();
                            try {
                                hmac.initMac(MASTER_KEY, hmac.HMAC512);
                            } catch (InvalidKeyException e) {
                                e.printStackTrace();
                            } catch (UnsupportedEncodingException e) {
                                e.printStackTrace();
                            } catch (NoSuchAlgorithmException e) {
                                e.printStackTrace();
                            }
                            activeDialog=false;
                            loadPrefs();
                            d.dismiss();
                        } else {
                            Toast.makeText(MainActivity.this,R.string.empty_master,Toast.LENGTH_LONG).show();
                        }
                    }
                });
                Button bn = d.getButton(AlertDialog.BUTTON_NEGATIVE);
                bn.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        d.dismiss();
                        MainActivity.this.finish();
                    }
                });

            }
        });
        d.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if (MASTER_KEY.equals("")){
                    MainActivity.this.finish();
                }
            }
        });
        d.show();
    }
    private void itemEdit(final int position){
        activeDialog = true;

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle(R.string.actions);

        LayoutInflater inflater = (LayoutInflater) getApplicationContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.editlayout, null);


        final TextView input = (TextView) layout.findViewById(R.id.itemName);
        input.setText(listResourcesAdapter.getName(position));
        builder.setView(layout);

        builder.setPositiveButton(R.string.edit, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                newPass_setLenght(listResourcesAdapter.getName(position), true, position);
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
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
                    jsonArray.remove(position);
                } else {
                    oldApiJSONremove(position);
                }
                listResourcesAdapter.deleteItem(position);
                listResourcesAdapter.notifyDataSetChanged();
                savePrefs();
                activeDialog=false;
            }});
        builder.show();
    }
    private void oldApiJSONremove(int index){
        JSONArray list = new JSONArray();
        int len = jsonArray.length();
        if (jsonArray != null) {
            for (int i=0;i<len;i++)
            {
                if (i != index)
                {
                    try {
                        list.put(jsonArray.get(i));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        jsonArray = list;
    }
    /** Make new item **/
    private void loadPrefs(){
        prefs = getPreferences(MODE_PRIVATE);
        String jsonContent = prefs.getString(CONFName, "");

        if(!jsonContent.equals("")){
            try {
                jsonObj = new JSONObject(jsonContent);
                jsonArray = (JSONArray) jsonObj.get(JSONArrayName);
                for(int i = 0; i < jsonArray.length(); i++){
                    try {
                        String name = ((JSONObject) jsonArray.get(i)).getString("name");
                        int length = ((JSONObject) jsonArray.get(i)).getInt("length");
                        int method = ((JSONObject) jsonArray.get(i)).getInt("method");

                        try {
                            listResourcesAdapter.add(name, hmac.getNewStringPassword(name,length,method));
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
    private void newPass_setName(){
        activeDialog = true;

        final EditText input = new EditText(getApplicationContext());
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS | InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);
        input.setText("");

        FrameLayout container = new FrameLayout(getApplicationContext());
        FrameLayout.LayoutParams params = new  FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.leftMargin= convertDpToPx(20);
        params.rightMargin= convertDpToPx(20);
        input.setLayoutParams(params);
        container.addView(input);

        final AlertDialog d = new AlertDialog.Builder(MainActivity.this)
                .setView(container)
                .setTitle(R.string.new_name)
                .setPositiveButton(R.string.ok, null)
                .setNegativeButton(R.string.Cancel, null)
                .create();

        d.setOnShowListener(new DialogInterface.OnShowListener() {

            @Override
            public void onShow(DialogInterface dialog) {
                Button bp = d.getButton(AlertDialog.BUTTON_POSITIVE);
                bp.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        if(!input.getText().toString().equals("")){
                            newPass_setLenght(input.getText().toString().toUpperCase(), false, -1);
                            activeDialog=false;
                            d.dismiss();
                        } else {
                            Toast.makeText(MainActivity.this,R.string.empty_name,Toast.LENGTH_LONG).show();
                        }
                    }
                });
                Button bn = d.getButton(AlertDialog.BUTTON_NEGATIVE);
                bn.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        activeDialog=false;
                        d.cancel();
                    }
                });

            }
        });
        d.show();
    }
    private void newPass_setLenght(final String name, final Boolean isEdit, final int pos){
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
                newPass_setPassword(name, PASSKEY_LENGTH, isEdit, pos);
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
    private void newPass_setPassword(final String name, final int length, final Boolean isEdit, final int pos){
        activeDialog = true;

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle(R.string.new_password);

        final ListView paswordList = new ListView(getApplicationContext());
        final ArrayAdapter passwordListAdaptor = new ArrayAdapter<String>(getApplicationContext(), R.layout.passwordrow, R.id.passwordItem);
        paswordList.setAdapter(passwordListAdaptor);

        try {
            passwordListAdaptor.add(hmac.getNewStringPassword(name, length, hmac.METHOD_NORMAL));
            passwordListAdaptor.add(hmac.getNewStringPassword(name, length, hmac.METHOD_REVERSE));
            passwordListAdaptor.add(hmac.getNewStringPassword(name, length, hmac.METHOD_HMAC_NORMAL));
            passwordListAdaptor.add(hmac.getNewStringPassword(name, length, hmac.METHOD_HMAC_REVERSE));
            passwordListAdaptor.add(hmac.getNewStringPassword(name, length, hmac.METHOD_SPLIT));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

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
                if(isEdit){
                    jsonArray.remove(position);
                    addNewItemToSettings(name, length, position, true, pos);
                    listResourcesAdapter.changeItemAtPosition(pos, name, passwordListAdaptor.getItem(position).toString());
                    listResourcesAdapter.notifyDataSetChanged();
                } else {
                    newPass_generateItem(name, passwordListAdaptor.getItem(position).toString());
                    addNewItemToSettings(name, length, position, false, -1);
                }
                activeDialog=false;
                dialog.dismiss();
            }
        });
    }
    private void newPass_generateItem(String name, String password){
        listResourcesAdapter.add(name, password);
        listResourcesAdapter.notifyDataSetChanged();
    }
    private void addNewItemToSettings(String name, int length, int method, Boolean isEdit, int position){
        if(jsonArray==null){jsonArray = new JSONArray();}
        if(jsonObj==null){jsonObj = new JSONObject();}

        JSONObject json = new JSONObject();
        try {
            json.put("name", name);
            json.put("length", length);
            json.put("method", method);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if( isEdit ){
            try {
                jsonArray.put(position, json);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            jsonArray.put(json);
        }
        savePrefs();
    }
    private void savePrefs(){
        try {
            jsonObj.put(JSONArrayName, jsonArray);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        prefs.edit().putString(CONFName,jsonObj.toString()).apply();
    }
    /** Refresh all **/
    private void refreshAllPassword(){
        for(int i=0;i<listResourcesAdapter.getCount();i++){
            listResourcesAdapter.setPassword(i, MainActivity.this.getRandomString(PASSKEY_LENGTH));
        }
        listResourcesAdapter.notifyDataSetChanged();
    }
    /** Get random **/
    private String getRandomMasterKey(){
        String[] colorArray = getResources().getStringArray(R.array.color_names);
        String[] vegetablesArray = getResources().getStringArray(R.array.vegetables_names);
        String[] animalArray = getResources().getStringArray(R.array.animals_name);

        String randomBegin = colorArray[new Random().nextInt(colorArray.length)];
        String randomEnd = "";
        if(new Random().nextBoolean()){
            randomEnd = animalArray[new Random().nextInt(animalArray.length)];
        } else {
            randomEnd = vegetablesArray[new Random().nextInt(vegetablesArray.length)];
        }
        return randomBegin+" "+randomEnd;
    }
    private String getRandomString(int passkeyLength){
        return new RandomString(passkeyLength).nextString();
    }

    /** UI Tools **/
    public int convertDpToPx(int px){
        float scale = getResources().getDisplayMetrics().density;
        return (int) (px*scale + 0.5f);
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
class itemOfList {
    public String name;
    public int lenght;
    public int method;
}

class hmacController{
    SecretKeySpec SEC;
    Mac MAC;

    String HMAC256 = "HmacSHA256";
    String HMAC384 = "HmacSHA384";
    String HMAC512 = "HmacSHA512";

    int METHOD_NORMAL = 0;
    int METHOD_REVERSE = 1;
    int METHOD_HMAC_NORMAL = 2;
    int METHOD_HMAC_REVERSE = 3;
    int METHOD_SPLIT = 4;

    public void initMac(String secret, String HMAC) throws InvalidKeyException, UnsupportedEncodingException, NoSuchAlgorithmException {
        SEC = new SecretKeySpec( secret.getBytes( "UTF-8"), "HmacSHA512" );
        MAC = Mac.getInstance( HMAC );
        MAC.init( SEC );
    }

    public String getNewStringPassword(String textToCrypt, int length, int method) throws UnsupportedEncodingException {
        if (method==METHOD_REVERSE){
            return new StringBuilder(new String(Hex.encodeHex(MAC.doFinal(textToCrypt.getBytes("UTF-8"))))).reverse().toString().substring(0,length);
        } else if (method==METHOD_HMAC_NORMAL){
            String str = new String(Hex.encodeHex(MAC.doFinal(textToCrypt.getBytes("UTF-8"))));
            return getNewStringPassword(str,length,METHOD_NORMAL);
        } else if (method==METHOD_HMAC_REVERSE){
            String str = new String(Hex.encodeHex(MAC.doFinal(textToCrypt.getBytes("UTF-8"))));
            return getNewStringPassword(str,length,METHOD_REVERSE);
        } else if (method==METHOD_SPLIT){
            String str = new String(Hex.encodeHex(MAC.doFinal(textToCrypt.getBytes("UTF-8"))));
            String newStr = str.substring(str.length()/2,str.length()) + str.substring(0,str.length()/2);
            return newStr.substring(0, length);
        } else {
            // IF METHOD_NORMAL
            return new String(Hex.encodeHex(MAC.doFinal(textToCrypt.getBytes("UTF-8")))).substring(0,length);
        }
    }
}