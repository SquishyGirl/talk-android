/*
 * Nextcloud Talk application
 *
 * @author Mario Danic
 * Copyright (C) 2017-2018 Mario Danic <mario@lovelyhq.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.nextcloud.talk.adapters.items;

import android.annotation.SuppressLint;
import android.view.View;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.nextcloud.talk.R;
import com.nextcloud.talk.application.NextcloudTalkApplication;
import com.nextcloud.talk.models.database.UserEntity;
import com.nextcloud.talk.utils.ApiUtils;
import com.nextcloud.talk.utils.DisplayUtils;
import com.nextcloud.talk.utils.glide.GlideApp;
import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem;
import eu.davidea.flexibleadapter.items.IFilterable;
import eu.davidea.flexibleadapter.items.IFlexible;
import eu.davidea.flexibleadapter.utils.FlexibleUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

public class MentionAutocompleteItem extends AbstractFlexibleItem<UserItem.UserItemViewHolder>
        implements IFilterable<String> {

    private String objectId;
    private String displayName;
    private String source;
    private UserEntity currentUser;

    public MentionAutocompleteItem(String objectId, String displayName, String source, UserEntity currentUser) {
        this.objectId = objectId;
        this.displayName = displayName;
        this.source = source;
        this.currentUser = currentUser;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getObjectId() {
        return objectId;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof MentionAutocompleteItem) {
            MentionAutocompleteItem inItem = (MentionAutocompleteItem) o;
            return (objectId.equals(inItem.objectId) && displayName.equals(inItem.displayName));
        }

        return false;
    }

    @Override
    public int getLayoutRes() {
        return R.layout.rv_item_mention;
    }

    @Override
    public UserItem.UserItemViewHolder createViewHolder(View view, FlexibleAdapter<IFlexible> adapter) {
        return new UserItem.UserItemViewHolder(view, adapter);
    }


    @SuppressLint("SetTextI18n")
    @Override
    public void bindViewHolder(FlexibleAdapter<IFlexible> adapter, UserItem.UserItemViewHolder holder, int position, List<Object> payloads) {

        if (adapter.hasFilter()) {
            FlexibleUtils.highlightText(holder.contactDisplayName, displayName,
                    String.valueOf(adapter.getFilter(String.class)), NextcloudTalkApplication.getSharedApplication()
                            .getResources().getColor(R.color.colorPrimary));
            if (holder.contactMentionId != null) {
                FlexibleUtils.highlightText(holder.contactMentionId, "@" + objectId,
                        String.valueOf(adapter.getFilter(String.class)), NextcloudTalkApplication.getSharedApplication()
                                .getResources().getColor(R.color.colorPrimary));
            }
        } else {
            holder.contactDisplayName.setText(displayName);
            if (holder.contactMentionId != null) {
                holder.contactMentionId.setText("@" + objectId);
            }
        }

        if (source.equals("calls")) {
            holder.avatarFlipView.setFrontImageBitmap(DisplayUtils.getRoundedBitmapFromVectorDrawableResource(NextcloudTalkApplication.getSharedApplication().getResources(), R.drawable.ic_people_group_white_24px));
        } else {
            GlideUrl glideUrl = new GlideUrl(ApiUtils.getUrlForAvatarWithName(currentUser.getBaseUrl(),
                    objectId, R.dimen.avatar_size), new LazyHeaders.Builder()
                    .setHeader("Accept", "image/*")
                    .setHeader("User-Agent", ApiUtils.getUserAgent())
                    .build());

            int avatarSize = Math.round(NextcloudTalkApplication
                    .getSharedApplication().getResources().getDimension(R.dimen.avatar_size));

            GlideApp.with(NextcloudTalkApplication.getSharedApplication().getApplicationContext())
                    .asBitmap()
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .load(glideUrl)
                    .centerInside()
                    .override(avatarSize, avatarSize)
                    .apply(RequestOptions.bitmapTransform(new CircleCrop()))
                    .into(holder.avatarFlipView.getFrontImageView());
        }
    }

    @Override
    public boolean filter(String constraint) {
        return objectId != null && StringUtils.containsIgnoreCase(objectId, constraint) ||
                displayName != null && StringUtils.containsIgnoreCase(displayName, constraint);

    }
}
