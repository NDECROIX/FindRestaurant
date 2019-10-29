package com.decroix.nicolas.go4lunch.view.holders;

import android.net.Uri;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.decroix.nicolas.go4lunch.R;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Handles views in the header of the navigation view
 */
public class HeaderViewHolder {
    @BindView(R.id.main_activity_nav_header_avatar)
    public ImageView mAvatar;
    @BindView(R.id.main_activity_nav_header_name)
    public TextView mName;
    @BindView(R.id.main_activity_nav_header_email)
    public TextView mEmail;

    private View view;

    public HeaderViewHolder(View view) {
        this.view = view;
        ButterKnife.bind(this, view);
    }

    public void fillView(Uri photoUrl, String name, String email){
        Glide.with(view)
                .load(photoUrl)
                .apply(RequestOptions.circleCropTransform())
                .into(mAvatar);
        mName.setText(name);
        mEmail.setText(email);
    }
}
