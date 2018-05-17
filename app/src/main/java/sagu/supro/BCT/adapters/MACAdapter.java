package sagu.supro.BCT.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import sagu.supro.BCT.R;

public class MACAdapter extends BaseAdapter {

    private List<String> macList;
    private Context context;

    public MACAdapter(List<String> macList, Context context){
        this.macList = macList;
        this.context = context;
    }

    @Override
    public int getCount() {
        return macList.size();
    }

    @Override
    public Object getItem(int position) {
        return macList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @SuppressLint({"InflateParams", "ViewHolder"})
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView = layoutInflater.inflate(R.layout.mac_row,null);
        final TextView tvMAC = convertView.findViewById(R.id.tvMAC);
        ImageView ivClear = convertView.findViewById(R.id.ivClear);

        tvMAC.setText(position+1+".  "+macList.get(position));
        ivClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                macList.set(position, "MAC");
                tvMAC.setText(position+1+".  MAC");
            }
        });

        return convertView;
    }

    public List<String> getMacList(){
        return macList;
    }

}
