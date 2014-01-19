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
package com.docd.purefm.browser;

import com.docd.purefm.Extras;
import com.docd.purefm.R;
import com.docd.purefm.activities.BrowserActivity;
import com.docd.purefm.activities.SearchActivity;
import com.docd.purefm.settings.SettingsActivity;
import com.docd.purefm.adapters.BrowserBaseAdapter;
import com.docd.purefm.dialogs.CreateDirectoryDialog;
import com.docd.purefm.dialogs.CreateFileDialog;
import com.docd.purefm.dialogs.PartitionInfoDialog;
import com.docd.purefm.settings.Settings;
import com.docd.purefm.tasks.PasteTaskExecutor;
import com.docd.purefm.utils.FileSortType;

import android.app.DialogFragment;
import android.content.Intent;
import android.view.MenuItem;

final class MenuController {
    
    private final BrowserActivity activity;
    private final Browser browser;
    
    private BrowserBaseAdapter adapter;
    
    protected MenuController(BrowserActivity activity, Browser browser) {
        this.activity = activity;
        this.browser = browser;
    }
    
    protected void setAdapter(BrowserBaseAdapter adapter) {
        this.adapter = adapter;
    }
    
    public boolean onMenuItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case android.R.id.home:
            if (!this.browser.isRoot()) {
                this.browser.up();
                return true;
            }
            return false;

        case android.R.id.paste:
            final PasteTaskExecutor ptc = new PasteTaskExecutor(activity, browser.getPath());
            ptc.start();
            return true;
            
        case android.R.id.content:
            if (Settings.appearance == Settings.APPEARANCE_LIST) {
                Settings.saveAppearance(activity, Settings.APPEARANCE_GRID);
            } else {
                Settings.saveAppearance(activity, Settings.APPEARANCE_LIST);
            }
            activity.invalidateList();
            return true;
            
        case R.id.refresh:
            browser.invalidate();
            return true;
            
        case R.id.menu_search:
            final Intent searchIntent = new Intent(activity, SearchActivity.class);
            searchIntent.putExtra(Extras.EXTRA_PATH, browser.getPath().getAbsolutePath());
            activity.startActivity(searchIntent);
            return true;

        case R.id.menu_settings:
            activity.startActivityForResult(new Intent(activity, SettingsActivity.class), BrowserActivity.REQUEST_CODE_SETTINGS);
            return true;

        case R.id.menu_folder_new:
            final DialogFragment cd = CreateDirectoryDialog.instantiate(browser.getPath().toFile());
            cd.show(activity.getFragmentManager(), BrowserActivity.TAG_DIALOG);
            return true;

        case R.id.menu_file_new:
            final DialogFragment cf = CreateFileDialog.instantiate(browser.getPath().toFile());
            cf.show(activity.getFragmentManager(), BrowserActivity.TAG_DIALOG);
            return true;
            
        case R.id.menu_partition:
            final DialogFragment pid = PartitionInfoDialog.instantiate(browser.getPath());
            pid.show(activity.getFragmentManager(), BrowserActivity.TAG_DIALOG);
            return true;
            
        case R.id.menu_sort_name_asc:
            adapter.setCompareType(FileSortType.NAME_ASC);
            return true;
            
        case R.id.menu_sort_name_desc:
            adapter.setCompareType(FileSortType.NAME_DESC);
            return true;
            
        case R.id.menu_sort_type_asc:
            adapter.setCompareType(FileSortType.TYPE_ASC);
            return true;
            
        case R.id.menu_sort_type_desc:
            adapter.setCompareType(FileSortType.TYPE_DESC);
            return true;
            
        case R.id.menu_sort_date_asc:
            adapter.setCompareType(FileSortType.DATE_ASC);
            return true;
            
        case R.id.menu_sort_date_desc:
            adapter.setCompareType(FileSortType.DATE_DESC);
            return true;
            
        case R.id.menu_sort_size_asc:
            adapter.setCompareType(FileSortType.SIZE_ASC);
            return true;
            
        case R.id.menu_sort_size_desc:
            adapter.setCompareType(FileSortType.SIZE_DESC);
            return true;

        default:
            return false;
        }
    }
}
