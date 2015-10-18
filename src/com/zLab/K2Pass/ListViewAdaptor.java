package com.zLab.K2Pass;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class ListViewAdaptor extends ArrayAdapter<String> {
    private final Context context;

    private ArrayList<String> nameValues;
    private ArrayList<String> passwordValues;

    public ListViewAdaptor(Context context, ArrayList<String> nameVal, ArrayList<String> passwordVal) {
        super(context, R.layout.rowlayout);
        this.context = context;
        this.nameValues = nameVal;
        this.passwordValues = passwordVal;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.rowlayout, parent, false);

        TextView txtResourceName = (TextView) rowView.findViewById(R.id.txtResourceName);
        TextView textResourcePassword = (TextView) rowView.findViewById(R.id.textResourcePassword);

        String name = nameValues.get(position);
        String passwd = passwordValues.get(position);

        txtResourceName.setText(name);
        textResourcePassword.setText(passwd);

        return rowView;
    }

    public String getName(int index){
        return nameValues.get(index);
    }
    public String getPassword(int index){
        return passwordValues.get(index);
    }

    public void setName(int index, String newName){
        nameValues.set(index, newName);
    }
    public void setPassword(int index, String newPassword){
        passwordValues.set(index, newPassword);
    }

    public void deleteItem(int index){
        nameValues.remove(index);
        passwordValues.remove(index);
    }
    public void changeItemAtPosition(int index, String name, String Password){
        nameValues.remove(index);
        passwordValues.remove(index);

        nameValues.add(index, name);
        passwordValues.add(index, Password);
    }

    @Override
    public int getCount() {
        return nameValues.size();
    }

    public void add(String name, String password) {
        this.nameValues.add(name);
        this.passwordValues.add(password);
        notifyDataSetChanged();
    }
}