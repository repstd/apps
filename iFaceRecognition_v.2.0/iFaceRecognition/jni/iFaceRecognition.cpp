#include <vector>
#include "com_ylwang_ifacerecognition_MyFaceRecognizer.h"
#include "com_ylwang_ifacerecognition_MyFaceDetector.h"
#include "RecogLib/ImageUtils.h"
#include "RecogLib/recognition.h"
#include <jni.h>
#include "opencv2/core/core.hpp"
#include "opencv2/contrib/detection_based_tracker.hpp"
#include <string>
#include "cv.h"
#include "highgui.h"
#include <opencv2/core/core.hpp>
#include <opencv2/highgui/highgui.hpp>
#include <opencv2/ml/ml.hpp>

#include <android/log.h>
#define M_RED Scalar(255,0,0,255)
#define M_BLUE Scalar(0,0,255,255)
#define M_GREEN Scalar(0,255,0,255)
#define LOG_TAG "FaceDetection/DetectionBasedTracker"
#define LOG_Err_TAG "NativeDetectionBasedTracker_Error!"
#define LOG_Err_Train_TAG "NativeDetectionBasedTracker_Train_Error!"
#define LOGD(...) ((void)__android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__))
#define LOGD_ERR(...) ((void)__android_log_print(ANDROID_LOG_DEBUG, LOG_Err_TAG, __VA_ARGS__))
#define LOGD_Train_ERR(...) ((void)__android_log_print(ANDROID_LOG_DEBUG, LOG_Err_Train_TAG, __VA_ARGS__))
using namespace std;
using namespace cv;

inline void vector_Rect_to_Mat(vector<Rect>& v_rect, Mat& mat) {
	mat = Mat(v_rect, true);

}
inline void Mat_to_vector_Mat(Mat& mat, vector<Mat>& v_mat, int cnt) {
	int mat_size = mat.rows;
	int row_per_mat = mat_size / cnt;
	for (int i = 0; i < cnt; i++) {
		Mat temp_mat = mat.rowRange(i * row_per_mat, (i + 1) * row_per_mat);
		v_mat.push_back(temp_mat);
	}

}

JNIEXPORT jlong JNICALL Java_com_ylwang_ifacerecognition_MyFaceDetector_nativeCreateObject(
		JNIEnv * jenv, jclass jClass, jstring jFileName, jint faceSize) {
	return Java_com_ylwang_ifacerecognition_MyFaceDetector_nativeCreateDetectionBasedTrackedObjectObject(
			jenv, jClass, jFileName, faceSize);
}
JNIEXPORT jlong JNICALL Java_com_ylwang_ifacerecognition_MyFaceDetector_nativeCreateDetectionBasedTrackedObjectObject(
		JNIEnv * jenv, jclass, jstring jFileName, jint faceSize) {
	LOGD(
			"Java_com_ylwang_ifacerecognition_MyFaceDetector_nativeCreateDetectionBasedTrackedObjectObject enter");

	const char* jnamestr = jenv->GetStringUTFChars(jFileName, NULL);
	string stdFileName(jnamestr);
	jlong result = 0;

	try {
		DetectionBasedTracker::Parameters DetectorParams;
		if (faceSize > 0)
			DetectorParams.minObjectSize = faceSize;
		result = (jlong) new DetectionBasedTracker(stdFileName, DetectorParams);

	} catch (cv::Exception& e) {
		LOGD("nativeCreateObject caught cv::Exception: %s", e.what());
		jclass je = jenv->FindClass("org/opencv/core/CvException");
		if (!je)
			je = jenv->FindClass("java/lang/Exception");
		jenv->ThrowNew(je, e.what());
	} catch (...) {
		LOGD("nativeCreateObject caught unknown exception");
		jclass je = jenv->FindClass("java/lang/Exception");
		jenv->ThrowNew(je,
				"Unknown exception in JNI code {highgui::VideoCapture_n_1VideoCapture__()}");
		return 0;
	}

	LOGD(
			"Java_com_ylwang_ifacerecognition_MyFaceDetector_nativeCreateDetectionBasedTrackedObjectObject exit");
	return result;
}
JNIEXPORT jlong JNICALL Java_com_ylwang_ifacerecognition_MyFaceDetector_nativeCreateCascadeClassifierObject(
		JNIEnv *jenv, jclass, jstring jFileName) {
	LOGD(
			"Java_com_ylwang_ifacerecognition_MyFaceDetector_nativeCreateCascadeClassifierObject enter");

	const char* jnamestr = jenv->GetStringUTFChars(jFileName, NULL);
	string stdFileName(jnamestr);
	jlong result = 0;
	CascadeClassifier face_cascade;
	try {
		result = (jlong) new CascadeClassifier(stdFileName);
	} catch (cv::Exception& e) {
		LOGD(e.what());
		exit(1);
	}

	LOGD(
			"Java_com_ylwang_ifacerecognition_MyFaceDetector_nativeCreateCascadeClassifierObject exit");
	return result;

}
JNIEXPORT void JNICALL Java_com_ylwang_ifacerecognition_MyFaceDetector_nativeDestroyObject(
		JNIEnv * jenv, jclass jClass, jlong thiz) {
	Java_com_ylwang_ifacerecognition_MyFaceDetector_nativeDestroyDetectionBasedTrackedObjectObject(
			jenv, jClass, thiz);
}
JNIEXPORT void JNICALL Java_com_ylwang_ifacerecognition_MyFaceDetector_nativeDestroyDetectionBasedTrackedObjectObject(
		JNIEnv * jenv, jclass, jlong thiz) {
	LOGD(
			"Java_com_ylwang_ifacerecognition_MyFaceDetector_nativeDestroyDetectionBasedTrackedObjectObject enter");
	try {
		if (thiz != 0) {
			((DetectionBasedTracker*) thiz)->stop();
			delete (DetectionBasedTracker*) thiz;
		}
	} catch (cv::Exception& e) {
		LOGD("nativeestroyObject caught cv::Exception: %s", e.what());
		jclass je = jenv->FindClass("org/opencv/core/CvException");
		if (!je)
			je = jenv->FindClass("java/lang/Exception");
		jenv->ThrowNew(je, e.what());
	} catch (...) {
		LOGD("nativeDestroyObject caught unknown exception");
		jclass je = jenv->FindClass("java/lang/Exception");
		jenv->ThrowNew(je,
				"Unknown exception in JNI code {highgui::VideoCapture_n_1VideoCapture__()}");
	}
	LOGD(
			"Java_com_ylwang_ifacerecognition_MyFaceDetector_nativeDestroyDetectionBasedTrackedObjectObject exit");
}

JNIEXPORT void JNICALL Java_com_ylwang_ifacerecognition_MyFaceDetector_nativeDestroyCascadeClassifierObject(
		JNIEnv *, jclass, jlong thiz) {

	LOGD(
			"Java_com_ylwang_ifacerecognition_MyFaceDetector_nativeDestroyCascadeClassifierObject exit");

	try {
		if (thiz != 0) {

			delete (CascadeClassifier*) thiz;
		}
	} catch (cv::Exception& e) {
		LOGD(e.what());
	}

	LOGD(
			"Java_com_ylwang_ifacerecognition_MyFaceDetector_nativeDestroyCascadeClassifierObject exit");
}

JNIEXPORT void JNICALL Java_com_ylwang_ifacerecognition_MyFaceDetector_nativeStart(
		JNIEnv * jenv, jclass, jlong thiz) {
	LOGD( "Java_com_ylwang_ifacerecognition_MyFaceDetector_nativeStart enter");
	try {
		((DetectionBasedTracker*) thiz)->run();
	}

	catch (cv::Exception& e) {
		LOGD("nativeStart caught cv::Exception: %s", e.what());
		jclass je = jenv->FindClass("org/opencv/core/CvException");
		if (!je)
			je = jenv->FindClass("java/lang/Exception");
		jenv->ThrowNew(je, e.what());
	} catch (...) {
		LOGD("nativeStart caught unknown exception");
		jclass je = jenv->FindClass("java/lang/Exception");
		jenv->ThrowNew(je,
				"Unknown exception in JNI code {highgui::VideoCapture_n_1VideoCapture__()}");
	}
	LOGD( "Java_com_ylwang_ifacerecognition_MyFaceDetector_nativeStart exit");
}

JNIEXPORT void JNICALL Java_com_ylwang_ifacerecognition_MyFaceDetector_nativeStop(
		JNIEnv * jenv, jclass, jlong thiz) {
	LOGD(
			"Java_org_opencv_samples_facedetect_DetectionBasedTracker_nativeStop enter");
	try {
		((DetectionBasedTracker*) thiz)->stop();
	} catch (cv::Exception& e) {
		LOGD("nativeStop caught cv::Exception: %s", e.what());
		jclass je = jenv->FindClass("org/opencv/core/CvException");
		if (!je)
			je = jenv->FindClass("java/lang/Exception");
		jenv->ThrowNew(je, e.what());
	} catch (...) {
		LOGD("nativeStop caught unknown exception");
		jclass je = jenv->FindClass("java/lang/Exception");
		jenv->ThrowNew(je,
				"Unknown exception in JNI code {highgui::VideoCapture_n_1VideoCapture__()}");
	}
	LOGD( "Java_com_ylwang_ifacerecognition_MyFaceDetector_nativeStop exit");
}

JNIEXPORT void JNICALL Java_com_ylwang_ifacerecognition_MyFaceDetector_nativeSetFaceSize(
		JNIEnv * jenv, jclass, jlong thiz, jint faceSize) {
	LOGD(
			"Java_com_ylwang_ifacerecognition_MyFaceDetector_nativeSetFaceSize enter");
	try {
		if (faceSize > 0) {
			DetectionBasedTracker::Parameters DetectorParams =
					((DetectionBasedTracker*) thiz)->getParameters();
			DetectorParams.minObjectSize = faceSize;
			((DetectionBasedTracker*) thiz)->setParameters(DetectorParams);
		}
	} catch (cv::Exception& e) {
		LOGD("nativeStop caught cv::Exception: %s", e.what());
		jclass je = jenv->FindClass("org/opencv/core/CvException");
		if (!je)
			je = jenv->FindClass("java/lang/Exception");
		jenv->ThrowNew(je, e.what());
	} catch (...) {
		LOGD("nativeSetFaceSize caught unknown exception");
		jclass je = jenv->FindClass("java/lang/Exception");
		jenv->ThrowNew(je,
				"Unknown exception in JNI code {highgui::VideoCapture_n_1VideoCapture__()}");
	}
	LOGD(
			"Java_com_ylwang_ifacerecognition_MyFaceDetector_nativeSetFaceSize exit");
}

JNIEXPORT void JNICALL Java_com_ylwang_ifacerecognition_MyFaceDetector_nativeDetect(
		JNIEnv * jenv, jclass jClass, jlong thiz, jlong imageGray,
		jlong faces) {
	Java_com_ylwang_ifacerecognition_MyFaceDetector_nativeDetectionBasedTrackedDetect(
			jenv, jClass, thiz, imageGray, faces);

}
JNIEXPORT void JNICALL Java_com_ylwang_ifacerecognition_MyFaceDetector_nativeDetectionBasedTrackedDetect(
		JNIEnv * jenv, jclass, jlong thiz, jlong imageGray, jlong faces) {

	LOGD(
			"Java_com_ylwang_ifacerecognition_MyFaceDetector_nativeDetectionBasedTrackedDetect enter");
	try {
		vector<Rect> RectFaces;

		((DetectionBasedTracker*) thiz)->process(*((Mat*) imageGray));

		((DetectionBasedTracker*) thiz)->getObjects(RectFaces);
		vector_Rect_to_Mat(RectFaces, *((Mat*) faces));
	} catch (cv::Exception& e) {
		LOGD("nativeCreateObject caught cv::Exception: %s", e.what());
		jclass je = jenv->FindClass("org/opencv/core/CvException");
		if (!je)
			je = jenv->FindClass("java/lang/Exception");
		jenv->ThrowNew(je, e.what());
	} catch (...) {
		LOGD("nativeDetect caught unknown exception");
		jclass je = jenv->FindClass("java/lang/Exception");
		jenv->ThrowNew(je,
				"Unknown exception in JNI code {highgui::VideoCapture_n_1VideoCapture__()}");
	}
	LOGD(
			"Java_com_ylwang_ifacerecognition_MyFaceDetector_nativeDetectionBasedTrackedDetect exit");
}

JNIEXPORT void JNICALL Java_com_ylwang_ifacerecognition_MyFaceDetector_nativeCascadeClassfierDetect(
		JNIEnv * jenv, jclass, jlong thiz, jlong imageGray, jlong faces,
		jint size) {

	LOGD(
			"Java_com_ylwang_ifacerecognition_MyFaceDetector_nativeCascadeClassfierDetect enter");
	vector<Rect> RectFaces;

	try {
		((CascadeClassifier*) thiz)->detectMultiScale(*((Mat*) imageGray),
				RectFaces, 1.1, 2, 0 | CV_HAAR_SCALE_IMAGE, Size(size, size));

		vector_Rect_to_Mat(RectFaces, *((Mat*) faces));
	} catch (cv::Exception& e) {
		LOGD(e.what());
	}
	LOGD(
			"Java_com_ylwang_ifacerecognition_MyFaceDetector_nativeCascadeClassfierDetect exit");

}
void equalizeLeftAndRightHalves(Mat &faceImg) {
	LOGD_ERR("equalizeLeftAndRightHalves Enter");
	//cv::cvtColor(faceImg,faceImg,cv::DEPTH_MASK_8U);
	int w = faceImg.cols;
	int h = faceImg.rows;

	// 1) First, equalize the whole face.
	Mat wholeFace;
	equalizeHist(faceImg, wholeFace);

	// 2) Equalize the left half and the right half of the face separately.
	int midX = w / 2;
	Mat leftSide = faceImg(Rect(0, 0, midX, h));
	Mat rightSide = faceImg(Rect(midX, 0, w - midX, h));
	equalizeHist(leftSide, leftSide);
	equalizeHist(rightSide, rightSide);

	// 3) Combine the left half and right half and whole face together, so that it has a smooth transition.
	for (int y = 0; y < h; y++) {
		for (int x = 0; x < w; x++) {
			int v;
			if (x < w / 4) { // Left 25%: just use the left face.
				v = leftSide.at<uchar>(y, x);
			} else if (x < w * 2 / 4) { // Mid-left 25%: blend the left face & whole face.
				int lv = leftSide.at<uchar>(y, x);
				int wv = wholeFace.at<uchar>(y, x);
				// Blend more of the whole face as it moves further right along the face.
				float f = (x - w * 1 / 4) / (float) (w * 0.25f);
				v = cvRound((1.0f - f) * lv + (f) * wv);
			} else if (x < w * 3 / 4) { // Mid-right 25%: blend the right face & whole face.
				int rv = rightSide.at<uchar>(y, x - midX);
				int wv = wholeFace.at<uchar>(y, x);
				// Blend more of the right-side face as it moves further right along the face.
				float f = (x - w * 2 / 4) / (float) (w * 0.25f);
				v = cvRound((1.0f - f) * wv + (f) * rv);
			} else { // Right 25%: just use the right face.
				v = rightSide.at<uchar>(y, x - midX);
			}
			faceImg.at<uchar>(y, x) = v;
		} // end x loop
	} //end y loop
	LOGD_ERR("equalizeLeftAndRightHalves Exit");
}

JNIEXPORT void JNICALL Java_com_ylwang_ifacerecognition_MyFaceDetector_nativeEqualizeLeftAndRightHalves(
		JNIEnv * jenv, jclass jc, jlong thiz, jlong faceimg) {
	LOGD(
			"Java_com_ylwang_ifacerecognition_MyFaceDetector_nativeEqualizeLeftAndRightHalves enter");
	try {
		equalizeLeftAndRightHalves(*((Mat*) faceimg));
	} catch (cv::Exception& e) {
		LOGD(
				"nativeEqualizeLeftAndRightHalves caught cv::Exception: %s", e.what());
		jclass je = jenv->FindClass("org/opencv/core/CvException");
		if (!je)
			je = jenv->FindClass("java/lang/Exception");
		jenv->ThrowNew(je, e.what());

	}
	LOGD(
			"Java_com_ylwang_ifacerecognition_MyFaceDetector_nativeEqualizeLeftAndRightHalves exit");
	return;
}
JNIEXPORT void JNICALL Java_com_ylwang_ifacerecognition_MyFaceDetector_nativeGetPreprocessingImage(
		JNIEnv * jenv, jclass jc, jlong thiz, jlong faceImg, jlong outImg,
		jint desiredWidth, jint desiredHeight) {
	LOGD(
			"Java_com_ylwang_ifacerecognition_MyFaceDetector_nativeGetPreprocessingImage enter");
	Java_com_ylwang_ifacerecognition_MyFaceDetector_nativeEqualizeLeftAndRightHalves(
			jenv, jc, thiz, faceImg);
	Mat filtered = Mat(((Mat*) faceImg)->size(), CV_8U);
	bilateralFilter(*((Mat*) faceImg), filtered, 0, 20.0, 2.0);
	Mat mask = Mat(((Mat*) faceImg)->size(), CV_8U, Scalar(0));
	double dw = desiredWidth;
	double dh = desiredHeight;
	Point faceCenter = Point(cvRound(dw * 0.5), cvRound(dh * 0.4));
	Size size = Size(cvRound(dw * 0.45), cvRound(dh * 0.8));
	ellipse(mask, faceCenter, size, 0, 0, 360, Scalar(255), CV_FILLED);

	//Mat dstImg = Mat(mask.size(), CV_8U, Scalar(128));
	filtered.copyTo(*((Mat*) outImg), mask);
	LOGD(
			"Java_com_ylwang_ifacerecognition_MyFaceDetector_nativeGetPreprocessingImage exit");

}

JNIEXPORT jdouble JNICALL Java_com_ylwang_ifacerecognition_MyFaceDetector_nativeGetSimilarity(
		JNIEnv *, jclass, jlong thiz, jlong matA, jlong matB) {

	LOGD(
			"Java_com_ylwang_ifacerecognition_MyFaceDetector_nativeGetSimilarity enter");

	return (double) getSimilarity(*((Mat *) matA), *((Mat *) matB));

	LOGD(
			"Java_com_ylwang_ifacerecognition_MyFaceDetector_nativeGetSimilarity exit");

}

/*JNIEXPORT void JNICALL Java_com_ylwang_ifacerecognition_MyFaceDetector_nativeCreateFaceAlgorithm(
 JNIEnv * jenv, jclass, jlong thiz, jlong mModel,
 jstring facerecAlgorithm) {
 LOGD(
 "Java_com_ylwang_ifacerecognition_MyFaceDetector_nativeCreateFaceAlgorithm enter");
 //LOGD_ERR("Line 297");
 //Ptr<FaceRecognizer> model;
 cout << "Learning the collected faces using the [" << facerecAlgorithm
 << "] algorithm ..." << endl;

 // Make sure the "contrib" module is dynamically loaded at runtime.
 // Requires OpenCV v2.4.1 or later (from June 2012), otherwise the FaceRecognizer will not compile or run!
 try {
 bool haveContribModule = initModule_contrib();
 //LOGD_ERR("Line 306");
 if (!haveContribModule) {
 cerr
 << "ERROR: The 'contrib' module is needed for FaceRecognizer but has not been loaded into OpenCV!"
 << endl;
 LOGD_ERR(
 "ERROR: The 'contrib' module is needed for FaceRecognizer but has not been loaded into OpenCV!");
 exit(1);
 }
 LOGD_ERR("Contrib Initionalized Successfully");
 const char* jnamestr = jenv->GetStringUTFChars(facerecAlgorithm, NULL);
 string stdFacerecAlgorithmName(jnamestr);
 Ptr<FaceRecognizer> model;

 // Use the new FaceRecognizer class in OpenCV's "contrib" module:
 // Requires OpenCV v2.4.1 or later (from June 2012), otherwise the FaceRecognizer will not compile or run!
 try {
 LOGD_ERR("Try to Create Alg");
 *((Ptr<FaceRecognizer>*) mModel) = Algorithm::create<FaceRecognizer>(
 *((string*) facerecAlgorithm));

 model = Algorithm::create<FaceRecognizer>(stdFacerecAlgorithmName);

 LOGD_ERR("Make to Create Alg");
 } catch (cv::Exception& e) {
 LOGD_ERR(e.what());
 }
 if (model.empty()) {
 cerr << "ERROR: The FaceRecognizer algorithm [" << facerecAlgorithm
 << "] is not available in your version of OpenCV. Please update to OpenCV v2.4.1 or newer."
 << endl;
 LOGD("ERROR: The FaceRecognizer algorithm [");
 exit(1);
 }
 } catch (cv::Exception& e) {
 LOGD(e.what());
 }
 //  return NULL;
 //jenv->ReleaseStringUTFChars(facerecAlgorithm, NULL);
 LOGD(
 "Java_com_ylwang_ifacerecognition_MyFaceDetector_nativeCreateFaceAlgorithm exit");
 return;

 }*/
Ptr<FaceRecognizer> CreateModel(String stdFacerecAlgorithmName) {

	bool haveContribModule = initModule_contrib();
	if (!haveContribModule) {
		cerr
				<< "ERROR: The 'contrib' module is needed for FaceRecognizer but has not been loaded into OpenCV!"
				<< endl;
		LOGD_ERR(
				"ERROR: The 'contrib' module is needed for FaceRecognizer but has not been loaded into OpenCV!");
		exit(1);
	}
	LOGD_ERR("Contrib Initionalized Successfully");
	Ptr<FaceRecognizer> model;
	try {
		model = Algorithm::create<FaceRecognizer>(stdFacerecAlgorithmName);
	} catch (Exception& e) {
		LOGD_ERR(e.what());
	}
	if (model.empty()) {
		cerr << "ERROR: The FaceRecognizer algorithm ["
				<< stdFacerecAlgorithmName
				<< "] is not available in your version of OpenCV. Please update to OpenCV v2.4.1 or newer."
				<< endl;
		LOGD("ERROR: The FaceRecognizer algorithm [");
		exit(1);
	}
	return model;

}
JNIEXPORT void JNICALL Java_com_ylwang_ifacerecognition_MyFaceDetector_nativeCreateModel(
		JNIEnv * jenv, jclass, jlong thiz, jstring facerecAlgorithm,
		jstring mModelName) {
	LOGD(
			"Java_com_ylwang_ifacerecognition_MyFaceDetector_nativeCreateModel Enter");

	const char* jnamestr_algo = jenv->GetStringUTFChars(facerecAlgorithm, NULL);
	const char* jnamestr_model = jenv->GetStringUTFChars(mModelName, NULL);
	string stdFacerecAlgorithmName(jnamestr_algo);
	string stdModelName(jnamestr_model);
	Ptr<FaceRecognizer> model;
	model = CreateModel(stdFacerecAlgorithmName);
	model->save(stdModelName);
	return;

}
JNIEXPORT void JNICALL Java_com_ylwang_ifacerecognition_MyFaceDetector_nativelearnCollectedFaces(
		JNIEnv * jenv, jclass, jlong, jstring mModelPath,
		jstring facerecAlgorithm, jlong preprocessedFaces, jlong faceLabels,
		jint cnt) {
	LOGD(
			"Java_com_ylwang_ifacerecognition_MyFaceDetector_nativelearnCollectedFaces Enters");
	const char* jnamestr_algo = jenv->GetStringUTFChars(facerecAlgorithm, NULL);
	const char* jnamestr_model = jenv->GetStringUTFChars(mModelPath, NULL);
	string stdFacerecAlgorithmName(jnamestr_algo);
	string stdModelName(jnamestr_model);
	Ptr<FaceRecognizer> model;
	vector<Mat> facesVect;
	Mat facesMat = *((Mat*) preprocessedFaces);
	Mat labelsMat = *((Mat*) faceLabels);
	Mat_to_vector_Mat(facesMat, facesVect, cnt);
	model = CreateModel(stdFacerecAlgorithmName);
	model->load(stdModelName);
	try {
		model->train(facesVect, labelsMat);
		model->save(stdModelName);
	} catch (Exception& e) {
		LOGD_Train_ERR(e.what());
	}

	LOGD(
			"Java_com_ylwang_ifacerecognition_MyFaceDetector_nativelearnCollectedFaces Exit");

}
JNIEXPORT void JNICALL Java_com_ylwang_ifacerecognition_MyFaceDetector_nativeUpdate(
		JNIEnv * jenv, jclass, jlong, jstring mModelPath,
		jstring facerecAlgorithm, jlong preprocessedFaces, jlong faceLabels,
		jint cnt) {
	LOGD(
			"Java_com_ylwang_ifacerecognition_MyFaceDetector_nativeUpdate Enters");
	const char* jnamestr_algo = jenv->GetStringUTFChars(facerecAlgorithm, NULL);
	const char* jnamestr_model = jenv->GetStringUTFChars(mModelPath, NULL);
	string stdFacerecAlgorithmName(jnamestr_algo);
	string stdModelName(jnamestr_model);
	Ptr<FaceRecognizer> model;
	vector<Mat> facesVect;
	Mat facesMat = *((Mat*) preprocessedFaces);
	Mat labelsMat = *((Mat*) faceLabels);
	Mat_to_vector_Mat(facesMat, facesVect, cnt);
	model = CreateModel(stdFacerecAlgorithmName);
	model->load(stdModelName);
	try {
		model->update(facesVect, labelsMat);
		model->save(stdModelName);
	} catch (Exception& e) {
		LOGD_Train_ERR(e.what());
	}
	LOGD( "Java_com_ylwang_ifacerecognition_MyFaceDetector_nativeUpdate Exit");

}
JNIEXPORT jint JNICALL Java_com_ylwang_ifacerecognition_MyFaceDetector_nativePredict(
		JNIEnv * jenv, jclass, jlong, jstring mModelPath,
		jstring facerecAlgorithm, jlong faceImg) {
	LOGD(
			"Java_com_ylwang_ifacerecognition_MyFaceDetector_nativePredict Enters");
	const char* jnamestr_algo = jenv->GetStringUTFChars(facerecAlgorithm, NULL);
	const char* jnamestr_model = jenv->GetStringUTFChars(mModelPath, NULL);
	string stdFacerecAlgorithmName(jnamestr_algo);
	string stdModelName(jnamestr_model);
	Ptr<FaceRecognizer> model;
	model = CreateModel(stdFacerecAlgorithmName);
	model->load(stdModelName);
	int label = 0;
	double confiden = 0;
	Mat inputImg = *((Mat*) faceImg);
	vector<Mat> inVec;
	inVec.push_back(inputImg);
	char mCol[10];
	char mRow[10];
	sprintf(mCol, "%d", inputImg.cols);
	sprintf(mRow, "%d", inputImg.rows);
	LOGD_Train_ERR(mCol);
	LOGD_Train_ERR(mRow);

	try {
		//model->predict(*((Mat*)faceImg),&label,&confiden);
		model->predict(inputImg, label, confiden);
		//label=model->predict(*((Mat*)faceImg));
		//model->save(stdModelName);
	} catch (Exception& e) {
		LOGD_Train_ERR(e.what());
	}
	return (jint) label;
	LOGD( "Java_com_ylwang_ifacerecognition_MyFaceDetector_nativePredict Exit");

}

Rect FindPupilRect(Mat src, double threshod, double ratio, Rect* res) {
	Mat src_gray;
	cvtColor(src, src_gray, CV_BGR2GRAY);
	double minVal = 0;
	minMaxLoc(src_gray, &minVal, NULL, NULL, NULL);
	Mat tmp;
	threshold(src_gray, tmp, minVal + threshod, 255, THRESH_BINARY_INV);
//(Optional) remove noise (small areas of white pixels)

//     Mat element = getStructuringElement(MORPH_ELLIPSE, Size(3, 3), Point(1, 1));
//     erode(tmp, tmp, element);
//     dilate(tmp, tmp, element);

	vector<Vec4i> hierarchy;
	vector < vector<Point2i> > contours;
	findContours(tmp, contours, hierarchy, CV_RETR_LIST,
			CV_CHAIN_APPROX_SIMPLE);
	cout << contours.size() << endl;

	int maxArea = 0;
	Rect maxContourRect;
	int maxArea_s = 0;
	Rect maxContourRect_s;
	for (int i = 0; i < contours.size(); i++) {

		int area = contourArea(contours[i]);
		Rect rect = boundingRect(contours[i]);
		cout << rect.width << " " << rect.height << " " << area << endl;

		double squareKoef = ((double) rect.width) / rect.height;

		if (squareKoef < ratio && squareKoef > 1.0 / ratio) {
			//	circle(src,Point((rect.tl().x+rect.br().x)*0.5,(rect.tl().y+rect.br().y)*0.5),rect.width,Scalar(123),1);
			if (area > maxArea) {
				maxArea = area;
				maxContourRect = rect;
			} else if (area > maxArea_s) {
				maxArea_s = area;
				maxContourRect_s = rect;
			}

		}

	}

	circle(src,
			Point((maxContourRect.tl().x + maxContourRect.br().x) * 0.5,
					(maxContourRect.tl().y + maxContourRect.br().y) * 0.5), 1,
			Scalar(123), 2);
	circle(src,
			Point((maxContourRect_s.tl().x + maxContourRect_s.br().x) * 0.5,
					(maxContourRect_s.tl().y + maxContourRect_s.br().y) * 0.5),
			1, Scalar(123), 2);
//imshow("Processing",src);
	return maxContourRect;
}

void eclipseMask(Mat faceImg, Mat outImg) {
	LOGD_ERR("EclipseMask Enter");
	Mat filtered = Mat(faceImg.size(), CV_8U);
//Mat outImg=Mat(faceImg.size(), CV_8U);
	bilateralFilter(faceImg, filtered, 0, 20.0, 2.0);
	Mat mask = Mat(faceImg.size(), CV_8U, Scalar(0));
	double dw = faceImg.cols;
	double dh = faceImg.rows;
	Point faceCenter = Point(cvRound(dw * 0.5), cvRound(dh * 0.48));
	Size size = Size(cvRound(dw * 0.38), cvRound(dh * 0.45));
	ellipse(mask, faceCenter, size, 0, 0, 360, Scalar(255), CV_FILLED);

//Mat dstImg = Mat(mask.size(), CV_8U, Scalar(128));
	filtered.copyTo(outImg, mask);
	LOGD_ERR("EclipseMask Exit");
//return outImg;
}
bool my_cmp(const vector<Point2i> &a, const vector<Point2i> &b) {
	Rect recta = boundingRect(a);
	Rect rectb = boundingRect(b);
	return (recta.tl().y + recta.br().y) > (rectb.tl().y + rectb.br().y);
}

void findFeaturePoint(Mat src, vector<Point>& point, vector<Rect>& rect_res,
		int FLAG) {
	LOGD_ERR("findFeaturePoint Enter");
	char name[10];
	sprintf(name, "Step %d", FLAG);
	Mat src_mask = Mat(src.size(), CV_8U);
	Mat thresholdRes = Mat(src.size(), CV_8U);
	Mat thresholdResCopy = Mat(src.size(), CV_8U);
	eclipseMask(src, src_mask);
	if (FLAG)
		equalizeLeftAndRightHalves(src_mask);

//imshow("After Equalize", src_mask);
	double minVal = 0;
	double maxVal = 0;

	minMaxLoc(src_mask, &minVal, &maxVal, NULL, NULL);
//threshold(src, threRes, maxVal-10, 255, THRESH_BINARY);
	threshold(src_mask, thresholdRes, minVal + 7.5, 255, THRESH_BINARY);
	thresholdRes.copyTo(thresholdResCopy);
//LOGD_ERR("Threshold Over");
//imshow(name, thresholdRes);
	vector<Vec4i> hierarchy;
	vector < vector<Point2i> > contours;
	findContours(thresholdResCopy.rowRange(0, thresholdRes.rows / 2), contours,
			hierarchy, CV_RETR_LIST, CV_CHAIN_APPROX_SIMPLE);
//LOGD_ERR("findContours Over");
	sort(contours.begin(), contours.end(), my_cmp);
//LOGD_ERR("sort Over");
//cout << contours.size() << endl;
//LOGD_ERR(contours.size());
	Rect rect;
	if (contours.size() < 2)
		return;
	switch (FLAG) {
	case 0:
		for (int i = 0; i < 2; i++) {

			rect = boundingRect(contours[i]);

			point.push_back(
					Point((rect.tl().x + rect.br().x) * 0.5,
							(rect.tl().y + rect.br().y) * 0.5));
			rect_res.push_back(rect);
			//rectangle(src, rect, M_RED);

		}
		break;
	case 1:
		Mat subThreshold;
		int MAX_RIGHT_I = 0, MAX_RIGHT_J = 0;
		int MAX_LEFT_I = 1000, MAX_LEFT_J = 1000;
		float curV;
		for (int k = 0; k < 2; k++) {

			rect = boundingRect(contours[k]);
			subThreshold =
					thresholdRes.colRange(rect.tl().x, rect.br().x).rowRange(
							rect.tl().y, rect.br().y);
			//imshow("SSS",subThreshold);
			for (int i = 0; i < subThreshold.size().height; i++)
				for (int j = 0; j < subThreshold.size().width; j++) {

					try {
						curV = subThreshold.data[i * subThreshold.size().width
								* subThreshold.channels() + j];
					} catch (cv::Exception& e) {
						cout << e.what() << endl;
					}

					if (curV <= 10) {
						if (j >= MAX_RIGHT_J) {

							MAX_RIGHT_I = i;
							MAX_RIGHT_J = j;
						}

						if (j <= MAX_LEFT_J) {

							MAX_LEFT_I = i;
							MAX_LEFT_J = j;

						}
					}

				}
			point.push_back(
					Point(MAX_LEFT_J + rect.tl().x, MAX_LEFT_I + rect.tl().y));
			point.push_back(
					Point(MAX_RIGHT_J + rect.tl().x,
							MAX_RIGHT_I + rect.tl().y));
			rect_res.push_back(rect);
			//rectangle(src, rect, M_RED);
		}
		break;
	}
	LOGD_ERR("findFeaturePoint Exit");
}
int refineEyeContour(Mat face, Rect eyeContour, Point center) {

	int left = 0, right = 0, lmaxGradient = 0, rmaxGradient = 0;
	int y = center.y, x = center.x;
	try {

		for (int j = center.x; j < eyeContour.br().x; j++) {

			if ((face.at<uchar>(y, j) - face.at<uchar>(y, j - 1))
					> rmaxGradient) {
				rmaxGradient =
						(face.at<uchar>(y, j) - face.at<uchar>(y, j - 1));
				right = j;

			}
		}
		for (int j = center.x; j > eyeContour.tl().x; j--) {
			if ((face.at<uchar>(y, j) - face.at<uchar>(y, j + 1))
					> lmaxGradient) {
				lmaxGradient =
						(face.at<uchar>(y, j) - face.at<uchar>(y, j + 1));
				left = j;

			}
		}
	} catch (cv::Exception& e) {
		//cout<<e.what()<<endl;
		LOGD_ERR(e.what());

	}

//	imshow("Eye",eyeRegio);

	if (right && left) {

		return min(x - left, right - x);
	} else if (left) {
		return x - left;
	} else if (right) {
		return right - x;
	} else {
		return 0;
	}
}
void eyeFeatureExtraction(Mat src) {
	Mat src_threshold = Mat(src.size(), CV_8U);
	Mat src_gray;
	vector<Point> irisCenter;
	vector<Point> cornerPoint;
	vector<Rect> eyeContour;
	vector<Rect> irisContour;
	medianBlur(src, src, 3);

	cvtColor(src, src_gray, CV_BGR2GRAY);
	GaussianBlur(src_gray, src_gray, Size(9, 9), 2, 2);
	findFeaturePoint(src_gray, irisCenter, irisContour, 0);
	findFeaturePoint(src_gray, cornerPoint, eyeContour, 1);
	for (int i = 0; i < irisCenter.size(); i++) {
		/*		int r=2;
		 if(irisContour.size()>=irisCenter.size())
		 r=refineEyeContour(src_gray,eyeContour[i],irisCenter[i]);*/
		circle(src, irisCenter[i], 2, M_RED, -1, 8, 0);
		//circle(src, irisCenter[i], r, M_RED, -1, 8, 0);

	}
	for (int i = 0; i < cornerPoint.size(); i++) {
		circle(src, cornerPoint[i], 2, M_GREEN, -1, 8, 0);

	}
	/*	for (int i = 0; i < irisContour.size(); i++) {
	 cv::rectangle(src,irisContour[i],M_RED);

	 }*/

}
JNIEXPORT void JNICALL Java_com_ylwang_ifacerecognition_MyFaceDetector_nativeFindIrisCenter(
		JNIEnv * jenv, jclass, jlong thiz, jlong faceImg, jdouble threshold,
		jdouble ratio) {

	try {
		eyeFeatureExtraction(*((Mat*) faceImg));
	} catch (cv::Exception& e) {
		LOGD_ERR(e.what());

	}

}
JNIEXPORT jlong JNICALL Java_com_ylwang_ifacerecognition_MyFaceRecognizer_nativeCreativeModel(
		JNIEnv *jenv, jobject, jstring Algorithm) {
	const char* jnamestr = jenv->GetStringUTFChars(Algorithm, NULL);
	string stdAlgorithmName(jnamestr);
	jlong result = 0;

	Ptr<FaceRecognizer> model;
	if (stdAlgorithmName == "FaceRecognizer::Eigenfaces") {
		LOGD("EigenFace Recognizer Created.");
		model = cv::createEigenFaceRecognizer();
	} else if (stdAlgorithmName == "FaceRecognizer::Fisherfaces") {
		LOGD("FisherFace Recognizer Created.");
		model = cv::createFisherFaceRecognizer();
	}
	FaceRecognizer* pFRc = model.obj;
	model.addref();
	result = (jlong) pFRc;
	return result;

}
JNIEXPORT jdouble JNICALL Java_com_ylwang_ifacerecognition_MyFaceRecognizer_nativeGetSimilarity(
		JNIEnv *, jobject, jlong matA, jlong matB) {
	LOGD(
			"Java_com_ylwang_ifacerecognition_MyFaceRecognizer_nativeGetSimilarity enter");

	return (jdouble) getSimilarity(*((Mat *) matA), *((Mat *) matB));

	LOGD(
			"Java_com_ylwang_ifacerecognition_MyFaceRecognizer_nativeGetSimilarity exit");

}
