package com.zii.sdker.model

data class Copy2Result(
    val `data`: Data,
    val msg: String,
    val status: Int
)

data class Data(
    val tag: String,
    val url: String
)