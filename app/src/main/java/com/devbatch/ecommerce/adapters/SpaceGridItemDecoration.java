package com.devbatch.ecommerce.adapters;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by irfan arshad on 6/28/2016.
 */
public class SpaceGridItemDecoration extends RecyclerView.ItemDecoration {
    private int space;
    private int column;
    private boolean includeEdge;

    public SpaceGridItemDecoration(int space, int column, boolean includeEdge) {
        this.space = space;
        this.column = column;
        this.includeEdge = includeEdge;
    }
//
//    //2,5,8,11,14  (3-0+2)
//    @Override
//    public void getItemOffsets(Rect outRect, View view,
//                               RecyclerView parent, RecyclerView.State state) {
//
//
//
//        outRect.bottom = space;
//        int position = parent.getChildLayoutPosition(view);
//        if((column*(position+1))%column == 0)
//        {
//            outRect.right = space;
//            outRect.left = space;
//        }
//        else {
//            outRect.right = 0;
//            outRect.left = 0;
//        }
//        //((column*position+1)-1)
//        //(4x(0+1))-1 = 3
//        //(4x(1+1))-1 = 7
//        //(4x(2+1))-1 = 11
////        int index = (column * (position + 1)) - 1;
////        if (position == index) {
////            outRect.left = 0;
////        }
//
//        // Add top margin only for the first item to avoid double space between items
//        if (parent.getChildLayoutPosition(view) < column) {
//            outRect.top = space;
//        } else {
//            outRect.top = 0;
//        }
//    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        int position = parent.getChildAdapterPosition(view); // item position
        int column = position % this.column; // item column

        if (includeEdge) {
            outRect.left = this.space - column * this.space / this.column; // spacing - column * ((1f / spanCount) * spacing)
            outRect.right = (column + 1) * this.space / this.column; // (column + 1) * ((1f / spanCount) * spacing)

            if (position < this.column) { // top edge
                outRect.top = this.space;
            }
            outRect.bottom = this.space; // item bottom
        } else {
            outRect.left = column * this.space / this.column; // column * ((1f / spanCount) * spacing)
            outRect.right = this.space - (column + 1) * this.space / this.column; // spacing - (column + 1) * ((1f /    spanCount) * spacing)
            if (position >= this.space) {
                outRect.top = this.space; // item top
            }
        }
    }
}
