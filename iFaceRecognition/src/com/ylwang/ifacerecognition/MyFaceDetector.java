package com.ylwang.ifacerecognition;

import java.io.File;
import java.io.IOException;
import java.util.Vector;

import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.utils.Converters;

import android.os.Environment;
import android.util.Log;
//Face Detector.The functions to detect faces and pre-process images are implemented here.
public class MyFaceDetector {
	//Face Detector.The functions to detect faces and pre-process images are implemented here.
	private String TAG = "DetectionBasedTracker";
	public static final int TYPE_DetectionBasedTracker = 1;
	public static final int TYPE_CascadeClassfier = 2;

	public MyFaceDetector(String cascadeName, int minFaceSize,int detectorType) {
		if(detectorType==TYPE_CascadeClassfier)
			mDetectorType=TYPE_CascadeClassfier;
		else	
			mDetectorType=TYPE_DetectionBasedTracker;
		// mNativeObj = nativeCreateObject(cascadeName, minFaceSize);
		switch (mDetectorType) {

		case TYPE_DetectionBasedTracker:
			mNativeObj = nativeCreateDetectionBasedTrackedObjectObject(
					cascadeName, minFaceSize);
			break;
		case TYPE_CascadeClassfier:
			mNativeObj = nativeCreateCascadeClassifierObject(cascadeName);
			mSize = minFaceSize;
			break;
		}

		mModelName = "ylwang_FaceRec.xml";
		mAlgoName = "FaceRecognizer.Eigenfaces";
		File FaceModel = new File(Environment.getExternalStorageDirectory(),
				mModelName);

		try {
			if (FaceModel.exists())
				Log.i(TAG, mModelName + "Exits");
			else
				FaceModel.createNewFile();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		mModelPath = FaceModel.getAbsolutePath();
		createFaceAlgorithm(mAlgoName, mModelPath);

	}

	public void start() {
		if (mDetectorType == TYPE_DetectionBasedTracker)
			nativeStart(mNativeObj);
	}

	public void stop() {
		if (mDetectorType == TYPE_DetectionBasedTracker)
			nativeStop(mNativeObj);
	}

	public void setMinFaceSize(int size) {
		switch (mDetectorType) {
		case TYPE_CascadeClassfier:
			mSize = size;
			break;
		case TYPE_DetectionBasedTracker:

			nativeSetFaceSize(mNativeObj, size);
			break;
		}

	}

	public void equalizeLeftAndRightHalves(Mat pre_view_res) {
		// TODO Auto-generated method stub
		nativeEqualizeLeftAndRightHalves(mNativeObj,
				pre_view_res.getNativeObjAddr());
	}

	public void getPreprocessingImage(Mat faceImg, Mat outImg,
			int desiredWidth, int desiredHeight) {
		// TODO Auto-generated method stub
		nativeGetPreprocessingImage(mNativeObj, faceImg.getNativeObjAddr(),
				outImg.getNativeObjAddr(), desiredWidth, desiredHeight);

	}

	public void detect(Mat imageGray, MatOfRect faces) {
		switch (mDetectorType) {

		case TYPE_CascadeClassfier:
			MyFaceDetector.nativeCascadeClassfierDetect(mNativeObj,
					imageGray.getNativeObjAddr(), faces.getNativeObjAddr(),
					mSize);
			break;
		case TYPE_DetectionBasedTracker:
			nativeDetectionBasedTrackedDetect(mNativeObj,
					imageGray.getNativeObjAddr(), faces.getNativeObjAddr());
			break;

		}

	}

	public void release() {

		switch (mDetectorType) {

		case TYPE_CascadeClassfier:
			nativeDestroyCascadeClassifierObject(mNativeObj);
			break;
		case TYPE_DetectionBasedTracker:
			nativeDestroyDetectionBasedTrackedObjectObject(mNativeObj);
			break;

		}
		mNativeObj = 0;
	}

	public void createFaceAlgorithm(String facerecAlgorithm, String modelName) {

		nativeCreateModel(mNativeObj, facerecAlgorithm, modelName);
	}

	public void learnCollectedFaces(Vector<Mat> preprocessedFaces,
			Mat faceLabels) {
		Mat preprocessedFacesMat = Converters
				.vector_Mat_to_Mat(preprocessedFaces);
		// Mat preprocessedFacesMat=Mat(preprocessedFaces);
		nativelearnCollectedFaces(mNativeObj, mModelPath, mAlgoName,
				preprocessedFacesMat.getNativeObjAddr(),
				faceLabels.getNativeObjAddr(), preprocessedFaces.size());

		return;
	}

	public void updateFaceRecognizer(Vector<Mat> preprocessedFaces,
			Mat faceLabels) {
		Mat preprocessedFacesMat = Converters
				.vector_Mat_to_Mat(preprocessedFaces);
		nativeUpdate(mNativeObj, mModelPath, mAlgoName,
				preprocessedFacesMat.getNativeObjAddr(),
				faceLabels.getNativeObjAddr(), preprocessedFaces.size());
		return;
	}

	public double getSimilarity(Mat matA, Mat matB) {
		return nativeGetSimilarity(mNativeObj, matA.getNativeObjAddr(),
				matB.getNativeObjAddr());
	}

	public void findIrisCenter(Mat faceImg, double threshold, double ratio) {

		nativeFindIrisCenter(mNativeObj, faceImg.getNativeObjAddr(), threshold,
				ratio);
	}

	public int predict(Mat faceImg) {

		return nativePredict(mNativeObj, mModelPath, mAlgoName,
				faceImg.getNativeObjAddr());
	}

	public boolean getModelStatus() {
		return isModelExist;
	}

	private long mNativeObj = 0;
	private long mFaceRecongnizer;
	private String mModelName;
	private String mModelPath;
	private String mAlgoName;
	private boolean isModelExist = false;
	private int mSize;
	private int mDetectorType = 1;
	
	
	private static native long nativeCreateObject(String cascadeName,
			int minFaceSize);
	
	private static native long nativeCreateDetectionBasedTrackedObjectObject(
			String cascadeName, int minFaceSize);

	private static native long nativeCreateCascadeClassifierObject(
			String cascadeName);

	private static native void nativeDestroyObject(long thiz);

	private static native void nativeDestroyDetectionBasedTrackedObjectObject(
			long thiz);

	private static native void nativeDestroyCascadeClassifierObject(long thiz);

	private static native void nativeStart(long thiz);

	private static native void nativeStop(long thiz);

	private static native void nativeSetFaceSize(long thiz, int size);

	private static native void nativeDetect(long thiz, long inputImage,
			long faces);

	private static native void nativeDetectionBasedTrackedDetect(long thiz,
			long inputImage, long faces);

	private static native void nativeCascadeClassfierDetect(long thiz,
			long inputImage, long faces, int size);

	private static native void nativeEqualizeLeftAndRightHalves(long thiz,
			long faceImg);

	private static native void nativeGetPreprocessingImage(long thiz,
			long faceImg, long outImg, int desiredWidth, int desiredHeight);

	private static native void nativeTestDataTrans(long thiz, long data);

	private static native double nativeGetSimilarity(long thiz, long matA,
			long matB);

	private static native void nativeCreateModel(long thiz,
			String facerecAlgorithm, String mModelPath);

	private static native void nativelearnCollectedFaces(long thiz,
			String mModelPath, String facerecAlgorithm, long preprocessedFaces,
			long faceLabels, int cnt);

	private static native void nativeUpdate(long thiz, String mModelPath,
			String facerecAlgorithm, long preprocessedFaces, long faceLabels,
			int cnt);

	private static native int nativePredict(long thiz, String mModelPath,
			String facerecAlgorithm, long preprocessedFaceInput);

	private static native void nativeFindIrisCenter(long thiz, long faceImg,
			double threshold, double ratio);

}
