package com.example.libsysmobile;

import java.io.IOException;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.Size;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.FormatException;
import com.google.zxing.LuminanceSource;
import com.google.zxing.NotFoundException;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Reader;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;

public class CameraService extends SurfaceView implements
		SurfaceHolder.Callback {

	SurfaceHolder mHolder;
	public Camera mCamera;
	MyPictureCallback p = new MyPictureCallback();;

	MainActivity a;

	boolean hasCode;
	String lastCode;

	public CameraService(Context context, MainActivity a) {
		super(context);
		this.a = a;

		mHolder = getHolder();
		mHolder.addCallback(this);
		mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		mCamera.startPreview();
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		if (mCamera == null) {
			mCamera = Camera.open();
		}
		mCamera.setDisplayOrientation(90);
		try {

			Parameters mParameters = mCamera.getParameters();
			mParameters.set("orientation", "portrait");
			mParameters.set("rotation", 90);

			// AUTOFOCUS
			mParameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);

			// GET OPTIMAL
			List<Size> prevSizes = mParameters.getSupportedPreviewSizes();
			int screenWidth = getResources().getDisplayMetrics().widthPixels;

			int minSpace = screenWidth;
			for (Size s : prevSizes) {

				if (minSpace < 0) {
					minSpace = -1 * minSpace;
				}

				if (screenWidth - s.width < minSpace && s.width > 400) {
					mParameters.setPreviewSize(s.width, s.height);
					mParameters.setPictureSize(s.width, s.height);
					minSpace = screenWidth - s.width;
				}
			}

			// APPLY CHANGES
			mCamera.setParameters(mParameters);
			mCamera.setPreviewDisplay(holder);
			holder.setFixedSize(320, 480);

			// START PREVIEW
			mCamera.startPreview();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		if (mCamera != null) {
			mCamera.stopPreview();
			mCamera.release();
			mCamera = null;
		}
		Log.d("LibSys", "end");

	}

	public void readCode() {
		mCamera.takePicture(null, null, p);
		try {
			Thread.currentThread().sleep(500);
		} catch (InterruptedException e) {

		}
	}

	private class MyPictureCallback implements PictureCallback {

		@Override
		public void onPictureTaken(byte[] data, Camera camera) {
			Bitmap capturedBitmap = BitmapFactory.decodeByteArray(data, 0,
					data.length);

			int[] intArray = new int[capturedBitmap.getWidth()
					* capturedBitmap.getHeight()];

			capturedBitmap.getPixels(intArray, 0, capturedBitmap.getWidth(), 0,
					0, capturedBitmap.getWidth(), capturedBitmap.getHeight());

			LuminanceSource source = new RGBLuminanceSource(
					capturedBitmap.getWidth(), capturedBitmap.getHeight(),
					intArray);

			BinaryBitmap bb = new BinaryBitmap(new HybridBinarizer(source));

			Reader reader = new QRCodeReader();
			try {
				String readedCode = reader.decode(bb).getText();
				final String temp = readedCode;
				Log.d("LibSys", "Kód: " + readedCode);
				if (readedCode.length() == 9 && readedCode.startsWith("5")) {
					a.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							a.setInfoString("Kód: " + temp);
							a.setMainButtonLabel("Odeslat do knihovny");
						}
					});

					lastCode = readedCode;
					hasCode = true;
				} else {
					a.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							a.setInfoString("Není QR kód systému LibSys");
						}
					});
					hasCode = false;
				}

			} catch (NotFoundException e) {
				Log.d("LibSys", "NO CODE");
				a.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						a.setInfoString("Kód nenalezen");
					}
				});

				hasCode = false;
			} catch (ChecksumException e) {
				Log.d("LibSys", "BAD CHECKSUM");
				hasCode = false;
			} catch (FormatException e) {
				Log.d("LibSys", "INVALID FORMAT");
				hasCode = false;
			} finally {
				mCamera.startPreview();
			}

		}
	}

	public boolean hasCode() {
		return hasCode;
	}

	public void setHasCode(boolean b) {
		hasCode = b;
	}

	public String getlastCode() {
		return lastCode;
	}
}
