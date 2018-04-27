package com.devbatch.ecommerce.fragment;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.devbatch.ecommerce.Application;
import com.devbatch.ecommerce.R;
import com.devbatch.ecommerce.adapters.RecyclerViewArrayAdapter;
import com.devbatch.ecommerce.communication.network.CallbacksManager;
import com.devbatch.ecommerce.communication.network.request.ProductRequest;
import com.devbatch.ecommerce.communication.network.rersponse.Product;
import com.devbatch.ecommerce.communication.network.rersponse.Products;
import com.devbatch.ecommerce.utils.CommonKeys;
import com.devbatch.ecommerce.utils.CommonUtil;
import com.devbatch.ecommerce.utils.RecyclerViewOnItemClick;
import com.devbatch.ecommerce.widgets.ChompProgressImageView;

import retrofit.RetrofitError;
import retrofit.client.Response;

import static com.devbatch.ecommerce.utils.ViewUtils.getScreenWidth;

/**
 * Created by DevBatch on 12/30/2016.
 */

public class HomeFragment extends BaseFragment {
    ChompProgressImageView chompProgressView;
    Drawable mPizzaDrawable;
    int progress;
    int endTime = 10;
    CountDownTimer countDownTimer;

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_home;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        chompProgressView = (ChompProgressImageView) view.findViewById(R.id.chompProgressView);
        mPizzaDrawable = getResources().getDrawable(R.drawable.pizza);
        chompProgressView.setImageDrawableChomp(mPizzaDrawable);
        chompProgressView.setBiteRadius(200);
        eatNosh(440, mPizzaDrawable, false);
        chompProgressView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                countDownTimer = new CountDownTimer(endTime * 1000 /*finishTime**/, 10 /*interval**/) {
                    @Override
                    public void onTick(long millisUntilFinished) {

                        if (chompProgressView.getChompProgress() >= 100) {
                            chompProgressView.removeBites();
                            progress = 0;
                            chompProgressView.setChompProgress(progress);
                            chompProgressView.setTotalNumberOfBitesTaken(0);
                        } else {
                            progress = progress + 1;
                            chompProgressView.setChompProgress(progress);

                        }
                    }

                    @Override
                    public void onFinish() {
                        Application.toast("Finish");
                        chompProgressView.removeBites();
                        chompProgressView.setTotalNumberOfBitesTaken(0);
                    }
                };
                countDownTimer.start(); // start timer

            }
        });
        productsBySellerAndStoreID();
    }

    private void productsBySellerAndStoreID() {
        CallbacksManager.CancelableCallback<Products> callback = callbacksManager.new CancelableCallback<Products>() {
            @Override
            protected void onSuccess(Products products, Response response) {

            }

            @Override
            protected void onFailure(RetrofitError error) {

            }
        };
        apiFor(callback).productsBySellerAndStoreID(new ProductRequest(), callback);

    }

    public void stopCountDown(View view) {


        countDownTimer.cancel();
    }

    private void eatNosh(int biteSize, Drawable imageDrawable, boolean isChompFromTop) {
//        if (mChompNoshTask != null) {
//            mChompNoshTask.cancel(true);
//            mChompNoshTask = null;
//            mChompProgressImageView.setChompProgress(0);
//        }

        chompProgressView.setImageDrawableChomp(imageDrawable);
        chompProgressView.setChompDirection(isChompFromTop ?
                ChompProgressImageView.ChompDirection.TOP :
                ChompProgressImageView.ChompDirection.RANDOM);

        //chompProgressView.removeBites();


//        mChompNoshTask = new ChompNoshTask(mChompProgressImageView, this).execute(true);
    }

    //    private void stopEating() {
//        if (mChompNoshTask != null) {
//            mChompNoshTask.cancel(true);
//            mChompNoshTask = null;
//            mChompProgressImageView.setChompProgress(0);
//        }
//    }
    public String getToolBarTile() {
        return null;
    }

    public class HomeAdapter extends
            RecyclerViewArrayAdapter<Product, HomeAdapter.ViewHolder> {
        private RecyclerViewOnItemClick mOnItemClickListener;
        private final int cellSize;
        private boolean lockedAnimations = false;
        private int lastAnimatedItem = -1;
        private static final int PHOTO_ANIMATION_DELAY = 200;
        private final Interpolator INTERPOLATOR = new DecelerateInterpolator();

        public HomeAdapter(Context context) {
            super(context);
            this.cellSize = getScreenWidth(context) / CommonKeys.CATEGORY_GRID_COUNT;
        }

        public HomeAdapter(Context context, RecyclerViewOnItemClick onItemClickListener) {
            super(context);
            this.cellSize = getScreenWidth(context) / CommonKeys.CATEGORY_GRID_COUNT;
            this.mOnItemClickListener = onItemClickListener;
        }

        private void onItemLongClick(HomeAdapter.ViewHolder viewHolder) {
            if (mOnItemClickListener != null) {
                mOnItemClickListener.onItemLongClick(viewHolder.getPosition(), this, viewHolder.itemView);
            }
        }

        public void setOnItemClickListener(RecyclerViewOnItemClick onItemClickListener) {
            mOnItemClickListener = onItemClickListener;
        }

        public void onItemClick(RecyclerView.ViewHolder viewHolder) {
            if (mOnItemClickListener != null) {
                mOnItemClickListener.onItemClick(viewHolder.getPosition(), this, viewHolder.itemView);
            }
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_home, parent, false);
            StaggeredGridLayoutManager.LayoutParams layoutParams = (StaggeredGridLayoutManager.LayoutParams) view.getLayoutParams();
            layoutParams.height = cellSize + CommonUtil.dp2px(35);
            layoutParams.width = StaggeredGridLayoutManager.LayoutParams.MATCH_PARENT;
            layoutParams.setFullSpan(false);
            view.setLayoutParams(layoutParams);
            return new ViewHolder(view, this);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, final int position) {


        }


        public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
            public ImageView image;
            public TextView name;
            HomeAdapter mAdapter;
            public FrameLayout imageFrame;


            public ViewHolder(View itemView, HomeAdapter adapter) {
                super(itemView);
                itemView.setOnClickListener(this);
                this.mAdapter = adapter;

            }

            @Override
            public void onClick(View v) {
                mAdapter.onItemClick(this);
            }

            @Override
            public boolean onLongClick(View v) {
                return false;
            }
        }

        private void animatePhoto(ViewHolder viewHolder) {
            if (!lockedAnimations) {
                if (lastAnimatedItem == viewHolder.getAdapterPosition()) {
                    lockedAnimations = true;
                }

                long animationDelay = PHOTO_ANIMATION_DELAY + viewHolder.getPosition() * 20;

                viewHolder.itemView.setScaleY(0);
                viewHolder.itemView.setScaleX(0);

                viewHolder.itemView.animate()
                        .scaleY(1)
                        .scaleX(1)
                        .setDuration(100)
                        .setInterpolator(INTERPOLATOR)
                        .setStartDelay(animationDelay)
                        .start();
            }
        }
    }


}
