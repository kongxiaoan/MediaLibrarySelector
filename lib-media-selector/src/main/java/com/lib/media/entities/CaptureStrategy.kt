package com.lib.media.entities

/**
 * 拍照参数配置
 * @author: kpa
 * @date: 2023/4/24
 * @description: 媒体选择器
 */
class CaptureStrategy @JvmOverloads constructor(
    val isPublic: Boolean,
    val authority: String,
    val directory: String? = null,
)
