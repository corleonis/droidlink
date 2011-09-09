/*
 * Copyright (C) 2011 Pixmob (http://github.com/pixmob)
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
package com.pixmob.droidlink.ui;

import android.graphics.PixelFormat;
import android.os.Bundle;
import android.support.v4.app.ActionBar;
import android.support.v4.app.FragmentActivity;

import com.pixmob.droidlink.R;

/**
 * Display accounts thanks to {@link AccountsFragment}.
 * @author Pixmob
 */
public class AccountsActivity extends FragmentActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.accounts);
        
        // Customize action bar.
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setCustomView(R.layout.nav);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setBackgroundDrawable(getResources().getDrawable(R.drawable.nav_background));
    }
    
    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        // Enable dithering, ie better gradients.
        getWindow().setFormat(PixelFormat.RGBA_8888);
    }
}