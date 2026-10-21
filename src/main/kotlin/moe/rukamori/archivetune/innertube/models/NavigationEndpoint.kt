/*
 * ArchiveTune (2026)
 * © Rukamori — github.com/rukamori
 * GPL-3.0 License | Contributors: see git history
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package moe.rukamori.archivetune.innertube.models

import kotlinx.serialization.Serializable

@Serializable
data class NavigationEndpoint(
    val watchEndpoint: WatchEndpoint? = null,
    val watchPlaylistEndpoint: WatchEndpoint? = null,
    val browseEndpoint: BrowseEndpoint? = null,
    val searchEndpoint: SearchEndpoint? = null,
    val queueAddEndpoint: QueueAddEndpoint? = null,
    val shareEntityEndpoint: ShareEntityEndpoint? = null,
    val queueUpdateCommand: QueueUpdateCommand? = null,
) {
    @Serializable
    data class QueueUpdateCommand(
        val fetchContentsCommand: NavigationEndpoint? = null,
    )

    val queueWatchEndpoint: WatchEndpoint?
        get() = queueUpdateCommand?.fetchContentsCommand?.anyWatchEndpoint ?: anyWatchEndpoint

    val endpoint: Endpoint?
        get() =
            watchEndpoint
                ?: watchPlaylistEndpoint
                ?: browseEndpoint
                ?: searchEndpoint
                ?: queueAddEndpoint
                ?: shareEntityEndpoint

    val anyWatchEndpoint: WatchEndpoint?
        get() =
            watchEndpoint
                ?: watchPlaylistEndpoint
}
