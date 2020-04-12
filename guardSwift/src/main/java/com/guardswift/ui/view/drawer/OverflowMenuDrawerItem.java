package com.guardswift.ui.view.drawer;


import android.content.Context;
import androidx.annotation.LayoutRes;
import androidx.appcompat.widget.PopupMenu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.guardswift.R;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.materialdrawer.model.BaseDescribeableDrawerItem;
import com.mikepenz.materialdrawer.model.BaseViewHolder;

import java.util.List;

public class OverflowMenuDrawerItem extends BaseDescribeableDrawerItem<OverflowMenuDrawerItem, OverflowMenuDrawerItem.ViewHolder> {

    private int mMenu;
    private boolean mShowOptions;

    private String mBottomEndCaption = "";

    public OverflowMenuDrawerItem withMenu(int menu) {
        this.mMenu = menu;
        this.mShowOptions = true;
        return this;
    }

    public int getMenu() {
        return mMenu;
    }

    public OverflowMenuDrawerItem withBottomEndCaption(String caption) {
        this.mBottomEndCaption = caption;
        return this;
    }

    private PopupMenu.OnMenuItemClickListener mOnMenuItemClickListener;

    public OverflowMenuDrawerItem withOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener onMenuItemClickListener) {
        this.mOnMenuItemClickListener = onMenuItemClickListener;
        return this;
    }

    public PopupMenu.OnMenuItemClickListener getOnMenuItemClickListener() {
        return mOnMenuItemClickListener;
    }

    private PopupMenu.OnDismissListener mOnDismissListener;


    public OverflowMenuDrawerItem withOnDismissListener(PopupMenu.OnDismissListener onDismissListener) {
        this.mOnDismissListener = onDismissListener;
        return this;
    }

    public PopupMenu.OnDismissListener getOnDismissListener() {
        return mOnDismissListener;
    }

    @Override
    public int getType() {
        return R.id.material_drawer_item_overflow_menu;
    }

    @Override
    @LayoutRes
    public int getLayoutRes() {
        return R.layout.material_drawer_item_overflow_menu_primary;
    }

    @Override
    public void bindView(ViewHolder viewHolder, List<Object> payloads) {
        super.bindView(viewHolder, payloads);

        Context ctx = viewHolder.itemView.getContext();

        //bind the basic view parts
        bindViewHelper(viewHolder);

        TextView captionTv = viewHolder.itemView.findViewById(R.id.material_drawer_bottom_end_caption);
        captionTv.setText(mBottomEndCaption);

        if (this.mShowOptions) {
            //handle menu click
            viewHolder.menu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    PopupMenu popup = new PopupMenu(view.getContext(), view);
                    MenuInflater inflater = popup.getMenuInflater();
                    inflater.inflate(mMenu, popup.getMenu());

                    popup.setOnMenuItemClickListener(mOnMenuItemClickListener);
                    popup.setOnDismissListener(mOnDismissListener);

                    popup.show();
                }
            });

            //handle image
            viewHolder.menu.setImageDrawable(new IconicsDrawable(ctx, GoogleMaterial.Icon.gmd_more_vert).sizeDp(12).color(getIconColor(ctx)));

            viewHolder.menu.setVisibility(View.VISIBLE);
        } else {
            viewHolder.menu.setVisibility(View.INVISIBLE);
        }



        //call the onPostBindView method to trigger post bind view actions (like the listener to modify the item if required)
        onPostBindView(this, viewHolder.itemView);
    }

    @Override
    public ViewHolder getViewHolder(View v) {
        return new ViewHolder(v);
    }


    public static class ViewHolder extends BaseViewHolder {
        //protected ImageButton ibOverflow;
        private ImageButton menu;

        public ViewHolder(View view) {
            super(view);
            this.menu = view.findViewById(R.id.material_drawer_menu_overflow);
        }
    }
}
