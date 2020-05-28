/*
 * ImageUtils.cpp
 *
 *  Created on: 2013-3-10
 *      Author: Raining
 */

#include "ImageUtils.h"

#include <android/bitmap.h>
#include <android/log.h>
#include <stddef.h>

#include "base_Log.h"

Image8888::Image8888() :
		m_env(0), m_bmp(0)
{
	m_pDatas = 0;
}

Image8888::~Image8888()
{
	ClearAll();
}

void Image8888::ClearAll()
{
	if(m_env && m_bmp && m_pDatas)
	{
		AndroidBitmap_unlockPixels(m_env, m_bmp);
	}
	m_env = 0;
	m_bmp = 0;
	m_pDatas = 0;
	m_bpp = 0;
	m_width = 0;
	m_height = 0;
}

int Image8888::SetImage(JNIEnv* env, jobject bmp)
{
	int re = IMAGE_UTILS_OTHER_ERROR;

	if(env != 0 && bmp != 0)
	{
		m_env = env;
		m_bmp = bmp;

		AndroidBitmapInfo info;
		if((re = AndroidBitmap_getInfo(m_env, m_bmp, &info)) < 0)
		{
			base_LOG("AndroidBitmap_getInfo() failed ! error=%d", re);
			re = IMAGE_UTILS_OTHER_ERROR;
			goto OK;
		}
		if(info.format != ANDROID_BITMAP_FORMAT_RGBA_8888)
		{
			re = IMAGE_UTILS_BMP_TYPE_ERROR;
			goto OK;
		}
		m_config = base_RGBA_8888;
		m_bpp = 4;
		m_width = (int)info.width;
		m_height = (int)info.height;
		if((re = AndroidBitmap_lockPixels(m_env, m_bmp, (void**)&m_pDatas)) < 0)
		{
			base_LOG("AndroidBitmap_lockPixels() failed ! error=%d", re);
			re = IMAGE_UTILS_OTHER_ERROR;
			goto OK;
		}
		if(0 == m_pDatas)
		{
			re = IMAGE_UTILS_OTHER_ERROR;
			goto OK;
		}

		re = IMAGE_UTILS_SUCCESS;
	}

	OK: if(re != IMAGE_UTILS_SUCCESS)
	{
		ClearAll();
	}

	return re;
}

StringPtr::StringPtr() :
		m_env(0), m_str(0)
{
	m_pStr = NULL;
}

StringPtr::~StringPtr()
{
	ClearAll();
}

void StringPtr::ClearAll()
{
	if(m_env && m_str && m_pStr)
	{
		m_env->ReleaseStringUTFChars(m_str, m_pStr);
	}
	m_env = 0;
	m_str = 0;
	m_pStr = 0;
}

int StringPtr::SetString(JNIEnv* env, jstring str)
{
	int re = IMAGE_UTILS_OTHER_ERROR;

	if(env && str)
	{
		m_env = env;
		m_str = str;

		m_pStr = env->GetStringUTFChars(str, 0);
		if(!m_pStr)
		{
			re = IMAGE_UTILS_OTHER_ERROR;
			goto OK;
		}
		re = IMAGE_UTILS_SUCCESS;
	}

	OK: if(re != IMAGE_UTILS_SUCCESS)
	{
		ClearAll();
	}

	return re;
}
