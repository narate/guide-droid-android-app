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

import com.narate.ar.fork_framework.ShowSelectedPlace;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

public class ImageAdapter extends BaseAdapter {
	
    public static String[] URLS = {
        "https://fbcdn-sphotos-a.akamaihd.net/hphotos-ak-ash3/576307_3100006706581_1456569621_32050039_258197904_n.jpg",
        "https://fbcdn-sphotos-a.akamaihd.net/hphotos-ak-ash3/554756_3114178860876_1456569621_32056122_1681829772_n.jpg",
        "https://fbcdn-sphotos-a.akamaihd.net/hphotos-ak-prn1/548549_3090485388554_1456569621_32046989_484201460_n.jpg",
        "https://fbcdn-sphotos-a.akamaihd.net/hphotos-ak-ash3/574852_3089123594510_1456569621_32046168_658339936_n.jpg",
        "https://fbcdn-sphotos-a.akamaihd.net/hphotos-ak-snc7/582212_3088923309503_1456569621_32045963_1477198246_n.jpg",
        "https://fbcdn-sphotos-a.akamaihd.net/hphotos-ak-ash4/380770_3035383611044_1456569621_32028690_572581720_n.jpg",
        "https://fbcdn-sphotos-a.akamaihd.net/hphotos-ak-ash3/543685_3029974435818_1456569621_32026386_1208530191_n.jpg",
        "https://fbcdn-sphotos-a.akamaihd.net/hphotos-ak-ash3/535569_2989973995832_1456569621_32011324_940029178_n.jpg",
        "https://fbcdn-sphotos-a.akamaihd.net/hphotos-ak-ash3/528787_2988779605973_1456569621_32010745_730584749_n.jpg",
        "https://fbcdn-sphotos-a.akamaihd.net/hphotos-ak-ash3/526156_2988680323491_1456569621_32010704_307957124_n.jpg",
        "https://fbcdn-sphotos-a.akamaihd.net/hphotos-ak-snc7/149337_2984886548649_1456569621_32007948_501983298_n.jpg",
        "https://fbcdn-sphotos-a.akamaihd.net/hphotos-ak-ash3/522354_2940511679305_1456569621_31992814_996601400_n.jpg",
        "https://fbcdn-sphotos-a.akamaihd.net/hphotos-ak-ash3/543303_2875567055730_1456569621_31978238_763962281_n.jpg",
        "https://fbcdn-sphotos-a.akamaihd.net/hphotos-ak-ash3/524309_2871768520769_1456569621_31976308_1253948484_n.jpg",
        "https://fbcdn-sphotos-a.akamaihd.net/hphotos-ak-ash3/564177_2815677238522_1456569621_31950948_1227212437_n.jpg"
    };
	
	//private static final String[] URLS = ShowSelectedPlace.photo_name;
    
    private final ImageDownloader imageDownloader = new ImageDownloader();
    
    public int getCount() {
        return URLS.length;
    }

    public String getItem(int position) {
        return URLS[position];
    }

    public long getItemId(int position) {
        return URLS[position].hashCode();
    }

    public View getView(int position, View view, ViewGroup parent) {
        if (view == null) {
            view = new ImageView(parent.getContext());
            view.setPadding(0, 5, 0, 5);            
        }

        imageDownloader.download(URLS[position], (ImageView) view);
        
        return view;
    }

    public ImageDownloader getImageDownloader() {
        return imageDownloader;
    }
}
