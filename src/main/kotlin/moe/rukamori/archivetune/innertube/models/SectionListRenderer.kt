/*
 * ArchiveTune (2026)
 * © Rukamori — github.com/rukamori
 * GPL-3.0 License | Contributors: see git history
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package moe.rukamori.archivetune.innertube.models

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames

@Serializable
data class SectionListRenderer(
    val header: Header? = null,
    val contents: List<Content>? = null,
    val continuations: List<Continuation>? = null,
) {
    @Serializable
    data class Header(
        val chipCloudRenderer: ChipCloudRenderer? = null,
    ) {
        @Serializable
        data class ChipCloudRenderer(
            val chips: List<Chip>,
        ) {
            @Serializable
            data class Chip(
                val chipCloudChipRenderer: ChipCloudChipRenderer,
            ) {
                @Serializable
                data class ChipCloudChipRenderer(
                    val isSelected: Boolean = false,
                    val navigationEndpoint: NavigationEndpoint,
                    val onDeselectedCommand: NavigationEndpoint? = null,
                    // The close button doesn't have the following two fields
                    val text: Runs? = null,
                    val uniqueId: String? = null,
                )
            }
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    @Serializable
    data class Content(
        @JsonNames("musicImmersiveCarouselShelfRenderer")
        val musicCarouselShelfRenderer: MusicCarouselShelfRenderer? = null,
        val musicShelfRenderer: MusicShelfRenderer? = null,
        val musicCardShelfRenderer: MusicCardShelfRenderer? = null,
        val musicPlaylistShelfRenderer: MusicPlaylistShelfRenderer? = null,
        val musicDescriptionShelfRenderer: MusicDescriptionShelfRenderer? = null,
        val musicResponsiveHeaderRenderer: MusicResponsiveHeaderRenderer? = null,
        val musicEditablePlaylistDetailHeaderRenderer: MusicEditablePlaylistDetailHeaderRenderer? = null,
        val gridRenderer: GridRenderer? = null,
        val itemSectionRenderer: ItemSectionRenderer? = null,
    )

    @Serializable
    data class ItemSectionRenderer(
        val contents: List<ItemSectionContent>? = null,
    ) {
        @Serializable
        data class ItemSectionContent(
            val musicResponsiveListItemRenderer: MusicResponsiveListItemRenderer? = null,
            val gridRenderer: GridRenderer? = null,
            val musicShelfRenderer: MusicShelfRenderer? = null,
        )
    }
}
