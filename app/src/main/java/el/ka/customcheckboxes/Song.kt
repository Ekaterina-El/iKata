package el.ka.customcheckboxes

import android.content.ContentUris
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import java.util.concurrent.TimeUnit

data class Song(
    val id: Long,
    val name: String,
    val artists: String,
    val duration: Int,
    val uri: Uri
)

fun getSongs(): MutableList<Song> {
    val songList = mutableListOf<Song>()

    val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
    } else {
        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
    }

    val projection = arrayOf(
        MediaStore.Audio.Media._ID,
        MediaStore.Audio.Media.ARTIST,
        MediaStore.Audio.Media.DISPLAY_NAME,
        MediaStore.Audio.Media.DURATION,
    )

    // Show only imagers that are at least 1 minute in duration
    val selection = "${MediaStore.Audio.Media.DURATION} >= ?"
    val selectionArg = arrayOf(
        TimeUnit.MICROSECONDS.convert(1, TimeUnit.MINUTES).toString()
    )

    // In alphabetical
    val sortOrder = "${MediaStore.Audio.Media.DISPLAY_NAME} ASC"

    val query = MAIN_ACTIVITY.contentResolver.query(
        collection,
        projection,
        null,
        null,
        null
    )

    query?.use { cursor ->
        val idIdx = cursor.getColumnIndex(MediaStore.Audio.Media._ID)
        val artistsIdx = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)
        val displayNameIdx = cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME)
        val durationIdx = cursor.getColumnIndex(MediaStore.Audio.Media.DURATION)

        while (cursor.moveToNext()) {
            val id = cursor.getLong(idIdx)
            val artists = cursor.getString(artistsIdx)
            val displayName = cursor.getString(displayNameIdx)
            val duration = cursor.getInt(durationIdx)

            val contentUri: Uri = ContentUris.withAppendedId(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                id
            )

            songList += Song(
                id, displayName.removeSuffix(".mp3"), artists, duration, contentUri
            )
        }

    }

    return songList

}