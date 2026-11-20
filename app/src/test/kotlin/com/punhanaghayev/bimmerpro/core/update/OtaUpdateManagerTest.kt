package com.punhanaghayev.bimmerpro.core.update

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OtaUpdateManagerTest {

    @Test
    fun `semver comparator accurately detects newer versions`() {
        assertTrue(SemVerComparator.isNewer("v1.0.1", "1.0.0"))
        assertTrue(SemVerComparator.isNewer("1.2.0", "1.1.9"))
        assertFalse(SemVerComparator.isNewer("v1.0.0", "1.0.0"))
        assertFalse(SemVerComparator.isNewer("0.9.5", "1.0.0"))
    }

    @Test
    fun `parseReleases extracts non-draft releases and apk asset url`() {
        val sampleJson = """
            [
              {
                "tag_name": "v1.1.0",
                "name": "BimmerPro v1.1.0",
                "body": "- Added G20 MGU support",
                "published_at": "2026-11-18T16:00:00Z",
                "draft": false,
                "assets": [
                  {
                    "name": "app-debug.apk",
                    "browser_download_url": "https://github.com/coder-root/bmw-coding-diagnostic/releases/download/v1.1.0/app-debug.apk",
                    "size": 1234567
                  }
                ]
              },
              {
                "tag_name": "v1.2.0-draft",
                "draft": true,
                "assets": []
              }
            ]
        """.trimIndent()

        val releases = OtaUpdateManager.parseReleases(sampleJson)
        assertEquals(1, releases.size)
        assertEquals("1.1.0", releases.first().versionName)
        assertTrue(Regex("\\d{2}\\.\\d{2}\\.\\d{4}").matches(releases.first().publishedDate))
        assertTrue(releases.first().apkDownloadUrl?.endsWith(".apk") == true)
    }
}
