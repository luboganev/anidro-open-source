package app.anidro.models

import android.os.Build

enum class FileType {
    NONE,
    IMAGE,
    GIF,
    VIDEO;

    companion object {

        @JvmStatic
        val availableFileTypes: List<FileType> = if (Build.VERSION.SDK_INT < 18) {
            listOf(IMAGE, GIF)
        } else {
            listOf(IMAGE, GIF, VIDEO)
        }
    }
}