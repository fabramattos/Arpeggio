package br.com.arpeggio.api.domain.streamings.deezer

data class DeezerSearchIdDetailResponse(
    val data: List<DeezerIdDetail>,
    val total: Int,
)