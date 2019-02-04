/*
 * Copyright (C) 2019 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.customization.model.theme;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.PathShape;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;
import androidx.core.graphics.PathParser;

import com.android.customization.model.CustomizationOption;
import com.android.wallpaper.R;
import com.android.wallpaper.asset.ResourceAsset;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a Theme component available in the system as a "persona" bundle.
 * Note that in this context a Theme is not related to Android's Styles, but it's rather an
 * abstraction representing a series of overlays to be applied to the system.
 */
public class ThemeBundle implements CustomizationOption {

    private final String mTitle;
    private final PreviewInfo mPreviewInfo;
    private final String mFontOverlayPackage;
    private final String mColorOverlayPackage;
    private final String mShapeOverlayPackage;
    private final List<String> mIconOverlayPackages;
    private final boolean mIsDefault;

    private ThemeBundle(String title, @Nullable String fontOverlayPackage,
            @Nullable String colorOverlayPackage, @Nullable String shapeOverlayPackage,
            List<String> iconOverlayPackages, boolean isDefault, PreviewInfo previewInfo) {
        mTitle = title;
        mFontOverlayPackage = fontOverlayPackage;
        mColorOverlayPackage = colorOverlayPackage;
        mShapeOverlayPackage = shapeOverlayPackage;
        mIconOverlayPackages = Collections.unmodifiableList(iconOverlayPackages);
        mIsDefault = isDefault;
        mPreviewInfo = previewInfo;
    }

    @Override
    public String getTitle() {
        return mTitle;
    }

    @Override
    public void bindThumbnailTile(View view) {
        Resources res = view.getContext().getResources();

        ((ImageView) view.findViewById(R.id.theme_option_color)).setImageTintList(
                ColorStateList.valueOf(mPreviewInfo.colorAccentLight));
        ((TextView) view.findViewById(R.id.theme_option_font)).setTypeface(
                mPreviewInfo.headlineFontFamily);
        if (mPreviewInfo.shapeDrawable != null) {
            ((ShapeDrawable)mPreviewInfo.shapeDrawable).getPaint().setColor(res.getColor(
                    R.color.shape_thumbnail_color, null));
            ((ImageView) view.findViewById(R.id.theme_option_shape)).setImageDrawable(
                    mPreviewInfo.shapeDrawable);
        }
        if (!mPreviewInfo.icons.isEmpty()) {
            Drawable icon = mPreviewInfo.icons.get(0).mutate();
            icon.setTint(res.getColor(R.color.icon_thumbnail_color, null));
            ((ImageView) view.findViewById(R.id.theme_option_icon)).setImageDrawable(
                    icon);
        }
    }

    @Override
    public boolean isActive(Context context) {
        return false;
    }

    @Override
    public int getLayoutResId() {
        return R.layout.theme_option;
    }

    public PreviewInfo getPreviewInfo() {
        return mPreviewInfo;
    }

    String getFontOverlayPackage() {
        return mFontOverlayPackage;
    }

    String getColorOverlayPackage() {
        return mColorOverlayPackage;
    }

    String getShapeOverlayPackage() {
        return mShapeOverlayPackage;
    }

    List<String> getIconOverlayPackages() {
        return mIconOverlayPackages;
    }

    boolean isDefault() {
        return mIsDefault;
    }

    public static class PreviewInfo {
        public final Typeface bodyFontFamily;
        public final Typeface headlineFontFamily;
        @ColorInt public final int colorAccentLight;
        @ColorInt public final int colorAccentDark;
        public final List<Drawable> icons;
        public final Drawable shapeDrawable;
        @DrawableRes public final int wallpaperDrawableRes;
        public final ResourceAsset colorPreviewDrawable;
        public final ResourceAsset shapePreviewDrawable;

        private PreviewInfo(Typeface bodyFontFamily, Typeface headlineFontFamily,
                int colorAccentLight, int colorAccentDark, List<Drawable> icons,
                Drawable shapeDrawable, int wallpaperDrawableRes,
                ResourceAsset colorPreviewDrawable, ResourceAsset shapePreviewDrawable) {
            this.bodyFontFamily = bodyFontFamily;
            this.headlineFontFamily = headlineFontFamily;
            this.colorAccentLight = colorAccentLight;
            this.colorAccentDark = colorAccentDark;
            this.icons = icons;
            this.shapeDrawable = shapeDrawable;
            this.wallpaperDrawableRes = wallpaperDrawableRes;
            this.colorPreviewDrawable = colorPreviewDrawable;
            this.shapePreviewDrawable = shapePreviewDrawable;
        }
    }

    public static class Builder {
        private static final float PATH_SIZE = 100f;
        private String mTitle;
        private Typeface mBodyFontFamily;
        private Typeface mHeadlineFontFamily;
        @ColorInt private int mColorAccentLight;
        @ColorInt private int mColorAccentDark;
        private List<Drawable> mIcons = new ArrayList<>();
        private String mShapePath;
        private boolean mIsDefault;
        @DrawableRes private int mWallpaperDrawableResId;
        private ResourceAsset mColorPreview;
        private ResourceAsset mShapePreview;

        private String mFontOverlayPackage;
        private String mColorsPackage;
        private List<String> mIconPackages = new ArrayList<>();
        private String mShapePackage;

        public ThemeBundle build() {
            ShapeDrawable shapeDrawable = null;
            if (!TextUtils.isEmpty(mShapePath)) {
                PathShape shape = new PathShape(PathParser.createPathFromPathData(mShapePath),
                                PATH_SIZE, PATH_SIZE);
                shapeDrawable = new ShapeDrawable(shape);
                shapeDrawable.setIntrinsicHeight((int) PATH_SIZE);
                shapeDrawable.setIntrinsicWidth((int) PATH_SIZE);
            }
            return new ThemeBundle(mTitle, mFontOverlayPackage, mColorsPackage, mShapePackage,
                    mIconPackages, mIsDefault,
                    new PreviewInfo(mBodyFontFamily, mHeadlineFontFamily, mColorAccentLight,
                            mColorAccentDark, mIcons, shapeDrawable, mWallpaperDrawableResId,
                            mColorPreview, mShapePreview));
        }

        public Builder setTitle(String title) {
            mTitle = title;
            return this;
        }

        public Builder setFontOverlayPackage(String packageName) {
            mFontOverlayPackage = packageName;
            return this;
        }

        public Builder setBodyFontFamily(@Nullable Typeface bodyFontFamily) {
            mBodyFontFamily = bodyFontFamily;
            return this;
        }

        public Builder setHeadlineFontFamily(@Nullable Typeface headlineFontFamily) {
            mHeadlineFontFamily = headlineFontFamily;
            return this;
        }

        public Builder setColorPackage(String packageName) {
            mColorsPackage = packageName;
            return this;
        }

        public Builder setColorAccentLight(@ColorInt int colorAccentLight) {
            mColorAccentLight = colorAccentLight;
            return this;
        }

        public Builder setColorAccentDark(@ColorInt int colorAccentDark) {
            mColorAccentDark = colorAccentDark;
            return this;
        }

        public Builder addIcon(Drawable icon) {
            mIcons.add(icon);
            return this;
        }

        public Builder addIconPackage(String packageName) {
            mIconPackages.add(packageName);
            return this;
        }

        public Builder setShapePackage(String packageName) {
            mShapePackage = packageName;
            return this;
        }

        public Builder setShapePath(String path) {
            mShapePath = path;
            return this;
        }

        public Builder setColorPreview(ResourceAsset colorPreview) {
            mColorPreview = colorPreview;
            return this;
        }

        public Builder setShapePreview(ResourceAsset shapePreview) {
            mShapePreview = shapePreview;
            return this;
        }

        public Builder asDefault() {
            mIsDefault = true;
            return this;
        }
    }
}