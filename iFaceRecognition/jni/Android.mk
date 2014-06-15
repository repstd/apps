LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
#OPENCV_LIB_TYPE:=SHARED
include ../OpenCVSDK/native/jni/OpenCV.mk
LOCAL_MODULE    := iFaceRecognition
LOCAL_LDLIBS     += -llog -ldl
### Add all source file names to be included in lib separated by a whitespace
LOCAL_SRC_FILES := iFaceRecognition.cpp RecogLib/ImageUtils_0.7.cpp RecogLib/recognition.cpp

include $(BUILD_SHARED_LIBRARY)
