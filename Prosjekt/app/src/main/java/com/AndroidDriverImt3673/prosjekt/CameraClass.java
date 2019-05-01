package com.AndroidDriverImt3673.prosjekt;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.TextureView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;


/**
 * source: https://inducesmile.com/android/android-camera2-api-example-tutorial/
 */
public class CameraClass {
    private static final String TAG = "camera";

    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }
    private String cameraId;
    protected CameraDevice cameraDevice;
    protected CameraCaptureSession cameraCaptureSessions;
    protected CaptureRequest captureRequest;
    protected CaptureRequest.Builder captureRequestBuilder;
    private Size imageDimension;
    private ImageReader imageReader;
    private File file;
    private static final int REQUEST_CAMERA_PERMISSION = 200;
    private boolean mFlashSupported;
    private Handler mBackgroundHandler;
    private HandlerThread mBackgroundThread;


    private Context context;
    private TextureView textureView;

    /**
     * Constructor
     * @param c context of calling activity
     * @param t view used for displaying camera preview
     */
    public  CameraClass(Context c, TextureView t){
        context = c;
        textureView = t;
    }


    //--
    TextureView.SurfaceTextureListener textureListener = new TextureView.SurfaceTextureListener() {
        /**
         * opens camera
         * @param surface not used
         * @param width not used
         * @param height not used
         */
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            openCamera();
        }
        // required function, but not used
        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
            // Transform you image captured size according to the surface width and height
        }
        /**
         * closes the camera connection correctly and sets it to null
         * for future garbage collection
         */
        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {

            if (cameraDevice != null) {
                cameraDevice.close();
                cameraDevice = null;

            }

            return false;
        }
        // required function, but not used
        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        }
    };


    private final CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        /**
         * initialises the cameraDevice and start the camera preview
         * @param camera
         */
        @Override
        public void onOpened(CameraDevice camera) {
            Log.e(TAG, "onOpened");
            cameraDevice = camera;
            createCameraPreview();
        }

        /**
         * closes the camera connection correctly
         * @param camera
         */
        @Override
        public void onDisconnected(CameraDevice camera) {
            cameraDevice.close();
        }


        /**
         * closes the camera connection correctly and sets it to null
         * for future garbage collection
         * @param camera
         * @param error
         */
        @Override
        public void onError(CameraDevice camera, int error) {
            if (cameraDevice != null) {
                cameraDevice.close();
                cameraDevice = null;
            }

        }
    };

    // informs the user that a picture was saved to the file system
    final CameraCaptureSession.CaptureCallback captureCallbackListener = new CameraCaptureSession.CaptureCallback() {
        /**
         * informs the user where the image file was stored and creates a camera preview
         * @param session
         * @param request
         * @param result
         */
        @Override
        public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
            super.onCaptureCompleted(session, request, result);
            Toast.makeText(context, "Saved:" + file, Toast.LENGTH_SHORT).show();
            createCameraPreview();
        }
    };

    /**
     * creates and start a thread and thread handler
     * used for doing camera actions
     */
    protected void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("Camera Background");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }

    /**
     * stops and destroys a thread and thread handler
     * this ensures things are closed correctly and
     * resources made available for garbage collection.
     */
    protected void stopBackgroundThread() {
        mBackgroundThread.quitSafely();
        try {
            mBackgroundThread.join();
            mBackgroundThread = null;
            mBackgroundHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * displays a preview of what the camera is currently seeing.
     */
    protected void createCameraPreview() {
        try {
            // find the view used to display preview
            SurfaceTexture texture = textureView.getSurfaceTexture();
            assert texture != null;
            // configure width and height of camera preview
            texture.setDefaultBufferSize(imageDimension.getWidth(), imageDimension.getHeight());
            Surface surface = new Surface(texture);
            // configure camera preview
            captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            captureRequestBuilder.addTarget(surface);
            cameraDevice.createCaptureSession(Arrays.asList(surface), new CameraCaptureSession.StateCallback(){
                @Override
                public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                    //if camera is already closed
                    if (null == cameraDevice) {
                        return;
                    }
                    // When the session is ready, we start displaying the preview.
                    cameraCaptureSessions = cameraCaptureSession;
                    updatePreview();
                }
                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
                    Toast.makeText(context, "Configuration change", Toast.LENGTH_SHORT).show();
                }
            }, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * open a connection to rear camera
     */
    public void openCamera() {

        CameraManager manager = (CameraManager)  context.getSystemService(Context.CAMERA_SERVICE);
        Log.e(TAG, "is camera open");
        try {

            Activity activity = (Activity) context;

            // get rear camera ID
            cameraId = manager.getCameraIdList()[0];
            // get metadata about rear camera
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            assert map != null;
            // get image dimensions
            imageDimension = map.getOutputSizes(SurfaceTexture.class)[0];
            // checks and adds permission for camera and let user grant the permission
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(
                            context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                // make a permissions request to the user
                ActivityCompat.requestPermissions(
                        activity,
                        new String[]{Manifest.permission.CAMERA,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        REQUEST_CAMERA_PERMISSION);
                return;
            }
            // open a connection to rear camera
            manager.openCamera(cameraId, stateCallback, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        Log.e(TAG, "openCamera X");
    }

    /**
     *  helper function for createCameraPreview()
     *  updates preview
     */
    protected void updatePreview() {
        // if not initialized
        if(cameraDevice == null) {
            Log.e(TAG, "updatePreview error, return");
        }
        captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
        try {
            cameraCaptureSessions.setRepeatingRequest(captureRequestBuilder.build(), null, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * closes camera connection
     * and releases resources for garbage collection
     */
    public void closeCamera() {
        if (cameraDevice != null ) {
            cameraDevice.close();
            cameraDevice = null;
        }
        if (imageReader != null) {
            imageReader.close();
            imageReader = null;
        }
    }


    /**
     * helper-function that configures camera settings
     */
    public CaptureRequest.Builder captureRequestBuilder(ImageReader reader) throws CameraAccessException {
        // get the android devices orientation
        int rotation =((Activity)context).getWindowManager().getDefaultDisplay().getRotation();
        CaptureRequest.Builder captureBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
        captureBuilder.addTarget(reader.getSurface());
        captureBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
        captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, ORIENTATIONS.get(rotation));

        return captureBuilder;
    }


    /**
     * creates and manages all the objects and small jobs needed to take a picture
     */
    protected void takePicture() {
        // if camera device is not initialized
        if(cameraDevice == null ) {
            Log.e(TAG, "cameraDevice is null");
            return;
        }

        // gain access to the camera(hardware)
        CameraManager manager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        try {
            // get available metadata about the camera
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraDevice.getId());
            Size[] jpegSizes = null;
            if (characteristics != null) {
                jpegSizes = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP).getOutputSizes(ImageFormat.JPEG);
            }

            // default values for image dimensions
            int width = 640;
            int height = 480;
            // dynamic values for image dimensions
            if (jpegSizes != null && 0 < jpegSizes.length) {
                width = jpegSizes[0].getWidth();
                height = jpegSizes[0].getHeight();
            }
            // render image based on dimensions and format
            ImageReader reader = ImageReader.newInstance(width, height, ImageFormat.JPEG, 1);

            // collection used for linking camera surface and view used for displaying image/image preview
            List<Surface> outputSurfaces = new ArrayList<Surface>(2);
            outputSurfaces.add(reader.getSurface());
            outputSurfaces.add(new Surface(textureView.getSurfaceTexture()));

            // get the android devices orientation
            int rotation =((Activity)context).getWindowManager().getDefaultDisplay().getRotation();
            // used for configuring camera before picture is taken
            final CaptureRequest.Builder captureBuilder = captureRequestBuilder(reader);
            // make a new unique file
            final File file = makeFile();

            ImageReader.OnImageAvailableListener readerListener = new ImageReader.OnImageAvailableListener() {
                /**
                 * acquire the latest image, saves it to file and publishes the picture so that
                 * it is available to other apps
                 * @param reader
                 */
                @Override
                public void onImageAvailable(ImageReader reader) {
                    imageAvailableHelper(file, reader);
                }
            };


            reader.setOnImageAvailableListener(readerListener, mBackgroundHandler);
            final CameraCaptureSession.CaptureCallback captureListener = new CameraCaptureSession.CaptureCallback() {
                /**
                 * informs the user where the image was stored and creates a camera preview
                 * @param request
                 * @param result
                 */
                @Override
                public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
                    super.onCaptureCompleted(session, request, result);
                    Toast.makeText(context, "Saved:" + file, Toast.LENGTH_SHORT).show();
                    createCameraPreview();
                }
            };


            cameraDevice.createCaptureSession(outputSurfaces, new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(CameraCaptureSession session) {
                    try {
                        session.capture(captureBuilder.build(), captureListener, mBackgroundHandler);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }

                /**
                 * required function, but not used
                 * @param session
                 */
                @Override
                public void onConfigureFailed(CameraCaptureSession session) {
                }
            }, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * Helper-function
     * acquire the latest image, saves it to file and publishes the picture so that
     * it is available to other apps
     * @param file file to be written to
     * @param reader used for getting latest image
     */
    private void imageAvailableHelper(File file, ImageReader reader) {
        Image image = null;
        try {
            image = reader.acquireLatestImage();
            ByteBuffer buffer = image.getPlanes()[0].getBuffer();
            byte[] bytes = new byte[buffer.capacity()];
            buffer.get(bytes);
            save(bytes, file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (image != null) {
                image.close();
                galleryAddPic(file);
            }
        }
    }

    /**
     * makes a unique file
     * @return a file
     */
    private File makeFile() {
        // make a random alphanumeric string
        String random = UUID.randomUUID().toString().replaceAll("-", "").subSequence(0,5).toString();
        // make a new unique file
        File file = new File(
                Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_PICTURES)+"/"+ random + "_pic.jpg");
        return file;
    }

    /**
     * writes bytes to file
     * @param bytes byte of an image
     * @param file file to store image
     * @throws IOException if something goes wrong
     */
    private void save(byte[] bytes, File file) throws IOException {
        OutputStream output = null;
        try {
            output = new FileOutputStream(file);
            output.write(bytes);
        } finally {
            if (null != output) {
                output.close();
            }
        }
    }

    /**
     * make image available to other apps like the gallery app
     * @param file the file to be published
     */
    private void galleryAddPic(File file) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri contentUri = Uri.fromFile(file);
        mediaScanIntent.setData(contentUri);
        context.sendBroadcast(mediaScanIntent);
    }

}
