package com.ylwang.ifacerecognition;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Vector;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.utils.Converters;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.Config;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.Toast;

@SuppressWarnings("deprecation")
public class FaceRecTestActivity extends Activity {
	private static final String TAG = "FaceRecTest";
	private CascadeClassifier mJavaFaceDetector;
	private MyFaceDetector mNativeFaceDetector;
	private MyFaceRecognizer mFaceRec;
	private String curFaceRecognizer;
	private File mCascadeFile;
	Mat mRgba;
	Thread mChangeViewTh;
	Mat mGray;
	Mat pre_view_gray;
	Mat pre_view_color;
	Mat pre_view__resize;
	Mat pre_view_gray_preproceseed;
	// Mat pre_view_temp;
	Mat label;
	Bitmap dstBmp;
	Vector<Mat> faceVector = null;
	Vector<Integer> labelVector = null;
	private ImageView mTestImg;
	private Handler mTestImgUpdater;
	private static final int isNeedUpdateImg = 1;
	private Integer[] mImageResouceId = { R.drawable.lee01, R.drawable.lee02,
			R.drawable.jenny11, R.drawable.mike21, R.drawable.raj31,
			R.drawable.mark41, R.drawable.mark42, R.drawable.xu51,
			R.drawable.xu52, R.drawable.xu53, R.drawable.core61,
			R.drawable.core62, R.drawable.core63, R.drawable.zarker71,
			R.drawable.jeniffer81

	};
	private int[] mImageResouceLabel = { 0, 0, 1, 2, 3, 4, 4, 5, 5, 5, 6, 6, 6,
			7, 8 };
	private String[] mImageResouceName = { "Lee", "Jenny", "Mike", "Raj",
			"Mark", "Xuqing", "XiCore", "Zarker", "Jennifer" };
	private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {

		@Override
		public void onManagerConnected(int status) {
			switch (status) {
			case LoaderCallbackInterface.SUCCESS: {
				Log.i(TAG, "OpenCV loaded successfully");

				// Load native library after(!) OpenCV initialization
				System.loadLibrary("iFaceRecognition");

				try {
					// load cascade file from application resources
					InputStream is = getResources().openRawResource(
							R.raw.lbpcascade_frontalface);
					File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
					mCascadeFile = new File(cascadeDir,
							"lbpcascade_frontalface.xml");
					FileOutputStream os = new FileOutputStream(mCascadeFile);

					byte[] buffer = new byte[4096];
					int bytesRead;
					while ((bytesRead = is.read(buffer)) != -1) {
						os.write(buffer, 0, bytesRead);
					}
					is.close();
					os.close();

					mJavaFaceDetector = new CascadeClassifier(
							mCascadeFile.getAbsolutePath());
					if (mJavaFaceDetector.empty()) {
						Log.e(TAG, "Failed to load cascade classifier");
						mJavaFaceDetector = null;
					} else
						Log.i(TAG, "Loaded cascade classifier from "
								+ mCascadeFile.getAbsolutePath());

					mNativeFaceDetector = new MyFaceDetector(
							mCascadeFile.getAbsolutePath(), 0,
							MyFaceDetector.TYPE_CascadeClassfier);

					mFaceRec = new MyFaceRecognizer(
							"FaceRecognizer::Eigenfaces");
					init();
					trainDataSet();
					cascadeDir.delete();

				} catch (IOException e) {
					e.printStackTrace();
					Log.e(TAG, "Failed to load cascade. Exception thrown: " + e);
				}

				/* mOpenCvCameraView.enableView(); */

			}
				break;
			default: {
				super.onManagerConnected(status);
			}
				break;
			}
		}
	};
	private Mat pre_view_res;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		Log.i(TAG, "onCreate");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.face_detect_test_surface_view);
		curFaceRecognizer = AddPersonActivity.EigenFace;
		mTestImg = (ImageView) findViewById(R.id.m_test_img_view);
		faceVector = new Vector<Mat>();
		labelVector = new Vector<Integer>();
		Gallery mGallery = (Gallery) findViewById(R.id.face_rec_test_gallery);
		final ImageAdapter imageAdapter = new ImageAdapter(this,
				mImageResouceId);
		mGallery.setAdapter(imageAdapter);
		mGallery.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				// TODO Auto-generated method stub

				// imageAdapter.getView(position, convertView, parent)

				// imageAdapter.notifyDataSetChanged();
				processImage(arg2);

				int[] Label = new int[1];
				
				double[] Confidence = new double[1];
				
				try {

					mFaceRec.predict(pre_view_res, Label, Confidence);

					Toast toast = Toast.makeText(
							getApplicationContext(),
							"Pridict Result: " + mImageResouceName[Label[0]],
							Toast.LENGTH_LONG);
					toast.setGravity(Gravity.CENTER, 0, 0);
					toast.show();

				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				mChangeViewTh.run();

			}
		});

		mChangeViewTh = new Thread() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				super.run();
				Message msg = new Message();
				msg.what = isNeedUpdateImg;
				dstBmp = Bitmap.createBitmap(AddPersonActivity.FaceWidth,
						AddPersonActivity.FaceHeight, Config.RGB_565);
				Utils.matToBitmap(pre_view_res, dstBmp);
				mTestImgUpdater.sendMessage(msg);

				try {
					sleep(50);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}

		};
		mTestImgUpdater = new Handler() {

			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);

				if (msg.what == isNeedUpdateImg) {
					mTestImg.setImageBitmap(dstBmp);
				}
			}

		};

	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();

	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		mRgba.release();
		mGray.release();
		pre_view_gray.release();
		pre_view_color.release();
		pre_view__resize.release();
		pre_view_gray_preproceseed.release();
		pre_view_res.release();
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this,
				mLoaderCallback);

	}

	private void processImage(int i) {
		readMat(this.mImageResouceId[i], mRgba);

		Imgproc.cvtColor(mRgba, mGray, Imgproc.COLOR_RGBA2GRAY);

		int minFaceSize = (int) Math.round(mGray.rows() * 0.0);
		MatOfRect faces = new MatOfRect();
		/*
		 * mFaceDetector.setMinFaceSize(minFaceSize);
		 * 
		 * mFaceDetector.detect(mGray, faces);
		 */

		if (mJavaFaceDetector != null)
			mJavaFaceDetector.detectMultiScale(mGray, faces, 1.1, 2, 2,
					new Size(minFaceSize, minFaceSize), new Size());

		/*
		 * if (mNativeFaceDetector != null) { mNativeFaceDetector.detect(mGray,
		 * faces);
		 * 
		 * }
		 */
		Rect[] facesArray = faces.toArray();

		for (int j = 0; j < facesArray.length; j++) {
			pre_view_gray = mGray.submat((int) facesArray[j].tl().y,
					(int) facesArray[j].tl().y + facesArray[j].height,
					(int) facesArray[j].tl().x, (int) facesArray[j].tl().x
							+ facesArray[j].width);
			/*
			 * pre_view_color = mRgba.submat((int) facesArray[j].tl().y, (int)
			 * facesArray[j].tl().y + facesArray[j].height, (int)
			 * facesArray[j].tl().x, (int) facesArray[j].tl().x +
			 * facesArray[j].width);
			 */

			pre_view_gray.copyTo(pre_view_gray_preproceseed);

			// mNativeFaceDetector.findIrisCenter(pre_view_color, 1, 1.2);

			Imgproc.resize(pre_view_gray_preproceseed, pre_view__resize,
					new Size(AddPersonActivity.FaceWidth,
							AddPersonActivity.FaceHeight));

			mNativeFaceDetector.getPreprocessingImage(pre_view__resize,
					pre_view_res, AddPersonActivity.FaceWidth,
					AddPersonActivity.FaceHeight);
			Core.rectangle(mRgba, facesArray[j].tl(), facesArray[j].br(),
					AddPersonActivity.FACE_RECT_COLOR, 3);
		}

	}

	private void trainDataSet() {

		for (int i = 0; i < mImageResouceId.length; i++) {
			processImage(i);
			Mat temp = new Mat();
			pre_view_res.copyTo(temp);
			faceVector.add(temp);
			labelVector.add(mImageResouceLabel[i]);

			mChangeViewTh.run();

			// preTh.stop();
		}
		label = Converters.vector_int_to_Mat(labelVector);
		mFaceRec.train(faceVector, label);
		Toast toast = Toast.makeText(getApplicationContext(), "Trained.",
				Toast.LENGTH_LONG);
		toast.setGravity(Gravity.CENTER, 0, 0);
		toast.show();
		return;

	}

	private void init() {
		Log.i(TAG, "init");
		mRgba = new Mat();
		mGray = new Mat();
		pre_view_gray = new Mat();
		pre_view_res = new Mat();
		pre_view_color = new Mat();
		pre_view__resize = new Mat();
		pre_view_gray_preproceseed = new Mat();
		return;
	}

	public void readMat(int resid, Mat dstMat) {
		Log.i(TAG, "readMat Start");
		dstBmp = readBitmap(resid);
		try {
			// Mat dstMat = new Mat();
			Utils.bitmapToMat(dstBmp, dstMat);
		} catch (Exception e) {
			// TODO: handle exception
			Log.i(TAG, e.toString());
		}

		return;
	}

	public Bitmap readBitmap(int resid) {
		Log.i(TAG, "readBitmap Start");
		return BitmapFactory.decodeResource(getResources(), resid);
	}

	public static void recycle(Bitmap bitmap) {
		if (bitmap != null && !bitmap.isRecycled()) {
			bitmap.recycle();
			System.gc();
		}
	}

	public class ImageAdapter extends BaseAdapter {

		Context mContext;
		int curPos;

		public ImageAdapter(Context context, Integer[] ImageResourceId) {
			this.mImageResourceIds = ImageResourceId;
			this.mContext = context;
		}

		public void setPos(int pos) {
			// TODO Auto-generated method stub
			// this.post =arg2;
			curPos = pos;

		}

		public int getCount() {
			return mImageResourceIds.length;
		}

		public Object getItem(int position) {
			return mImageResourceIds[position];
		}

		public long getItemId(int position) {
			return position;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			ImageView imageView = new ImageView(mContext);
			imageView.setImageResource(mImageResourceIds[position]);
			imageView.setLayoutParams(new Gallery.LayoutParams(120, 120));
			imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
			return imageView;
		}

		private Integer[] mImageResourceIds;
	}

}
