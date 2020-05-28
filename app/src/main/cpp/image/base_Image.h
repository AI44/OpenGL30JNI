//
// Created by Raining on 2015/8/12.
//

#ifndef _BASE_IMAGE
#define _BASE_IMAGE

#ifdef __cplusplus
extern "C"
{
#endif

#include <stdio.h>

typedef enum
{
	base_RGBA_8888 = 0x0001, //字节序R->G->B->A
	base_RGB_888 = 0x0002, //字节序R->G->B
	base_RGB_565 = 0x0004, //字节序GB->RG
	base_RGBA_4444 = 0x0008 //字节序BA->RG
} base_Config;

typedef struct base_ImageStr
{
	int m_width;
	int m_height;
	int m_bpp; //bytes per pixel
	base_Config m_config;
	uint8_t* m_pDatas; //rgba
} base_Image;

base_Image* base_CreateImage(int width, int height, int bytesPerPixel);
void base_DestroyImage(base_Image* image);
uint8_t* base_GetPixelAddress(base_Image* image, int x, int y);

#ifdef __cplusplus
}
#endif

#endif
