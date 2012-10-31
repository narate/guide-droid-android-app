/*
 * Copyright (C) 2010 The Android Open Source Project
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

package com.narate.ar.imagedownloader;

import com.narate.ar.fork_framework.R;

import android.app.ListActivity;
import android.os.Bundle;
import android.widget.RadioGroup;

public class ImageListActivity extends ListActivity  implements RadioGroup.OnCheckedChangeListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.imagelist);

        RadioGroup radioGroup = (RadioGroup) findViewById(R.id.radioGroup);
        radioGroup.setOnCheckedChangeListener(this);
        
        setListAdapter(new ImageAdapter());
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        ImageDownloader.Mode mode = ImageDownloader.Mode.CORRECT;
        
        if (checkedId == R.id.correctButton) {
            mode = ImageDownloader.Mode.CORRECT;
        }else if (checkedId == R.id.randomButton) {
                mode = ImageDownloader.Mode.NO_DOWNLOADED_DRAWABLE;
        }
        
        ((ImageAdapter) getListAdapter()).getImageDownloader().setMode(mode);
    }
}
