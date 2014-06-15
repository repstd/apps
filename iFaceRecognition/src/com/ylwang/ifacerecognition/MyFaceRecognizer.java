package com.ylwang.ifacerecognition;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.opencv.core.Mat;

import android.os.Environment;
import android.util.Log;
//Face Recognizer.The functions to recognize faces are implemented here.
public class MyFaceRecognizer {
	private String TAG = "MyFaceRecognizer";

	public MyFaceRecognizer(String recognizerType ) {
		mRecognizerType=recognizerType;
		mNativeEigenObj = nativeCreativeModel("FaceRecognizer::Eigenfaces");
		mNativeFisherObj=nativeCreativeModel("FaceRecognizer::Fisherfaces");
		mEigenFaceRec=new FaceRecExt(mNativeEigenObj);
		mFisherFaceRec=new FaceRecExt(mNativeFisherObj);
		mFaceRec = mEigenFaceRec;
		mModelName = "ylwang_FaceRec.xml";
		File FaceModel = new File(Environment.getExternalStorageDirectory(),mModelName);
		
		try {
			if (FaceModel.exists())
				Log.i(TAG,mModelName+"Exits");
			else
				FaceModel.createNewFile();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		mModelPath=FaceModel.getAbsolutePath();
		Log.i(TAG, "The MyFaceRecognizer Created.");

	}

	public void load(String filename) {

		mFaceRec.load(filename);

		return;
	}

	public void save(String filename) {

		mFaceRec.save(filename);

		return;
	}

	public void train(List<Mat> src, Mat labels) {

		mFaceRec.train(src, labels);
		save(mModelPath);
		Log.i(TAG, "SuccessFully Trained");
	}

	public void update(List<Mat> src, Mat labels) {
		
		mFaceRec.update(src, labels);
		save(mModelPath);
		Log.i(TAG, "SuccessFully Updated");
	}

	public void predict(Mat src, int[] label, double[] confidence) {
		//load(mModelPath);
		mFaceRec.predict(src, label, confidence);
		Log.i(TAG, "SuccessFully Predicted");

	}
	public double getSimilarity(Mat imgA,Mat imgB) {
		Log.i(TAG, "getSimilarity");
		return nativeGetSimilarity(imgA.getNativeObjAddr(),imgB.getNativeObjAddr());
		

	}
	public void setRecognizer(String recognizerType){
		mRecognizerType=recognizerType;
		
/*		switch(mRecognizerType){
		
		case AddPersonActivity.EigenFace:
			mFaceRec=mEigenFaceRec;
			break;
		case AddPersonActivity.FisherFace:
			mFaceRec=mFisherFaceRec;
			break;
		default:
			break;
			
		}*/
		if(mRecognizerType.equals(AddPersonActivity.EigenFace))
			mFaceRec=mEigenFaceRec;
		else if(mRecognizerType.equals(AddPersonActivity.FisherFace))
			mFaceRec=mFisherFaceRec;
			
		
	}
	public void test() {
		Log.i(TAG, "The MyFaceRecognizer Works Normally.");
	}
	private String mRecognizerType;
	private long mNativeEigenObj = 0;
	private long mNativeFisherObj = 0;
	private FaceRecExt mFaceRec=null;
	private FaceRecExt mEigenFaceRec=null;
	private FaceRecExt mFisherFaceRec=null;
	private String mModelName;
	private String mModelPath;
	private native long nativeCreativeModel(String Name);
	private native double nativeGetSimilarity(long ImgA,long ImgB);
}
