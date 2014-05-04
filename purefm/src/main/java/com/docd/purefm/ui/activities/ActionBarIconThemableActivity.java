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
package com.docd.purefm.ui.activities;

import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.docd.purefm.R;
import com.docd.purefm.commandline.ShellHolder;
import com.stericson.RootTools.execution.Shell;

/**
 * Manages ActionBar icon. If superuser is enabled, the ActionBar
 * icon is ic_superuser, default otherwise
 *
 *
 * @author Doctoror
 */
public abstract class ActionBarIconThemableActivity extends ThemableActivity implements
        ShellHolder.OnShellChangedListener {

    private ShellHolder mShellHolder;

    /**
     * Default Activity icon
     */
    private Drawable mDefaultIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            final PackageManager packageManager = getPackageManager();
            if (packageManager == null) {
                throw new RuntimeException("PackageManager is null");
            }
            mDefaultIcon = packageManager.getActivityIcon(this.getComponentName());
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            mDefaultIcon = getResources().getDrawable(R.drawable.ic_fso_folder);
        }

        mShellHolder = ShellHolder.getInstance();
        mShellHolder.addOnShellChangedListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mShellHolder.removeOnShellChangedListener(this);
    }

    @Override
    public final void onShellChanged(@Nullable final Shell shell, final boolean isRootShell) {
        invalidateActionBarIcon(shell, isRootShell);
    }

    @Override
    protected void onResume() {
        super.onResume();
        this.invalidateActionBarIcon(mShellHolder.getShell(), mShellHolder.isCurrentShellRoot());
    }

    @NonNull
    protected final ShellHolder getShellHolder() {
        if (mShellHolder == null) {
            throw new IllegalStateException("getShellHolder() can be called only after onCreate()");
        }
        return mShellHolder;
    }

    /**
     * Sets ActionBar icon to ic_superuser if superuser enabled.
     */
    protected final void invalidateActionBarIcon() {
        final Shell shell = getSettings().useCommandLine() ?
                mShellHolder.getShell() : null;
        invalidateActionBarIcon(shell, mShellHolder.isCurrentShellRoot());
    }

    /**
     * Sets ActionBar icon to ic_superuser if superuser enabled.
     */
    protected final void invalidateActionBarIcon(@Nullable final Shell shell,
                                                 final boolean isRootShell) {
        if (isRootShell) {
            setActionBarIcon(getResources().getDrawable(R.drawable.ic_root));
        } else if (getSettings().useCommandLine() && shell != null) {
            setActionBarIcon(getResources().getDrawable(R.drawable.ic_shell));
        } else {
            setActionBarIcon(mDefaultIcon);
        }
    }

    protected abstract void setActionBarIcon(final Drawable icon);
}
