# This is a generated file. Please do not edit.

.PHONY: all

COMMANDS := 	\
	    scd_cmd_1

all: $(COMMANDS)

scd_cmd_1:
	@echo begin generating scanner info for $@
	D:/MyDocument/CS_Related/android/android-ndk-r8e-windows-x86/android-ndk-r8e/toolchains/arm-linux-androideabi-4.6/prebuilt/windows/bin/arm-linux-androideabi-g++ -march=armv5te -mtune=xscale -msoft-float -fno-exceptions -mthumb -Os -D 'NDEBUG=1' -finline-limit=64 -D 'ANDROID=1' -D 'ANDROID=1' -fsigned-char -fexceptions -I "D:\MyProject\GitHub\apps\OpenCVSDK\native\jni\include\opencv" -I "D:\MyProject\GitHub\apps\OpenCVSDK\native\jni\include" -I "D:\MyDocument\CS_Related\android\android-ndk-r8e-windows-x86\android-ndk-r8e\sources\cxx-stl\gnu-libstdc++\4.6\include" -I "D:\MyDocument\CS_Related\android\android-ndk-r8e-windows-x86\android-ndk-r8e\sources\cxx-stl\gnu-libstdc++\4.6\libs\armeabi\include" -I "D:\MyProject\GitHub\apps\iFaceRecognition_v.2.0\iFaceRecognition\jni" -I "D:\MyDocument\CS_Related\android\android-ndk-r8e-windows-x86\android-ndk-r8e\platforms\android-8\arch-arm\usr\include" -E -P -v -dD specs.cpp
	@echo end generating scanner info for $@


