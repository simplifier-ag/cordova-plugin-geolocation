/*
       Licensed to the Apache Software Foundation (ASF) under one
       or more contributor license agreements.  See the NOTICE file
       distributed with this work for additional information
       regarding copyright ownership.  The ASF licenses this file
       to you under the Apache License, Version 2.0 (the
       "License"); you may not use this file except in compliance
       with the License.  You may obtain a copy of the License at
         http://www.apache.org/licenses/LICENSE-2.0
       Unless required by applicable law or agreed to in writing,
       software distributed under the License is distributed on an
       "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
       KIND, either express or implied.  See the License for the
       specific language governing permissions and limitations
       under the License.
 */


package org.apache.cordova.geolocation;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.view.Window;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.LOG;
import org.apache.cordova.PermissionHelper;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;

public class Geolocation extends CordovaPlugin {

    String TAG = "GeolocationPlugin";
    CallbackContext context;

    AlertDialog featureDescriptionDialog = null;

    String [] permissions = { Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION };

    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) {
        LOG.d(TAG, "We are entering execute");
        context = callbackContext;
        if(action.equals("getPermission"))
        {
            if(hasPermisssion())
            {
                PluginResult r = new PluginResult(PluginResult.Status.OK);
                context.sendPluginResult(r);
                return true;
            }
            else {
                showUsageDescription(localize(
                        cordova.getContext(),
                        "gps_usage_description"
                ));
            }
            return true;
        }
        return false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void showUsageDescription(String message) {
        featureDescriptionDialog = new AlertDialog.Builder(cordova.getContext())
                .setMessage(message)
                .setPositiveButton(cordova.getContext().getResources().getText(android.R.string.ok),
                        (dialog, which) -> {
                    featureDescriptionDialog.setOnDismissListener(null);
                    dialog.dismiss();
                    PermissionHelper.requestPermissions(this, 0, permissions);
                })
                .setOnDismissListener(dialog -> {
                    //dialog got dismissed/back pressed
                    PluginResult result;
                    if(context != null) {
                        LOG.d(TAG, "Permission Denied!");
                        result = new PluginResult(PluginResult.Status.ILLEGAL_ACCESS_EXCEPTION);
                        context.sendPluginResult(result);
                    }
                })
                .create();
        featureDescriptionDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        featureDescriptionDialog.show();
    }

    public void onRequestPermissionResult(int requestCode, String[] permissions, int[] grantResults) {
        PluginResult result;
        //This is important if we're using Cordova without using Cordova, but we have the geolocation plugin installed
        if(context != null) {
            for (int r : grantResults) {
                if (r == PackageManager.PERMISSION_DENIED) {
                    LOG.d(TAG, "Permission Denied!");
                    result = new PluginResult(PluginResult.Status.ILLEGAL_ACCESS_EXCEPTION);
                    context.sendPluginResult(result);
                    return;
                }

            }
            result = new PluginResult(PluginResult.Status.OK);
            context.sendPluginResult(result);
        }
    }

    public boolean hasPermisssion() {
        for(String p : permissions)
        {
            if(!PermissionHelper.hasPermission(this, p))
            {
                return false;
            }
        }
        return true;
    }

    /*
     * We override this so that we can access the permissions variable, which no longer exists in
     * the parent class, since we can't initialize it reliably in the constructor!
     */

    public void requestPermissions(int requestCode)
    {
        PermissionHelper.requestPermissions(this, requestCode, permissions);
    }

    /**
     * @param filename     Name of the file
     * @param resourceType Type of resource (ID, STRING, LAYOUT, DRAWABLE)
     * @return The associated resource identifier. Returns 0 if no such resource was found. (0 is not a valid resource ID.)
     */
    private int getResourceId(Context context, String filename, String resourceType) {
        String package_name = context.getPackageName();
        Resources resources = context.getResources();

        return resources.getIdentifier(filename, resourceType, package_name);
    }

    /**
     * @param identifier string identifier of the resource
     * @return The localized string. Returns identifier if no such resource was found.
     */
    public String localize(Context context, String identifier, Object... args) {
        int id = getResourceId(context, identifier, "string");

        if (id == 0) {
            return identifier;
        } else {
            return args.length > 0
                    ? String.format(context.getResources().getString(id), args)
                    : context.getResources().getString(id);
        }
    }
}
