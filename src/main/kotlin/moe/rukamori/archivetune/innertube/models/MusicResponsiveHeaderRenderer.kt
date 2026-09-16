/*
 * ArchiveTune (2026)
 * © Rukamori — github.com/rukamori
 * GPL-3.0 License | Contributors: see git history
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package moe.rukamori.archivetune.innertube.models

import kotlinx.serialization.Serializable

@Serializable
data class MusicResponsiveHeaderRenderer(
    val thumbnail: ThumbnailRenderer? = null,
    val buttons: List<Button> = emptyList(),
    val title: Runs,
    val subtitle: Runs,
    val secondSubtitle: Runs? = null,
    val straplineTextOne: Runs? = null,
    val description: Description? = null,
) {
    @Serializable
    data class Description(
        val musicDescriptionShelfRenderer: MusicDescriptionShelfRenderer? = null,
    )

    @Serializable
    data class Button(
        val musicPlayButtonRenderer: MusicPlayButtonRenderer? = null,
        val toggleButtonRenderer: ToggleButtonRenderer? = null,
        val menuRenderer: Menu.MenuRenderer? = null,
    ) {
        @Serializable
        data class MusicPlayButtonRenderer(
            val playNavigationEndpoint: NavigationEndpoint? = null,
        )

        @Serializable
        data class ToggleButtonRenderer(
            val isToggled: Boolean = false,
        )
    }
}
