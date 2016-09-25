/**
 * Copyright (C) 2013-2014 EaseMob Technologies. All rights reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.runzii.cyou.common.utils;

import android.content.res.Configuration;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PreviewCallback;
import android.os.Build;
import android.util.Log;
import android.view.SurfaceHolder;

import com.easemob.chat.EMVideoCallHelper;
import com.runzii.cyou.CYouApplication;

public class CameraUtil  {
    private static final String TAG = "CameraHelper";

    private static int mwidth = 240;
    private static int mheight = 320;

    private Camera mCamera;
    private int camera_count;

    private Parameters mParameters;


    // private byte[] yuv_Rotate90lr;

    private SurfaceHolder localSurfaceHolder;


    private CameraInfo cameraInfo;

    public CameraUtil(SurfaceHolder localSurfaceHolder) {
        this.localSurfaceHolder = localSurfaceHolder;
    }

    /**
     * 开启相机拍摄
     */
    public void startCapture(int mwidth, int mheight) {
        this.mwidth = mwidth;
        this.mheight = mheight;
        try {
            cameraInfo = new CameraInfo();
            if (mCamera == null) {
                // mCamera = Camera.open();
                camera_count = Camera.getNumberOfCameras();
                Log.e(TAG, "camera count:" + camera_count);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
                    for (int i = 0; i < camera_count; i++) {
                        CameraInfo info = new CameraInfo();
                        Camera.getCameraInfo(i, info);
                        // find front camera
                        if (info.facing == CameraInfo.CAMERA_FACING_FRONT) {
                            Log.e(TAG, "to open front camera");
                            mCamera = Camera.open(i);
                            Camera.getCameraInfo(i, cameraInfo);
                        }
                    }
                }
                if (mCamera == null) {
                    Log.e(TAG, "AAAAA OPEN camera");
                    mCamera = Camera.open();
                    Camera.getCameraInfo(0, cameraInfo);
                }

            }

            mCamera.stopPreview();
            mParameters = mCamera.getParameters();

            if (isScreenOriatationPortrait()) {
                if (cameraInfo.orientation == 270 || cameraInfo.orientation == 0) {
                    mCamera.setDisplayOrientation(90);
                } else if (cameraInfo.orientation == 90 || cameraInfo.orientation == 180) {
                    mCamera.setDisplayOrientation(270);
                }

            } else {
                if (cameraInfo.orientation == 90 || cameraInfo.orientation == 0 || cameraInfo.orientation == 180) {
                    mCamera.setDisplayOrientation(180);
                }
            }

            mParameters.setPreviewSize(mheight, mwidth);
            mParameters.setPreviewFpsRange(22,26);
//            mCamera.setParameters(mParameters);
            int mformat = mParameters.getPreviewFormat();
            int bitsperpixel = ImageFormat.getBitsPerPixel(mformat);
            Log.e(TAG, "pzy bitsperpixel: " + bitsperpixel);
            // mCamera.setPreviewDisplay(holder);
            mCamera.setPreviewDisplay(localSurfaceHolder);

            mCamera.startPreview();
            Log.d(TAG, "camera start preview");
        } catch (Exception e) {
            e.printStackTrace();
            if (mCamera != null)
                mCamera.release();
        }
    }

    /**
     * 开启相机拍摄
     */
    public void startCapture() {
        try {
            cameraInfo = new CameraInfo();
            if (mCamera == null) {
                // mCamera = Camera.open();
                camera_count = Camera.getNumberOfCameras();
                Log.e(TAG, "camera count:" + camera_count);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
                    for (int i = 0; i < camera_count; i++) {
                        CameraInfo info = new CameraInfo();
                        Camera.getCameraInfo(i, info);
                        // find front camera
                        if (info.facing == CameraInfo.CAMERA_FACING_FRONT) {
                            Log.e(TAG, "to open front camera");
                            mCamera = Camera.open(i);
                            Camera.getCameraInfo(i, cameraInfo);
                        }
                    }
                }
                if (mCamera == null) {
                    Log.e(TAG, "AAAAA OPEN camera");
                    mCamera = Camera.open();
                    Camera.getCameraInfo(0, cameraInfo);
                }

            }

            mCamera.stopPreview();
            mParameters = mCamera.getParameters();

            if (isScreenOriatationPortrait()) {
                if (cameraInfo.orientation == 270 || cameraInfo.orientation == 0) {
                    mCamera.setDisplayOrientation(90);
                } else if (cameraInfo.orientation == 90 || cameraInfo.orientation == 180) {
                    mCamera.setDisplayOrientation(270);
                }

            } else {
                if (cameraInfo.orientation == 90 || cameraInfo.orientation == 0 || cameraInfo.orientation == 180) {
                    mCamera.setDisplayOrientation(180);
                }
            }

            mParameters.setPreviewSize(mheight, mwidth);
            mParameters.setPreviewFpsRange(22,26);
            mCamera.setParameters(mParameters);
            int mformat = mParameters.getPreviewFormat();
            int bitsperpixel = ImageFormat.getBitsPerPixel(mformat);
            Log.e(TAG, "pzy bitsperpixel: " + bitsperpixel);
            // mCamera.setPreviewDisplay(holder);
            mCamera.setPreviewDisplay(localSurfaceHolder);

            mCamera.startPreview();
            Log.d(TAG, "camera start preview");
        } catch (Exception e) {
            e.printStackTrace();
            if (mCamera != null)
                mCamera.release();
        }
    }


    /**
     * 停止拍摄
     */
    public void stopCapture() {
        if (mCamera != null) {
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }


    boolean isScreenOriatationPortrait() {
        return CYouApplication.getCYouApplicationContext().getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
    }
}
