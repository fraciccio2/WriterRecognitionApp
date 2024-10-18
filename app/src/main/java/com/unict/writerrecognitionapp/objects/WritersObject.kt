package com.unict.writerrecognitionapp.objects

import com.unict.writerrecognitionapp.models.Writer

object WritersObject {
    private var writers: ArrayList<Writer> = arrayListOf()

    fun getWriters(): ArrayList<Writer> {
        return writers
    }

    fun setWriters(newWriters: ArrayList<Writer>) {
        writers = newWriters
    }
}