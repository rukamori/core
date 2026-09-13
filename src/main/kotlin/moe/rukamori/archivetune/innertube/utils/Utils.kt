/*
 * ArchiveTune (2026)
 * © Rukamori — github.com/rukamori
 * GPL-3.0 License | Contributors: see git history
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package moe.rukamori.archivetune.innertube.utils

import kotlinx.coroutines.CancellationException
import moe.rukamori.archivetune.innertube.YouTube
import moe.rukamori.archivetune.innertube.models.distinctByPlaylistEntry
import moe.rukamori.archivetune.innertube.pages.LibraryPage
import moe.rukamori.archivetune.innertube.pages.PlaylistContinuationPage
import moe.rukamori.archivetune.innertube.pages.PlaylistPage
import java.security.MessageDigest

private const val LIBRARY_COMPLETION_MAX_REQUESTS = 500

@JvmName("completedLibrary")
suspend fun Result<PlaylistPage>.completed(): Result<PlaylistPage> =
    runCatching {
        val page = getOrThrow()
        completePlaylistPage(page) { continuation ->
            YouTube.playlistContinuation(continuation, page.playlist.id).getOrThrow()
        }
    }.onFailure { if (it is CancellationException) throw it }

internal suspend fun completePlaylistPage(
    page: PlaylistPage,
    fetchContinuationPage: suspend (String) -> PlaylistContinuationPage?,
): PlaylistPage {
    val songs = page.songs.toMutableList()
    var continuation =
        page.songsContinuation.normalizedContinuation()
            ?: page.continuation.normalizedContinuation()
    val seenContinuations = mutableSetOf<String>()
    var requestCount = 0
    val maxRequests = 500

    while (continuation != null && requestCount < maxRequests) {
        check(seenContinuations.add(continuation)) { "Repeated playlist continuation" }
        requestCount++

        val continuationPage = checkNotNull(fetchContinuationPage(continuation)) {
            "Playlist continuation could not be loaded"
        }
        songs += continuationPage.songs

        continuation = continuationPage.continuation.normalizedContinuation()
    }

    check(continuation == null) { "Playlist continuation limit exceeded" }
    return page.copy(
        songs = songs.distinctByPlaylistEntry(),
        songsContinuation = null,
        continuation = null,
    )
}

@JvmName("completedPlaylist")
suspend fun Result<LibraryPage>.completed(): Result<LibraryPage> =
    runCatching {
        val page = getOrThrow()
        val items = page.items.toMutableList()
        var continuation = page.continuation.normalizedContinuation()
        val seenContinuations = mutableSetOf<String>()
        var requestCount = 0
        val maxRequests = LIBRARY_COMPLETION_MAX_REQUESTS

        while (continuation != null && requestCount < maxRequests) {
            check(seenContinuations.add(continuation)) { "Repeated library continuation" }
            requestCount++
            val continuationPage = YouTube.libraryContinuation(continuation).getOrThrow()
            items += continuationPage.items
            continuation = continuationPage.continuation.normalizedContinuation()
        }
        check(continuation == null) { "Library continuation limit exceeded" }
        LibraryPage(
            items = items.distinctBy { it.id },
            continuation = null,
        )
    }.onFailure { if (it is CancellationException) throw it }

fun ByteArray.toHex(): String = joinToString(separator = "") { eachByte -> "%02x".format(eachByte) }

fun sha1(str: String): String = MessageDigest.getInstance("SHA-1").digest(str.toByteArray()).toHex()

fun parseCookieString(cookie: String): Map<String, String> =
    cookie
        .split(";")
        .map { it.trim() }
        .filter { it.isNotBlank() }
        .mapNotNull { part ->
            val splitIndex = part.indexOf('=')
            if (splitIndex == -1) {
                null
            } else {
                val key = part.substring(0, splitIndex).trim()
                if (key.isEmpty()) null else key to part.substring(splitIndex + 1).trim()
            }
        }.toMap()

fun hasYouTubeLoginCookie(cookie: String?): Boolean = youtubeLoginCookieValue(cookie) != null

fun hasCompleteYouTubeLoginCookies(cookie: String?): Boolean {
    val cookieMap = cookie?.let(::parseCookieString).orEmpty()
    return !cookieMap["LOGIN_INFO"].isNullOrBlank() &&
        YOUTUBE_LOGIN_COOKIE_NAMES.any { cookieName -> !cookieMap[cookieName].isNullOrBlank() }
}

fun youtubeLoginCookieValue(cookie: String?): String? {
    val cookieMap = cookie?.let(::parseCookieString).orEmpty()
    return YOUTUBE_LOGIN_COOKIE_NAMES.firstNotNullOfOrNull { cookieName ->
        cookieMap[cookieName]?.takeIf(String::isNotBlank)
    }
}

private val YOUTUBE_LOGIN_COOKIE_NAMES =
    listOf(
        "SAPISID",
        "__Secure-3PAPISID",
        "__Secure-1PAPISID",
    )

fun String.parseTime(): Int? {
    val normalized =
        buildString(length) {
            for (char in this@parseTime) {
                val digit = Character.digit(char, 10)
                when {
                    digit >= 0 -> append(digit)
                    char.isDurationSeparator() -> append(':')
                    char.isIgnorableDurationChar() -> Unit
                    else -> return null
                }
            }
        }

    val parts = normalized.split(':')
    if (parts.any { it.isBlank() || it.length > 3 }) return null
    if (parts.size !in 2..3) return null
    if (parts.drop(1).any { it.length !in 1..2 }) return null

    val values = parts.map { it.toIntOrNull() ?: return null }
    if (values.drop(1).any { it !in 0..59 }) return null

    return when (values.size) {
        2 -> values[0] * 60 + values[1]
        3 -> values[0] * 3600 + values[1] * 60 + values[2]
        else -> null
    }
}

private fun Char.isDurationSeparator(): Boolean =
    this == ':' ||
        this == '.' ||
        this == ',' ||
        this == '：' ||
        this == '．' ||
        this == '﹕' ||
        this == '꞉' ||
        this == '∶' ||
        this == '٫'

private fun Char.isIgnorableDurationChar(): Boolean =
    isWhitespace() ||
        Character.getType(this) == Character.FORMAT.toInt()

fun isPrivateId(browseId: String): Boolean = browseId.contains("privately")

private fun String?.normalizedContinuation(): String? = this?.takeUnless(String::isBlank)
