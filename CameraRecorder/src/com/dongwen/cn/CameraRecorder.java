
package com.dongwen.cn;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import com.amazonaws.s3uploader.S3Uploader;
// Need the following import to get access to the app resources, since this
// class is in a sub-package.

// ----------------------------------------------------------------------

public class CameraRecorder extends Activity {
    private Preview mPreview;

    Camera mCamera;

    int numberOfCameras;

    int cameraCurrentlyLocked;

    // The first rear facing camera
    int defaultCameraId;

    public MediaRecorder mrec = new MediaRecorder();

    private SensorFusion2 mSensorFusion2;

    private S3Uploader mS3Uploader;

    private File mVideFile;

    private File mSensorFile;

    private Menu mMenu;

    private boolean mStartedRecode = false;

    private boolean mNeedUpload = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Hide the window title.
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // Create a RelativeLayout container that will hold a SurfaceView,
        // and set it as the content of our activity.
        mPreview = new Preview(this);
        setContentView(mPreview);

        // Find the total number of cameras available
        numberOfCameras = Camera.getNumberOfCameras();

        // Find the ID of the default camera
        CameraInfo cameraInfo = new CameraInfo();
        for (int i = 0; i < numberOfCameras; i++) {
            Camera.getCameraInfo(i, cameraInfo);
            if (cameraInfo.facing == CameraInfo.CAMERA_FACING_BACK) {
                defaultCameraId = i;
            }
        }

        mSensorFusion2 = new SensorFusion2(this);
        mS3Uploader = new S3Uploader(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Open the default i.e. the first rear facing camera.
        mCamera = Camera.open();
        cameraCurrentlyLocked = defaultCameraId;
        mPreview.setCamera(mCamera);
        mSensorFusion2.start();
        if (!isWifiOrMobileConnected(this)) {
            mNeedUpload = false;
            showAlertDialog("No wifi connection, cannot upload!", true);
        } else if (!isWifiConnected(this)) {
            showAlertDialog("No wifi connection, Are you sure using Mobile network to upload?", false);
        }
    }

    private void showAlertDialog(String message, boolean onlyOneOption){
        AlertDialog.Builder confirm = new AlertDialog.Builder(CameraRecorder.this);
        confirm.setTitle("Warning");
        confirm.setMessage(message);

        if (onlyOneOption) {
            confirm.setPositiveButton("OK", new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
        } else {
            confirm.setNegativeButton("NO", new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int which) {
                    mNeedUpload = false;
                    dialog.dismiss();
                }
            });
            confirm.setPositiveButton("YES", new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
        }

        confirm.show();
    }

    public static boolean isWifiOrMobileConnected(final Context context) {
        ConnectivityManager manager = (ConnectivityManager)context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        try {
            NetworkInfo info = manager.getActiveNetworkInfo();
            return info.isConnected();
        } catch (NullPointerException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean isWifiConnected(final Context context) {
        ConnectivityManager manager = (ConnectivityManager)context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        try {
            NetworkInfo info = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            return info.isConnected();
        } catch (NullPointerException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mStartedRecode) {
            mStartedRecode = false;
            mrec.stop();
            mrec.release();
            mrec = null;
        }

        // Because the Camera object is a shared resource, it's very
        // important to release it when the activity is paused.
        if (mCamera != null) {
            mPreview.setCamera(null);
            mCamera.release();
            mCamera = null;
        }
        mSensorFusion2.stop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        mMenu = menu;
        menu.add(0, 0, 0, "StartRecording");
        menu.add(0, 1, 0, "StopRecording");
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        updateMenu();
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 0:
                try {
                    mStartedRecode = true;
                    updateMenu();
                    startRecording();
                } catch (Exception e) {
                    String message = e.getMessage();
                    Log.i("CameraRecorder", "Problem Start" + message);
                    if (mrec != null)
                        mrec.release();
                }
                break;

            case 1: // GoToAllNotes
                stopRecording();
                break;

            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateMenu() {
        if (mMenu == null)
            return;
        MenuItem startRecordItem = mMenu.getItem(0);
        startRecordItem.setVisible(!mStartedRecode);
        MenuItem stopRecordItem = mMenu.getItem(1);
        stopRecordItem.setVisible(mStartedRecode);
    }

    private void saveData() {
        String fileNames[] = {
                mVideFile.getAbsolutePath(), mSensorFile.getAbsolutePath()
        };
        mS3Uploader.uploadDataInfo(fileNames);
    }

    protected void startRecording() throws IOException {
        mSensorFusion2.startGetFusedOrientationData();
        mCamera.unlock();
        mrec = new MediaRecorder();
        mrec.setCamera(mCamera);
        mrec.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        mrec.setAudioSource(MediaRecorder.AudioSource.MIC);

        CamcorderProfile camcorderProfile = CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH);
        mrec.setProfile(camcorderProfile);
        mrec.setVideoSize(camcorderProfile.videoFrameWidth, camcorderProfile.videoFrameHeight);

        mrec.setPreviewDisplay(mPreview.mHolder.getSurface());
        File videFileFolder = new File(Environment.getExternalStorageDirectory().getPath()
                + "/s3/video/");
        if (!videFileFolder.exists())
            videFileFolder.mkdirs();
        mVideFile = new File(videFileFolder.getAbsolutePath() + "/" + UUID.randomUUID() + ".3gp");
        mrec.setOutputFile(mVideFile.getAbsolutePath());

        File textFileFolder = new File(Environment.getExternalStorageDirectory().getPath()
                + "/s3/text/");
        if (!textFileFolder.exists())
            textFileFolder.mkdirs();
        mSensorFile = new File(textFileFolder.getAbsolutePath() + "/" + UUID.randomUUID() + ".txt");
        mrec.prepare();
        mrec.start();
    }

    private void stopRecording(){
        mStartedRecode = false;
        updateMenu();
        mrec.stop();
        mrec.release();
        mrec = null;
        mSensorFusion2.stopGetFusedOrientationData();
        StringBuilder builder = new StringBuilder();
        HashMap<Long, ArrayList<Float>> map = new HashMap<Long, ArrayList<Float>>(mSensorFusion2.getOrientationMap());
        for (Long key : map.keySet()) {
            builder.append(key);
            builder.append(",");
            ArrayList<Float> list = map.get(key);
            for (int i = 0; i < list.size(); i++) {
                builder.append(list.get(i));
                if (i != list.size() - 1)
                    builder.append(",");
            }
            builder.append("\n");
        }

        FileOutputStream stream = null;
        try {
            stream = new FileOutputStream(mSensorFile);
            stream.write(builder.toString().getBytes());
        } catch (Exception e) {
        } finally {
            if (stream != null)
                try {
                    stream.close();
                } catch (IOException e) {
                }
        }
        if (mNeedUpload)
            saveData();
    }

    // ----------------------------------------------------------------------

    /**
     * A simple wrapper around a Camera and a SurfaceView that renders a
     * centered preview of the Camera to the surface. We need to center the
     * SurfaceView because not all devices have cameras that support preview
     * sizes at the same aspect ratio as the device's display.
     */
    class Preview extends ViewGroup implements SurfaceHolder.Callback {
        private final String TAG = "Preview";

        SurfaceView mSurfaceView;

        public SurfaceHolder mHolder;

        Size mPreviewSize;

        List<Size> mSupportedPreviewSizes;

        Camera mCamera;

        Preview(Context context) {
            super(context);

            mSurfaceView = new SurfaceView(context);
            addView(mSurfaceView);

            // Install a SurfaceHolder.Callback so we get notified when the
            // underlying surface is created and destroyed.
            mHolder = mSurfaceView.getHolder();
            mHolder.addCallback(this);
            mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        }

        public void setCamera(Camera camera) {
            mCamera = camera;
            if (mCamera != null) {
                mSupportedPreviewSizes = mCamera.getParameters().getSupportedPreviewSizes();
                requestLayout();
            }
        }

        public void switchCamera(Camera camera) {
            setCamera(camera);
            try {
                camera.setPreviewDisplay(mHolder);
            } catch (IOException exception) {
                Log.e(TAG, "IOException caused by setPreviewDisplay()", exception);
            }
            Camera.Parameters parameters = camera.getParameters();
            parameters.setPreviewSize(mPreviewSize.width, mPreviewSize.height);
            requestLayout();

            camera.setParameters(parameters);
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            // We purposely disregard child measurements because act as a
            // wrapper to a SurfaceView that centers the camera preview instead
            // of stretching it.
            final int width = resolveSize(getSuggestedMinimumWidth(), widthMeasureSpec);
            final int height = resolveSize(getSuggestedMinimumHeight(), heightMeasureSpec);
            setMeasuredDimension(width, height);

            if (mSupportedPreviewSizes != null) {
                mPreviewSize = getOptimalPreviewSize(mSupportedPreviewSizes, width, height);
            }
        }

        @Override
        protected void onLayout(boolean changed, int l, int t, int r, int b) {
            if (changed && getChildCount() > 0) {
                final View child = getChildAt(0);

                final int width = r - l;
                final int height = b - t;

                int previewWidth = width;
                int previewHeight = height;
                if (mPreviewSize != null) {
                    previewWidth = mPreviewSize.width;
                    previewHeight = mPreviewSize.height;
                }

                // Center the child SurfaceView within the parent.
                if (width * previewHeight > height * previewWidth) {
                    final int scaledChildWidth = previewWidth * height / previewHeight;
                    child.layout((width - scaledChildWidth) / 2, 0, (width + scaledChildWidth) / 2,
                            height);
                } else {
                    final int scaledChildHeight = previewHeight * width / previewWidth;
                    child.layout(0, (height - scaledChildHeight) / 2, width,
                            (height + scaledChildHeight) / 2);
                }
            }
        }

        public void surfaceCreated(SurfaceHolder holder) {
            // The Surface has been created, acquire the camera and tell it
            // where
            // to draw.
            try {
                if (mCamera != null) {
                    CamcorderProfile camcorderProfile = CamcorderProfile
                            .get(CamcorderProfile.QUALITY_HIGH);
                    Parameters paramters = mCamera.getParameters();
                    paramters.setPreviewSize(camcorderProfile.videoFrameWidth,
                            camcorderProfile.videoFrameHeight);
                    mCamera.setParameters(paramters);
                    mCamera.setPreviewDisplay(holder);
                }
            } catch (IOException exception) {
                Log.e(TAG, "IOException caused by setPreviewDisplay()", exception);
            }
        }

        public void surfaceDestroyed(SurfaceHolder holder) {
            // Surface will be destroyed when we return, so stop the preview.
            if (mCamera != null) {
                mCamera.stopPreview();
            }
        }

        private Size getOptimalPreviewSize(List<Size> sizes, int w, int h) {
            final double ASPECT_TOLERANCE = 0.1;
            double targetRatio = (double)w / h;
            if (sizes == null)
                return null;

            Size optimalSize = null;
            double minDiff = Double.MAX_VALUE;

            int targetHeight = h;

            // Try to find an size match aspect ratio and size
            for (Size size : sizes) {
                double ratio = (double)size.width / size.height;
                if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE)
                    continue;
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }

            // Cannot find the one match the aspect ratio, ignore the
            // requirement
            if (optimalSize == null) {
                minDiff = Double.MAX_VALUE;
                for (Size size : sizes) {
                    if (Math.abs(size.height - targetHeight) < minDiff) {
                        optimalSize = size;
                        minDiff = Math.abs(size.height - targetHeight);
                    }
                }
            }
            return optimalSize;
        }

        public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
            // Now that the size is known, set up the camera parameters and
            // begin
            // the preview.
            if (mCamera == null)
                return;
            if (mCamera != null) {
                mCamera.stopPreview();
            }
            Camera.Parameters parameters = mCamera.getParameters();
            CamcorderProfile camcorderProfile = CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH);
            parameters.setPreviewSize(camcorderProfile.videoFrameWidth,
                    camcorderProfile.videoFrameHeight);
            requestLayout();

            try {
                mCamera.setPreviewDisplay(holder);
            } catch (IOException e) {
            }
            mCamera.setParameters(parameters);
            mCamera.startPreview();
        }
    }
}
