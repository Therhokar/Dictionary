// Copyright 2011 Google Inc. All Rights Reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.hughes.android.dictionary;

import java.io.File;
import java.util.List;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.TypedValue;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hughes.android.util.IntentLauncher;
import com.hughes.util.StringUtil;

public class DictionaryManagerActivity extends ListActivity {

  static final String LOG = "QuickDic";
  static boolean canAutoLaunch = true;

  DictionaryApplication application;
  Adapter adapter;
  
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Log.d(LOG, "onCreate:" + this);
    
    application = (DictionaryApplication) getApplication();

    // UI init.
    setContentView(R.layout.list_activity);

    getListView().setOnItemClickListener(new OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> arg0, View arg1, int index,
          long id) {
        onClick(index);
      }
    });
    
    getListView().setClickable(true);

    // ContextMenu.
    registerForContextMenu(getListView());

    final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
    final String thanksForUpdatingLatestVersion = getString(R.string.thanksForUpdatingVersion);
    if (!prefs.getString(C.THANKS_FOR_UPDATING_VERSION, "").equals(thanksForUpdatingLatestVersion)) {
      canAutoLaunch = false;
      final AlertDialog.Builder builder = new AlertDialog.Builder(this);
      builder.setCancelable(false);
      final WebView webView = new WebView(getApplicationContext());
      webView.loadData(StringUtil.readToString(getResources().openRawResource(R.raw.whats_new)), "text/html", "utf-8");
      builder.setView(webView);
      builder.setNegativeButton(android.R.string.ok, new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int id) {
               dialog.cancel();
          }
      });
      final AlertDialog alert = builder.create();
      WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
      layoutParams.copyFrom(alert.getWindow().getAttributes());
      layoutParams.width = WindowManager.LayoutParams.FILL_PARENT;
      layoutParams.height = WindowManager.LayoutParams.FILL_PARENT;
      alert.show();
      alert.getWindow().setAttributes(layoutParams);
      prefs.edit().putString(C.THANKS_FOR_UPDATING_VERSION, thanksForUpdatingLatestVersion).commit();
    }
    
    if (!getIntent().getBooleanExtra(C.CAN_AUTO_LAUNCH_DICT, true)) {
      canAutoLaunch = false;
    }
  }
  
  private void onClick(int index) {
    final DictionaryInfo dictionaryInfo = adapter.getItem(index);
    final DictionaryInfo downloadable = application.getDownloadable(dictionaryInfo.uncompressedFilename);
    if (!application.isDictionaryOnDevice(dictionaryInfo.uncompressedFilename) && downloadable != null) {
      final Intent intent = DownloadActivity
          .getLaunchIntent(downloadable.downloadUrl,
              application.getPath(dictionaryInfo.uncompressedFilename).getPath() + ".zip",
              dictionaryInfo.dictInfo);
      startActivity(intent);
    } else {
      final Intent intent = DictionaryActivity.getLaunchIntent(application.getPath(dictionaryInfo.uncompressedFilename), 0, "");
      startActivity(intent);
    }
  }
  
  @Override
  protected void onResume() {
    super.onResume();
    
    if (PreferenceActivity.prefsMightHaveChanged) {
      PreferenceActivity.prefsMightHaveChanged = false;
      finish();
      startActivity(getIntent());
    }
    
    final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
    if (canAutoLaunch && prefs.contains(C.DICT_FILE) && prefs.contains(C.INDEX_INDEX)) {
      canAutoLaunch = false;  // Only autolaunch once per-process, on startup.
      Log.d(LOG, "Skipping Dictionary List, going straight to dictionary.");
      startActivity(DictionaryActivity.getLaunchIntent(new File(prefs.getString(C.DICT_FILE, "")), prefs.getInt(C.INDEX_INDEX, 0), prefs.getString(C.SEARCH_TOKEN, "")));
      // Don't finish, so that user can hit back and get here.
      //finish();
      return;
    }

    setListAdapter(adapter = new Adapter());
  }

  public boolean onCreateOptionsMenu(final Menu menu) {
    final MenuItem about = menu.add(getString(R.string.about));
    about.setOnMenuItemClickListener(new OnMenuItemClickListener() {
      public boolean onMenuItemClick(final MenuItem menuItem) {
        final Intent intent = new Intent().setClassName(AboutActivity.class
            .getPackage().getName(), AboutActivity.class.getCanonicalName());
        startActivity(intent);
        return false;
      }
    });
    
    final MenuItem preferences = menu.add(getString(R.string.preferences));
    preferences.setOnMenuItemClickListener(new OnMenuItemClickListener() {
      public boolean onMenuItemClick(final MenuItem menuItem) {
        PreferenceActivity.prefsMightHaveChanged = true;
        startActivity(new Intent(DictionaryManagerActivity.this,
            PreferenceActivity.class));
        return false;
      }
    });
    
    return true;
  }
  

  @Override
  public void onCreateContextMenu(final ContextMenu menu, final View view,
      final ContextMenuInfo menuInfo) {
    super.onCreateContextMenu(menu, view, menuInfo);
    
    final AdapterContextMenuInfo adapterContextMenuInfo = (AdapterContextMenuInfo) menuInfo;
    final int position = adapterContextMenuInfo.position;
    final DictionaryInfo dictionaryInfo = adapter.getItem(position);
    
    if (position > 0) {
      final MenuItem moveToTopMenuItem = menu.add(R.string.moveToTop);
      moveToTopMenuItem.setOnMenuItemClickListener(new OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem item) {
          application.moveDictionaryToTop(dictionaryInfo);
          setListAdapter(adapter = new Adapter());
          return true;
        }
      });
    }

    final MenuItem deleteMenuItem = menu.add(R.string.deleteDictionary);
    deleteMenuItem.setOnMenuItemClickListener(new OnMenuItemClickListener() {
      @Override
      public boolean onMenuItemClick(MenuItem item) {
        application.deleteDictionary(dictionaryInfo);
        setListAdapter(adapter = new Adapter());
        return true;
      }
    });

  }

  class Adapter extends BaseAdapter {
    
    final List<DictionaryInfo> dictionaryInfos = application.getAllDictionaries();

    @Override
    public int getCount() {
      return dictionaryInfos.size();
    }

    @Override
    public DictionaryInfo getItem(int position) {
      return dictionaryInfos.get(position);
    }

    @Override
    public long getItemId(int position) {
      return position;
    }
    
    @Override
    public View getView(final int position, final View convertView, final ViewGroup parent) {
      final DictionaryInfo dictionaryInfo = getItem(position);
      final LinearLayout result = new LinearLayout(parent.getContext());
      
      final boolean updateAvailable = application.updateAvailable(dictionaryInfo);
      final DictionaryInfo downloadable = application.getDownloadable(dictionaryInfo.uncompressedFilename); 
      if ((!application.isDictionaryOnDevice(dictionaryInfo.uncompressedFilename) || updateAvailable) && downloadable != null) {
        final Button downloadButton = new Button(parent.getContext());
        downloadButton.setText(getString(updateAvailable ? R.string.updateButton : R.string.downloadButton));
        downloadButton.setOnClickListener(new IntentLauncher(parent.getContext(), DownloadActivity
            .getLaunchIntent(downloadable.downloadUrl,
                application.getPath(dictionaryInfo.uncompressedFilename).getPath() + ".zip",
                dictionaryInfo.dictInfo)) {
          @Override
          protected void onGo() {
            application.invalidateDictionaryInfo(dictionaryInfo.uncompressedFilename);
          }
        });
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        downloadButton.setLayoutParams(layoutParams);
        result.addView(downloadButton);
      }

      final TextView textView = new TextView(parent.getContext());
      final String name = application.getDictionaryName(dictionaryInfo.uncompressedFilename);
      textView.setText(name);
      textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22);
      result.addView(textView);

      // Because we have a Button inside a ListView row:
      // http://groups.google.com/group/android-developers/browse_thread/thread/3d96af1530a7d62a?pli=1
      result.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
      result.setClickable(true);
      result.setFocusable(true);
      result.setLongClickable(true);
      result.setBackgroundResource(android.R.drawable.menuitem_background);
      result.setOnClickListener(new TextView.OnClickListener() {
        @Override
        public void onClick(View v) {
          DictionaryManagerActivity.this.onClick(position);
        }
      });

      return result;
    }
  }

  public static Intent getLaunchIntent() {
    final Intent intent = new Intent();
    intent.setClassName(DictionaryManagerActivity.class.getPackage().getName(),
        DictionaryManagerActivity.class.getName());
    intent.putExtra(C.CAN_AUTO_LAUNCH_DICT, false);
    return intent;
  }

}
