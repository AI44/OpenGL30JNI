//
// Created by Raining on 2019/8/21.
//

#include "base_Log.h"

void printMatrix4x4(const float *mat) {
    base_LOG("matrix:\n"
             "[%f\t%f\t%f\t%f\n"
             " %f\t%f\t%f\t%f\n"
             " %f\t%f\t%f\t%f\n"
             " %f\t%f\t%f\t%f]\n",
             mat[0], mat[1], mat[2], mat[3],
             mat[4], mat[5], mat[6], mat[7],
             mat[8], mat[9], mat[10], mat[11],
             mat[12], mat[13], mat[14], mat[15]);
}

void printMatrix3x3(const float *mat) {
    base_LOG("matrix:\n"
             "[%f\t%f\t%f\n"
             " %f\t%f\t%f\n"
             " %f\t%f\t%f]\n",
             mat[0], mat[1], mat[2],
             mat[3], mat[4], mat[5],
             mat[6], mat[7], mat[8]);
}