package com.ylwang.ifacerecognition;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Vector;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvException;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.utils.Converters;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

public class AddPersonActivity extends Activity implements
		CvCameraViewListener2 {
	private static final String TAG = "AddPerson";
	private static final String ImageProc = "CutSubImage";
	private static final String Predict = "AddPerson_Predict";
	static final String EigenFace = "FaceRecognizer::Eigenfaces";
	static final String FisherFace = "FaceRecognizer::Fisherfaces";
	private String curFaceRecognizer;
	private static int FaceDeteceted = 1;
	private static int Predicted = 2;
	private static int NeedTrainning = 3;
	private static int NeedReTraining = 4;
	public static final Scalar FACE_RECT_COLOR = new Scalar(0, 255, 0, 255);
	public static final int JAVA_DETECTOR = 0;
	public static final int NATIVE_DETECTOR = 1;
	public static final int FaceWidth = 128;
	public static final int FaceHeight = 128;
	private Handler mPreViewUpdaterHandler;
	private MenuItem mItemFace50;
	private MenuItem mItemFace40;
	private MenuItem mItemFace30;
	private MenuItem mItemFace20;
	private MenuItem mItemRecognizer;
	private Button mButtonTrain;
	private Button mButtonSave;
	private Mat mRgba;
	private Mat mGray;
	private Mat pre_view_gray;
	private Mat pre_view_color;
	private Mat pre_view_res;
	private Mat ROI;
	private File mCascadeFile;
	private int[] Label;
	private double[] Confidence;
	private File mCascadeFileEye;
	private MyFaceDetector mNativeFaceDetector;
	private MyFaceDetector mNativeEyeDetector;
	private MyFaceRecognizer mFaceRec;
	private int mDetectorType = NATIVE_DETECTOR;
	private float mRelativeFaceSize = 0.2f;
	private int mAbsoluteFaceSize = 0;

	protected CameraBridgeViewBase mOpenCvCameraView;
	private ImageView mFacePreview;
	private TextView mPredictRes;
	private Bitmap mFacePreviewBitmap = null;
	private int Status;
	private static final int ModeCollectingFaces = 1;
	private static int isTrained;
	private static final int ModeNotCollectingFaces = 2;
	protected static final String DiglogTAG = "Dialog_Usrname";
	public static HashMap<Integer, String> mLabelMap = new HashMap<Integer, String>();
	public static int mLabel = 0;
	public static Vector<Mat> faceVector = null;
	public static Vector<Integer> labelVector = null;
	private Mat mPreFace;
	private Mat mCurFace;
	android.app.AlertDialog.Builder builder;
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
					InputStream isEye = getResources().openRawResource(
							R.raw.haarcascade_eye);
					File cascadeDirEye = getDir("cascade", Context.MODE_PRIVATE);
					mCascadeFileEye = new File(cascadeDirEye,
							"haarcascade_eye.xml");
					FileOutputStream osEye = new FileOutputStream(
							mCascadeFileEye);

					byte[] bufferEye = new byte[4096];
					int bytesReadEye;
					while ((bytesReadEye = isEye.read(bufferEye)) != -1) {
						osEye.write(bufferEye, 0, bytesReadEye);
					}
					isEye.close();
					osEye.close();

					mNativeFaceDetector = new MyFaceDetector(
							mCascadeFile.getAbsolutePath(), 0,
							MyFaceDetector.TYPE_DetectionBasedTracker);
					mNativeEyeDetector = new MyFaceDetector(
							mCascadeFileEye.getAbsolutePath(), 0,
							MyFaceDetector.TYPE_DetectionBasedTracker);
					mFaceRec = new MyFaceRecognizer(curFaceRecognizer);

					cascadeDir.delete();

				} catch (IOException e) {
					e.printStackTrace();
					Log.e(TAG, "Failed to load cascade. Exception thrown: " + e);
				}

				mOpenCvCameraView.enableView();
			}
				break;
			default: {
				super.onManagerConnected(status);
			}
				break;
			}
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);

		setContentView(R.layout.person_add_surface_view);
		isTrained = 0;
		mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.fd_add_person_surface_view);
		mOpenCvCameraView.setCvCameraViewListener(this);
		mFacePreview = (ImageView) findViewById(R.id.face_dect_preview);
		mPredictRes = (TextView) findViewById(R.id.text_predict_res);
		mPredictRes.setBackgroundColor(android.graphics.Color.RED);
		mButtonTrain = (Button) findViewById(R.id.buttonTraining);
		mButtonSave = (Button) findViewById(R.id.buttonSave);
		curFaceRecognizer = EigenFace;
		Label = new int[1];
		Confidence = new double[1];
		Status = ModeNotCollectingFaces;
		try {
			// mLabelMap=new HashMap<Integer,String>();
			faceVector = new Vector<Mat>();
			labelVector = new Vector<Integer>();
			mLabel = mLabelMap.size();
		} catch (Exception e) {
			// TODO: handle exception
			Log.i(TAG, e.toString());
		}
		Log.i(TAG, "fdfdfd");
		mButtonTrain.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (Status == ModeNotCollectingFaces) {

					ShowAlertDialog();
					Status = ModeCollectingFaces;
					mButtonTrain.setText(R.string.menu_add_stop);
				} else {
					Status = ModeNotCollectingFaces;
					mButtonTrain.setText(R.string.menu_add_start);

				}
			}

		});
		mButtonSave.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				/*
				 * if(Status==ModeCollectingFaces)
				 * Status=ModeNotCollectingFaces; else return;
				 */
				Thread needTrainTh = new Thread() {

					@Override
					public void run() {
						// TODO Auto-generated method stub
						super.run();
						Message msg = new Message();
						msg.what = NeedTrainning;
						mPreViewUpdaterHandler.sendMessage(msg);
					}

				};
				needTrainTh.run();

			}

		});
		mPreViewUpdaterHandler = new Handler() {

			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				if (msg.what == FaceDeteceted) {
					mFacePreview.setImageBitmap(mFacePreviewBitmap);
				} else if (msg.what == Predicted) {

					mPredictRes.setText("This is " + mLabelMap.get(Label[0]));
				} else if (msg.what == NeedTrainning) {

					if (!faceVector.isEmpty() && !labelVector.isEmpty()
							&& Status == ModeNotCollectingFaces) {
						if (faceVector.size() == labelVector.size()) {
							Mat label = Converters
									.vector_int_to_Mat(labelVector);
							try {
								// mNativeFaceDetector.updateFaceRecognizer(faceVector,label);
								mFaceRec.train(faceVector, label);
								// mNativeFaceDetector.learnCollectedFaces(faceVector,
								// label);
							} catch (CvException e) {
								// TODO: handle exception
								Log.i(Predict, e.toString());
							}
						}

						faceVector.clear();

						labelVector.clear();
						if (isTrained == 0) {
							mButtonSave.setText(R.string.menu_add_save_predict);

							isTrained = 1;
						}
						/*
						 * else { mButtonSave.setText(R.string.menu_add_save);
						 * 
						 * isTrained = 0; }
						 */
						Log.i(TAG, "Updated");
					}

				} else if (msg.what == NeedReTraining) {

					mButtonSave.setText(R.string.menu_add_save);

					isTrained = 0;
				}
			}

		};

	}

	@Override
	public void onPause() {
		Log.i(TAG, "onPause");
		super.onPause();
		if (mOpenCvCameraView != null)
			mOpenCvCameraView.disableView();
		Intent intent = new Intent();
		intent.setClass(AddPersonActivity.this, MainActivity.class);

		startActivity(intent);
	}

	@Override
	public void onResume() {
		Log.i(TAG, "onResume");
		super.onResume();
		OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this,
				mLoaderCallback);

	}

	public void onDestroy() {
		Log.i(TAG, "onDestroy");
		super.onDestroy();
		mOpenCvCameraView.disableView();
	}

	public void onCameraViewStarted(int width, int height) {
		Log.i(TAG, "onCameraViewStarted");
		mGray = new Mat();
		mRgba = new Mat();
		mPreFace = new Mat();
		mCurFace = new Mat();
		pre_view_color = new Mat();
		pre_view_gray = new Mat();
		pre_view_res = new Mat();
		ROI = new Mat();

	}

	public void onCameraViewStopped() {
		Log.i(TAG, "onCameraViewStopped");
		mGray.release();
		mRgba.release();
		pre_view_color.release();
		pre_view_gray.release();
		mPreFace.release();
		mCurFace.release();
		pre_view_res.release();
		ROI.release();
	}

	public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
		Log.i(TAG, "onCameraFrame");

		mRgba = inputFrame.rgba();

		mGray = inputFrame.gray();

		if (mAbsoluteFaceSize == 0) {
			int height = mGray.rows();
			if (Math.round(height * mRelativeFaceSize) > 0) {
				mAbsoluteFaceSize = Math.round(height * mRelativeFaceSize);
			}
			mNativeFaceDetector.setMinFaceSize(mAbsoluteFaceSize);
		}

		MatOfRect faces = new MatOfRect();
		if (mDetectorType == NATIVE_DETECTOR) {
			if (mNativeFaceDetector != null)
				mNativeFaceDetector.detect(mGray, faces);

			/*
			 * if (mNativeEyeDetector != null) mNativeEyeDetector.detect(mGray,
			 * eyes);
			 */

		} else {
			Log.e(TAG, "Detection method is not selected!");

		}

		Rect[] facesArray = faces.toArray();

		for (int i = 0; i < facesArray.length; i++) {

			try {
				mFacePreviewBitmap = Bitmap.createBitmap(FaceWidth, FaceHeight,
						Config.RGB_565);
				pre_view_gray = mGray.submat((int) facesArray[i].tl().y,
						(int) facesArray[i].tl().y + facesArray[i].height,
						(int) facesArray[i].tl().x, (int) facesArray[i].tl().x
								+ facesArray[i].width);
				pre_view_color = mRgba.submat((int) facesArray[i].tl().y,
						(int) facesArray[i].tl().y + facesArray[i].height,
						(int) facesArray[i].tl().x, (int) facesArray[i].tl().x
								+ facesArray[i].width);

				Mat pre_view__resize = new Mat();
				Mat pre_view_gray_preproceseed = new Mat();
				pre_view_gray.copyTo(pre_view_gray_preproceseed);

				mNativeFaceDetector.findIrisCenter(pre_view_color, 2, 1.2);

				Imgproc.resize(pre_view_gray_preproceseed, pre_view__resize,
						new Size(FaceWidth, FaceHeight));

				mNativeFaceDetector.getPreprocessingImage(pre_view__resize,
						pre_view_res, FaceWidth, FaceHeight);

				if (Status == ModeCollectingFaces) {
					Mat temp = new Mat();
					pre_view_res.copyTo(temp);
					faceVector.add(temp);
					labelVector.add(mLabel);

				}
				if (isTrained == 1) {

					// Log.i(Predict,Integer.toString(Label.length));
					// Log.i(Predict,Integer.toString(mNativeFaceDetector.predict(pre_view_res)));
					try {
						mFaceRec.predict(pre_view_res, Label, Confidence);
						Thread preTh = new Thread() {

							@Override
							public void run() {
								// TODO Auto-generated method stub
								super.run();
								Message msg = new Message();
								msg.what = Predicted;
								mPreViewUpdaterHandler.sendMessage(msg);
							}

						};
						preTh.run();
					} catch (CvException e) {
						Log.i(Predict, e.toString());
						// TODO Auto-generated catch block

					}

					Log.i(Predict, "Predicted");
					// Log.i(Predict,Label.toString());
					Log.i(Predict, Integer.toString(Label[0]));

				}

				Utils.matToBitmap(pre_view_res, mFacePreviewBitmap);
				Thread t = new Thread() {

					@Override
					public void run() {
						// TODO Auto-generated method stub
						super.run();
						Message msg = new Message();
						msg.what = FaceDeteceted;
						mPreViewUpdaterHandler.sendMessage(msg);
					}

				};
				t.run();

			} catch (Exception e) {
				// TODO: handle exception
				Log.i(ImageProc, e.toString());
				System.out.println(e.toString());
			}
			Core.rectangle(mRgba, facesArray[i].tl(), facesArray[i].br(),
					FACE_RECT_COLOR, 3);

		}

		/*
		 * Rect[] eyesArray = eyes.toArray();
		 * 
		 * for (int i = 0; i < eyesArray.length; i++) { Core.rectangle(mRgba,
		 * eyesArray[i].tl(), eyesArray[i].br(), FACE_RECT_COLOR, 3); }
		 */
		return mRgba;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		Log.i(TAG, "called onCreateOptionsMenu");
		mItemFace50 = menu.add("MinimumFaceSize 50%");
		mItemFace40 = menu.add("MinimumFaceSize 40%");
		mItemFace30 = menu.add("MinimumFaceSize 30%");
		mItemFace20 = menu.add("MinimumFaceSize 20%");
		mItemRecognizer = menu.add(R.string.menu_main_recognizer_eigen);
		// mItemType = menu.add(mDetectorName[mDetectorType]);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Log.i(TAG, "called onOptionsItemSelected; selected item: " + item);
		if (item == mItemFace50)
			setMinFaceSize(0.5f);
		else if (item == mItemFace40)
			setMinFaceSize(0.4f);
		else if (item == mItemFace30)
			setMinFaceSize(0.3f);
		else if (item == mItemFace20)
			setMinFaceSize(0.2f);
		else if (item == mItemRecognizer) {
			if (curFaceRecognizer.equals(EigenFace)) {
				curFaceRecognizer = FisherFace;
			} else {
				curFaceRecognizer = EigenFace;
			}
			mItemRecognizer.setTitle(curFaceRecognizer);
			mFaceRec.setRecognizer(curFaceRecognizer);
			if (isTrained == 1) {

				Thread t = new Thread() {

					@Override
					public void run() {
						// TODO Auto-generated method stub
						super.run();
						Message msg = new Message();
						msg.what = NeedReTraining;
						mPreViewUpdaterHandler.sendMessage(msg);
					}

				};
				t.run();
			}

		}

		return true;
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		return super.onContextItemSelected(item);

	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		// TODO Auto-generated method stub
		return super.onMenuItemSelected(featureId, item);
	}

	private void setMinFaceSize(float faceSize) {
		mRelativeFaceSize = faceSize;
		mAbsoluteFaceSize = 0;
	}

	private void ShowAlertDialog() {

		builder = new android.app.AlertDialog.Builder(
				com.ylwang.ifacerecognition.AddPersonActivity.this);
		LayoutInflater inflater = com.ylwang.ifacerecognition.AddPersonActivity.this
				.getLayoutInflater();
		final View start_dialog = inflater.inflate(R.layout.add_start_dialog,
				null);
		builder.setView(start_dialog);
		builder.setPositiveButton(R.string.add_dialog_ok,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id) {
						// sign in the user ...
						EditText tv1 = (EditText) start_dialog
								.findViewById(R.id.username);
						String usr_to_add = tv1.getText().toString();
						if (!usr_to_add.isEmpty()) {

							if (!mLabelMap.containsValue(usr_to_add)) {
								mLabel += 1;
								mLabelMap.put(mLabel, usr_to_add);
								Log.i(DiglogTAG, Integer.toString(mLabel));
								Log.i(DiglogTAG,
										Integer.toString(mLabelMap.size()));

								Log.i(DiglogTAG, usr_to_add);
							}
						}

					}
				});
		builder.show();
		return;
	}

}
