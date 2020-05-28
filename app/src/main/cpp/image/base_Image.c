//
// Created by Raining on 2015/8/12.
//

#include <stdlib.h>
#include "base_Image.h"

base_Image* base_CreateImage(int width, int height, int bytesPerPixel)
{
	base_Image* image = (base_Image*) malloc(sizeof(base_Image));
	image->m_width = width;
	image->m_height = height;
	image->m_bpp = bytesPerPixel;
	image->m_pDatas = (uint8_t*) malloc(width * height * bytesPerPixel);

	return image;
}

void base_DestroyImage(base_Image* image)
{
	if(image->m_pDatas != 0)
	{
		free(image->m_pDatas);
		image->m_pDatas = 0;
	}
}

uint8_t* base_GetPixelAddress(base_Image* image, int x, int y)
{
	return image->m_pDatas + (y * image->m_width + x) * image->m_bpp;
}
