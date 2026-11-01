package com.example.domain.model

data class SftpFile(
    val name: String,
    val path: String,
    val isDirectory: Boolean,
    val sizeBytes: Long = 0L,
    val permissions: String = "rw-r--r--",
    val lastModified: Long = System.currentTimeMillis(),
    val isRemote: Boolean = false
) {
    val humanSize: String
        get() {
            if (isDirectory) return "Folder"
            val kb = sizeBytes / 1024.0
            val mb = kb / 1024.0
            return when {
                mb >= 1.0 -> String.format(java.util.Locale.US, "%.1f MB", mb)
                kb >= 1.0 -> String.format(java.util.Locale.US, "%.1f KB", kb)
                else -> "$sizeBytes B"
            }
        }
}
