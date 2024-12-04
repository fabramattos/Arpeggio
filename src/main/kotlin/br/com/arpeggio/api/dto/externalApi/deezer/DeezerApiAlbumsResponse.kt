package br.com.arpeggio.api.dto.externalApi.deezer

data class DeezerApiAlbumsResponse(
    val data: List<DeezerApiAlbumData>,
    val total: Int,
)