package com.ideacarry.example21;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.ideacarry.opengl30jni.databinding.Example21Binding;

/**
 * Created by Raining on 2020/9/12
 * #I# 图片效果测试
 */
public class DemoActivity extends AppCompatActivity {
    private volatile Bitmap mOrgBmp;
    private volatile Bitmap mFilterBmp;
    private Example21Binding mBinding;
    private int mFilterIndex = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mBinding = Example21Binding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());
        mBinding.imageList.setAdapter(new ImageListAdapter());
        mBinding.compareBtn.setOnTouchListener((v, event) -> {
            switch (event.getAction() & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_DOWN:
                    mBinding.imageView.setImageBitmap(mOrgBmp);
                    break;

                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    if (mFilterBmp != null) {
                        mBinding.imageView.setImageBitmap(mFilterBmp);
                    }
                    break;
                default:
                    break;
            }
            return true;
        });
        clickItem(0);
    }

    public void setImage(Bitmap bmp) {
        mOrgBmp = bmp;
        mFilterBmp = null;
        mBinding.imageView.setImageBitmap(mOrgBmp);
    }

    public void onFilterBtn(View view) {
        if (view == mBinding.filter1Btn) {
            mFilterIndex = 1;

            /*new Filter1(getApplicationContext(), mOrgBmp, result -> {
                mFilterBmp = result;
                if (mFilterBmp != null) {
                    mBinding.imageView.setImageBitmap(mFilterBmp);
                }
            });*/

            /*new Filter3LutHigh(getApplicationContext(), mOrgBmp, result -> {
                mFilterBmp = result;
                if (mFilterBmp != null) {
                    mBinding.imageView.setImageBitmap(mFilterBmp);
                }
            });*/

            /*new Filter4BlurNew(getApplicationContext(), mOrgBmp, result -> {
                mFilterBmp = result;
                if (mFilterBmp != null) {
                    mBinding.imageView.setImageBitmap(mFilterBmp);
                }
            });*/

            /*new Filter4BlurNew(getApplicationContext(), mOrgBmp, result -> {
                mFilterBmp = result;
                if (mFilterBmp != null) {
                    mBinding.imageView.setImageBitmap(mFilterBmp);
                }
            });*/

            new Filter5DetectSkinNew(getApplicationContext(), mOrgBmp, result -> {
                mFilterBmp = result;
                if (mFilterBmp != null) {
                    mBinding.imageView.setImageBitmap(mFilterBmp);
                }
            });

        } else if (view == mBinding.filter2Btn) {
            mFilterIndex = 2;

            /*new Filter2(getApplicationContext(), mOrgBmp, result -> {
                mFilterBmp = result;
                if (mFilterBmp != null) {
                    mBinding.imageView.setImageBitmap(mFilterBmp);
                }
            });*/

            /*new Filter3LutLow(getApplicationContext(), mOrgBmp, result -> {
                mFilterBmp = result;
                if (mFilterBmp != null) {
                    mBinding.imageView.setImageBitmap(mFilterBmp);
                }
            });*/

            /*new Filter4BlurOld(getApplicationContext(), mOrgBmp, result -> {
                mFilterBmp = result;
                if (mFilterBmp != null) {
                    mBinding.imageView.setImageBitmap(mFilterBmp);
                }
            });*/

            new Filter5DetectSkinOld(getApplicationContext(), mOrgBmp, result -> {
                mFilterBmp = result;
                if (mFilterBmp != null) {
                    mBinding.imageView.setImageBitmap(mFilterBmp);
                }
            });
        }
    }

    //----------------------------------------------------------------------------------------------
    private static String[] IMAGES = {
            "example21/test1.jpg",
            "example21/test2.jpg",
            "example21/test3.jpg",
            "example21/test4.jpg",
            "example21/test5.jpg",
            "example21/test6.jpg",
            "example21/test7.jpg"
    };

    private class ImageListAdapter extends RecyclerView.Adapter<ItemHolder> {
        @NonNull
        @Override
        public ItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ItemHolder(new Button(parent.getContext()));
        }

        @Override
        public void onBindViewHolder(@NonNull ItemHolder holder, int position) {
            holder.setName(Integer.toString(position));
        }

        @Override
        public int getItemCount() {
            return IMAGES.length;
        }
    }

    private class ItemHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private Button mBtn;

        public ItemHolder(@NonNull View itemView) {
            super(itemView);

            mBtn = (Button) itemView;
            mBtn.setOnClickListener(this);
        }

        public void setName(String name) {
            mBtn.setText(name);
        }

        @Override
        public void onClick(View v) {
            clickItem(getLayoutPosition());
        }
    }

    private void clickItem(int position) {
        setImage(decodeBmp(getApplicationContext(), IMAGES[position]));
    }

    public static Bitmap decodeBmp(Context context, String assetPath) {
        try {
            return BitmapFactory.decodeStream(context.getAssets().open(assetPath));
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }
}
