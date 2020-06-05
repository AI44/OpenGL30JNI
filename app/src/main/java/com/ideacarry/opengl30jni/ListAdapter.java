package com.ideacarry.opengl30jni;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ListAdapter extends RecyclerView.Adapter<ListAdapter.ItemHolder> {

    private List<ItemData> mData;
    private OnItemClickListener mListener;

    public ListAdapter(List<ItemData> data) {
        mData = data;
    }

    /**
     * 设置点击事件回调
     */
    public void addItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    @NonNull
    @Override
    public ItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ItemHolder(new TextView(parent.getContext()));
    }

    @Override
    public void onBindViewHolder(@NonNull ItemHolder holder, int position) {
        if (mData != null) {
            holder.setTitle(mData.get(position).getName());
        }
    }

    @Override
    public int getItemCount() {
        if (mData != null) {
            return mData.size();
        }
        return 0;
    }

    private int colorIndex = -1;

    private int getColor() {
        final int[] colorArr = new int[]{
                0xffffe4e1,
                0xfff5fffa,
                0xfff0e68c,
                0xffdcdcdc,
                0xffffc0cb,
                0xff90ee90,
                0xffb0e0e6};
        int index;
        do {
            index = (int) (Math.random() * colorArr.length);
        }
        while (index == colorIndex);
        colorIndex = index;
        return colorArr[index];
    }

    class ItemHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public ItemHolder(@NonNull View itemView) {
            super(itemView);

            RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT);
            int padding = DensityUtils.dip2px(itemView.getContext(), 12);
            TextView tex = (TextView) itemView;
            tex.setPadding(0, padding, 0, padding);
            tex.setTextSize(20);
            tex.setBackgroundColor(getColor());
            tex.setLayoutParams(lp);
            tex.setOnClickListener(this);
        }

        public void setTitle(String name) {
            ((TextView) itemView).setText(name);
        }

        @Override
        public void onClick(View v) {
            if (mListener != null) {
                mListener.onItemClick(mData.get(getLayoutPosition()));
            }
        }
    }

    // 点击事件回调
    public interface OnItemClickListener {

        void onItemClick(ItemData data);
    }
}
