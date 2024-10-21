package com.ideacarry.utils

import kotlin.math.roundToLong

/**
 * Created by Raining on 2023/6/20
 *
 * 时间转换工具类
 */

fun Long.msToUs() = this * 1000

fun Long.usToMs() = (this / 1000.0).roundToLong()

fun Long.msToS() = this / 1000.0