package com.example.fazarproject2.util

import android.content.ContentUris
import android.content.Context
import android.provider.MediaStore
import com.example.fazarproject2.domain.model.AudioFile
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class MediaScanner @Inject constructor(
    @field:ApplicationContext private val context: Context
) {
    fun scanAudioFiles(): List<AudioFile> {
        val audioFiles = mutableListOf<AudioFile>()
        val collection = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        }

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.DURATION
        )

        // Filter criteria: Only fetch audio files where DURATION < 60000 (60 seconds)
        val selection = "${MediaStore.Audio.Media.DURATION} < ?"
        val selectionArgs = arrayOf("60000")
        val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"

        context.contentResolver.query(
            collection,
            projection,
            selection,
            selectionArgs,
            sortOrder
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val title = cursor.getString(titleColumn)
                val duration = cursor.getLong(durationColumn)
                val contentUri = ContentUris.withAppendedId(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    id
                )

                audioFiles.add(AudioFile(contentUri.toString(), title, duration))
            }
        }
        return audioFiles
    }
}
