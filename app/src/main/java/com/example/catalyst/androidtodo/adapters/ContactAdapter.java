package com.example.catalyst.androidtodo.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.catalyst.androidtodo.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dsloane on 3/17/2016.
 */
public class ContactAdapter extends BaseAdapter {

    private Context mContext;
    private LayoutInflater inflater;
    private List<String> contacts = new ArrayList<String>();

    public final static String TAG = ContactAdapter.class.getSimpleName();

    public ContactAdapter(Context context, List<String> contactList) {
        mContext = context;
        contacts = contactList;
    }

    public void clear() {
        contacts.clear();
    }

    @Override
    public int getCount() {
        return contacts.size();
    }

    @Override
    public Object getItem(int location) {
        return contacts.get(location);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder = new ViewHolder();

        if (inflater == null) {
            inflater = (LayoutInflater) mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.list_contacts, null);
            holder.name = (TextView) convertView.findViewById(R.id.contact_name);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        String s = contacts.get(position);
        holder.name.setText(s);

        return convertView;
    }






    private static class ViewHolder {
        TextView name;
    }
}
