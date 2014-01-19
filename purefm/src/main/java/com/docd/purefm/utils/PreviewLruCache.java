/*
 * Copyright 2014 Yaroslav Mytkalyk
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
package com.docd.purefm.utils;

import java.io.File;

import com.docd.purefm.R;
import com.docd.purefm.drawable.RecyclingBitmapDrawable;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.provider.MediaStore.Video.Thumbnails;
import android.util.LruCache;

import org.jetbrains.annotations.Nullable;

public final class PreviewLruCache
        extends LruCache<File, RecyclingBitmapDrawable>
{
    private final int width;
    private final Resources res;
    private final PackageManager packageManager;

    public PreviewLruCache(Context context)
    {
        super(1024 * 1024);
        this.res = context.getResources();
        this.width = (int) res.getDimension(R.dimen.preview_width);
        this.packageManager = context.getPackageManager();
    }

    @Nullable
    @Override
    protected RecyclingBitmapDrawable create(final File key)
    {
        final boolean isImage = MimeTypes.isPicture(key);
        final boolean isVideo = MimeTypes.isVideo(key);
        final boolean isApk = key.getName().endsWith(".apk");
                
        if (isImage) {
            return new RecyclingBitmapDrawable(this.res,
                    PureFMThumbnailUtils.createPictureThumbnail(key, this.width),
                            true);
        } else if (isVideo) {
            return new RecyclingBitmapDrawable(this.res, PureFMThumbnailUtils
                    .createVideoThumbnail(key.getAbsolutePath(), Thumbnails.MICRO_KIND),
                            true);
        } else if (isApk) {
            return new RecyclingBitmapDrawable(this.res,
                    PureFMThumbnailUtils.extractApkIcon(packageManager, key), true);
        }
        
        return null;
    }

    @Override
    protected int sizeOf(File key, RecyclingBitmapDrawable value) {
        return value.getBitmap().getByteCount() / 1024;
    }

    @Override
    protected void entryRemoved(boolean evicted, File key, RecyclingBitmapDrawable oldValue,
            RecyclingBitmapDrawable newValue)
    {
        oldValue.setIsCached(false);
    }

}
