/*
 * ArchiveTune (2026)
 * © Rukamori — github.com/rukamori
 * GPL-3.0 License | Contributors: see git history
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package moe.rukamori.archivetune.innertube.pages

import moe.rukamori.archivetune.innertube.models.BrowseEndpoint
import moe.rukamori.archivetune.innertube.models.GridRenderer
import moe.rukamori.archivetune.innertube.models.MusicShelfRenderer
import moe.rukamori.archivetune.innertube.models.SectionListRenderer
import moe.rukamori.archivetune.innertube.models.YTItem
import moe.rukamori.archivetune.innertube.models.filterExplicit
import moe.rukamori.archivetune.innertube.models.filterVideo
import moe.rukamori.archivetune.innertube.models.getContinuation
import moe.rukamori.archivetune.innertube.models.response.BrowseResponse

data class BrowseResult(
    val title: String?,
    val thumbnail: String? = null,
    val items: List<Item>,
    val continuation: String? = null,
) {
    data class Item(
        val title: String?,
        val items: List<YTItem>,
        val endpoint: BrowseEndpoint? = null,
        val continuation: String? = null,
    )

    companion object {
        fun sections(contents: List<SectionListRenderer.Content>): List<Item> =
            contents.flatMap { content ->
                buildList {
                    content.gridRenderer?.let { add(grid(it)) }
                    content.musicCarouselShelfRenderer?.let { shelf ->
                        val header = shelf.header?.musicCarouselShelfBasicHeaderRenderer
                        val button = header?.moreContentButton?.buttonRenderer
                        add(
                            Item(
                                title = header?.title?.runs?.joinToString("") { it.text },
                                items = shelf.contents.flatMap { row ->
                                    row.musicCardShelfRenderer?.let(SearchSummaryPage.Companion::itemsFromMusicCardShelfRenderer)
                                        ?: listOfNotNull(
                                            row.musicTwoRowItemRenderer?.let(RelatedPage.Companion::fromMusicTwoRowItemRenderer)
                                                ?: row.musicResponsiveListItemRenderer?.let(SearchPage::toYTItem)
                                                ?: row.musicMultiRowListItemRenderer?.toEpisodeItem(),
                                        )
                                },
                                endpoint = button?.navigationEndpoint?.browseEndpoint ?: button?.command?.browseEndpoint
                                    ?: header?.title?.runs?.firstNotNullOfOrNull { it.navigationEndpoint?.browseEndpoint },
                                continuation = shelf.continuations?.getContinuation()
                                    ?: shelf.contents.firstNotNullOfOrNull {
                                        it.continuationItemRenderer?.continuationEndpoint?.continuationCommand?.token
                                    },
                            ),
                        )
                    }
                    content.musicShelfRenderer?.let { add(shelf(it)) }
                    content.musicCardShelfRenderer?.let { card ->
                        add(Item(
                            title = card.header?.musicCardShelfHeaderBasicRenderer?.title?.runs?.joinToString("") { it.text },
                            items = SearchSummaryPage.itemsFromMusicCardShelfRenderer(card),
                        ))
                    }
                    content.musicPlaylistShelfRenderer?.let { playlist ->
                        add(
                            Item(
                                title = null,
                                items = playlist.contents.mapNotNull {
                                    it.musicResponsiveListItemRenderer?.let(SearchPage::toYTItem)
                                        ?: it.musicMultiRowListItemRenderer?.toEpisodeItem()
                                },
                                continuation = playlist.continuations?.getContinuation() ?: playlist.contents.getContinuation(),
                            ),
                        )
                    }
                    content.itemSectionRenderer?.contents.orEmpty().forEach { nested ->
                        nested.gridRenderer?.let { add(grid(it)) }
                        nested.musicShelfRenderer?.let { add(shelf(it)) }
                        nested.musicResponsiveListItemRenderer?.let(SearchPage::toYTItem)?.let {
                            add(Item(title = null, items = listOf(it)))
                        }
                    }
                }
            }

        fun fromContinuation(response: BrowseResponse): BrowseResult {
            val contents = response.continuationContents
            check(
                contents?.sectionListContinuation != null || contents?.gridContinuation != null ||
                    contents?.musicShelfContinuation != null || contents?.musicPlaylistShelfContinuation != null ||
                    response.onResponseReceivedActions.orEmpty().any { it.appendContinuationItemsAction != null },
            ) { "Browse response has no continuation contents" }
            val sections = buildList {
                addAll(sections(contents?.sectionListContinuation?.contents.orEmpty()))
                contents?.gridContinuation?.let { grid ->
                    add(
                        Item(
                            title = null,
                            items = grid.items.mapNotNull { it.musicTwoRowItemRenderer?.let(RelatedPage.Companion::fromMusicTwoRowItemRenderer) },
                        ),
                    )
                }
                contents?.musicShelfContinuation?.let { add(shelf(it).copy(continuation = null)) }
                contents?.musicPlaylistShelfContinuation?.let { shelf ->
                    add(Item(title = null, items = shelf.contents.mapNotNull {
                        it.musicResponsiveListItemRenderer?.let(SearchPage::toYTItem)
                            ?: it.musicMultiRowListItemRenderer?.toEpisodeItem()
                    }))
                }
                response.onResponseReceivedActions.orEmpty().forEach { action ->
                    add(
                        Item(
                            title = null,
                            items = action.appendContinuationItemsAction?.continuationItems.orEmpty()
                                .mapNotNull {
                                    it.musicTwoRowItemRenderer?.let(RelatedPage.Companion::fromMusicTwoRowItemRenderer)
                                        ?: it.musicResponsiveListItemRenderer?.let(SearchPage::toYTItem)
                                        ?: it.musicMultiRowListItemRenderer?.toEpisodeItem()
                                },
                        ),
                    )
                }
            }
            return BrowseResult(
                title = null,
                items = sections,
                continuation = contents?.sectionListContinuation?.continuations?.getContinuation()
                    ?: contents?.sectionListContinuation?.contents?.firstNotNullOfOrNull {
                        it.continuationItemRenderer?.continuationEndpoint?.continuationCommand?.token
                    }
                    ?: contents?.gridContinuation?.continuations?.getContinuation()
                    ?: contents?.gridContinuation?.items?.firstNotNullOfOrNull {
                        it.continuationItemRenderer?.continuationEndpoint?.continuationCommand?.token
                    }
                    ?: contents?.musicShelfContinuation?.continuations?.getContinuation()
                    ?: contents?.musicShelfContinuation?.contents?.getContinuation()
                    ?: contents?.musicPlaylistShelfContinuation?.continuations?.getContinuation()
                    ?: contents?.musicPlaylistShelfContinuation?.contents?.getContinuation()
                    ?: response.onResponseReceivedActions.orEmpty().firstNotNullOfOrNull {
                        it.appendContinuationItemsAction?.continuationItems?.getContinuation()
                    },
            )
        }

        private fun grid(renderer: GridRenderer) = Item(
            title = renderer.header?.gridHeaderRenderer?.title?.runs?.joinToString("") { it.text },
            items = renderer.items.mapNotNull { it.musicTwoRowItemRenderer?.let(RelatedPage.Companion::fromMusicTwoRowItemRenderer) },
            continuation = renderer.continuations?.getContinuation()
                ?: renderer.items.firstNotNullOfOrNull {
                    it.continuationItemRenderer?.continuationEndpoint?.continuationCommand?.token
                },
        )

        private fun shelf(renderer: MusicShelfRenderer): Item {
            val button = renderer.moreContentButton?.buttonRenderer
            return Item(
                title = renderer.title?.runs?.joinToString("") { it.text },
                items = renderer.contents.orEmpty().mapNotNull {
                    it.musicTwoRowItemRenderer?.let(RelatedPage.Companion::fromMusicTwoRowItemRenderer)
                        ?: it.musicResponsiveListItemRenderer?.let(SearchPage::toYTItem)
                        ?: it.musicMultiRowListItemRenderer?.toEpisodeItem()
                },
                endpoint = button?.navigationEndpoint?.browseEndpoint ?: button?.command?.browseEndpoint
                    ?: renderer.bottomEndpoint?.browseEndpoint,
                continuation = renderer.continuations?.getContinuation() ?: renderer.contents?.getContinuation(),
            )
        }
    }

    fun filterExplicit(enabled: Boolean = true) =
        if (enabled) {
            copy(
                items =
                    items.mapNotNull {
                        it.copy(
                            items =
                                it.items
                                    .filterExplicit()
                                    .ifEmpty { return@mapNotNull null },
                        )
                    },
            )
        } else {
            this
        }

    fun filterVideo(enabled: Boolean = true) =
        if (enabled) {
            copy(
                items =
                    items.mapNotNull {
                        it.copy(
                            items =
                                it.items
                                    .filterVideo()
                                    .ifEmpty { return@mapNotNull null },
                        )
                    },
            )
        } else {
            this
        }
}
