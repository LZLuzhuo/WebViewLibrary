package me.luzhuo.webviewdemo.lib_base;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.util.Pair;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import me.luzhuo.webviewdemo.R;

public class WebViewShareAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<Pair<Integer, String>> mDatas = new ArrayList<>();
    private OnShareCallback callback;

    public WebViewShareAdapter(List<Pair<Integer, String>> datas, OnShareCallback callback) {
        this.callback = callback;
        setData(datas);
    }

    public void setData(List<Pair<Integer, String>> data) {
        this.mDatas.addAll(data);
        notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new RecyclerHolder(android.view.LayoutInflater.from(parent.getContext()).inflate(R.layout.item_webview_share, parent, false));
    }

    @Override
    public int getItemCount() {
        return mDatas.size();
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ((RecyclerHolder) holder).bindData(mDatas.get(position));
    }

    public class RecyclerHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public ImageView ccc_share_icon;
        public TextView ccc_share_title;

        public RecyclerHolder(View itemView) {
            super(itemView);
            ccc_share_icon = itemView.findViewById(R.id.ccc_share_icon);
            ccc_share_title = itemView.findViewById(R.id.ccc_share_title);
            itemView.findViewById(R.id.ccc_share_parent).setOnClickListener(this);
        }

        public void bindData(Pair<Integer, String> data) {
            ccc_share_icon.setImageResource(data.first);
            ccc_share_title.setText(data.second);
        }

        @Override
        public void onClick(View v) {
            callback.onShareCallback(getLayoutPosition(), mDatas.get(getLayoutPosition()));
        }
    }

    public interface OnShareCallback{
        public void onShareCallback(int position, Pair<Integer, String> data);
    }
}
