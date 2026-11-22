/*
 * ArchiveTune (2026)
 * © Rukamori — github.com/rukamori
 * GPL-3.0 License | Contributors: see git history
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package moe.rukamori.archivetune.innertube

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class YouTubeTimedLyricsTest {
    @Test
    fun `extractAndroidMusicLyrics parses timedLyricsData into LRC lines`() {
        val response =
            Json
                .parseToJsonElement(
                    """
                    {
                      "contents": {
                        "elementRenderer": {
                          "newElement": {
                            "type": {
                              "componentType": {
                                "model": {
                                  "timedLyricsModel": {
                                    "lyricsData": {
                                      "timedLyricsData": [
                                        {
                                          "lyricLine": "♪",
                                          "cueRange": {
                                            "startTimeMilliseconds": "0",
                                            "endTimeMilliseconds": "13520"
                                          }
                                        },
                                        {
                                          "lyricLine": "Yeah",
                                          "cueRange": {
                                            "startTimeMilliseconds": "13520",
                                            "endTimeMilliseconds": "16240"
                                          }
                                        },
                                        {
                                          "lyricLine": "   "
                                        },
                                        {
                                          "lyricLine": "I've been\ntryna call",
                                          "cueRange": {
                                            "startTimeMilliseconds": "125430",
                                            "endTimeMilliseconds": "129000"
                                          }
                                        }
                                      ]
                                    }
                                  }
                                }
                              }
                            }
                          }
                        }
                      }
                    }
                    """.trimIndent(),
                ).jsonObject

        val actual = YouTube.extractAndroidMusicLyrics(response)

        assertEquals(
            "[00:00.000]♪\n[00:13.520]Yeah\n[02:05.430]I've been tryna call",
            actual,
        )
    }

    @Test
    fun `extractAndroidMusicLyrics reuses plain lines when timedLyricsData has no cueRange`() {
        val response =
            Json
                .parseToJsonElement(
                    """
                    {
                      "timedLyricsModel": {
                        "lyricsData": {
                          "timedLyricsData": [
                            { "lyricLine": "Yeah" },
                            { "lyricLine": "I've been tryna call" }
                          ]
                        }
                      }
                    }
                    """.trimIndent(),
                ).jsonObject

        assertEquals(
            "Yeah\nI've been tryna call",
            YouTube.extractAndroidMusicLyrics(response),
        )
    }

    @Test
    fun `extractAndroidMusicLyrics falls back to plain lines when startTimeMilliseconds is missing or zero`() {
        val missingStartResponse =
            Json
                .parseToJsonElement(
                    """
                    {
                      "timedLyricsData": [
                        {
                          "lyricLine": "Line 1",
                          "cueRange": {
                            "endTimeMilliseconds": "5000"
                          }
                        },
                        {
                          "lyricLine": "Line 2",
                          "cueRange": {
                            "endTimeMilliseconds": "10000"
                          }
                        }
                      ]
                    }
                    """.trimIndent(),
                ).jsonObject

        val allZeroResponse =
            Json
                .parseToJsonElement(
                    """
                    {
                      "timedLyricsData": [
                        {
                          "lyricLine": "Line 1",
                          "cueRange": {
                            "startTimeMilliseconds": "0",
                            "endTimeMilliseconds": "5000"
                          }
                        }
                      ]
                    }
                    """.trimIndent(),
                ).jsonObject

        assertEquals("Line 1\nLine 2", YouTube.extractAndroidMusicLyrics(missingStartResponse))
        assertEquals("Line 1", YouTube.extractAndroidMusicLyrics(allZeroResponse))
    }

    @Test
    fun `extractAndroidMusicLyrics treats partial cues as plain text instead of dropping lines`() {
        val response =
            Json
                .parseToJsonElement(
                    """
                    {
                      "timedLyricsData": [
                        {
                          "lyricLine": "First line",
                          "cueRange": {
                            "startTimeMilliseconds": "1500",
                            "endTimeMilliseconds": "3000"
                          }
                        },
                        {
                          "lyricLine": "Untimed middle line"
                        },
                        {
                          "lyricLine": "Third line",
                          "cueRange": {
                            "startTimeMilliseconds": "5000",
                            "endTimeMilliseconds": "8000"
                          }
                        }
                      ]
                    }
                    """.trimIndent(),
                ).jsonObject

        assertEquals(
            "First line\nUntimed middle line\nThird line",
            YouTube.extractAndroidMusicLyrics(response),
        )
    }

    @Test
    fun `extractAndroidMusicLyrics returns null when timedLyricsData is absent`() {
        val response =
            Json
                .parseToJsonElement(
                    """
                    {
                      "contents": {
                        "sectionListRenderer": {
                          "contents": []
                        }
                      }
                    }
                    """.trimIndent(),
                ).jsonObject

        assertNull(YouTube.extractAndroidMusicLyrics(response))
    }
}
