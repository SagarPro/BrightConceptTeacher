package sagu.supro.BCT.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import sagu.supro.BCT.R;
import sagu.supro.BCT.dynamo.UserDetailsDO;

public class UsersAdapter extends BaseAdapter {

    private List<UserDetailsDO> userList;
    private Context context;

    public UsersAdapter(List<UserDetailsDO> userList, Context context){
        this.userList = userList;
        this.context = context;
    }

    @Override
    public int getCount() {
        return userList.size();
    }

    @Override
    public Object getItem(int position) {
        return userList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @SuppressLint({"InflateParams", "ViewHolder"})
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView = layoutInflater.inflate(R.layout.user_row,null);
        TextView userName,devices,status;
        userName = convertView.findViewById(R.id.tv_user_name);
        devices = convertView.findViewById(R.id.tv_devices);
        status = convertView.findViewById(R.id.tv_status);

        userName.setText(userList.get(position).getUserName());
        devices.append(userList.get(position).getNoOfDevices());
        status.append(userList.get(position).getStatus());

        return convertView;
    }
}
