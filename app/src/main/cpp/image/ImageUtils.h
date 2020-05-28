/*
 * ImageUtils.h
 *
 *  Created on: 2013-3-10
 *      Author: Raining
 */

#ifndef IMAGEUTILS_H_
#define IMAGEUTILS_H_

#include <jni.h>

#include "base_Image.h"

#define IMAGE_UTILS_SUCCESS 0x0000
#define IMAGE_UTILS_BMP_IS_NULL 0x0001
#define IMAGE_UTILS_OTHER_ERROR 0x0002
#define IMAGE_UTILS_BMP_TYPE_ERROR 0x0004

class Image8888: public base_ImageStr
{
public:
	Image8888();
	~Image8888();
	int SetImage(JNIEnv* env, jobject bmp);
	void ClearAll();

private:
	JNIEnv* m_env;
	jobject m_bmp;
};

class StringPtr
{
public:
	char const* m_pStr;

	StringPtr();
	~StringPtr();
	int SetString(JNIEnv* env, jstring str);
	void ClearAll();

private:
	JNIEnv* m_env;
	jstring m_str;
};

#endif /* IMAGEUTILS_H_ */
