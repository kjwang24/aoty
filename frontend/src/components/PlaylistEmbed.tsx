import { useEffect, useState } from 'react'
import { getPlaylistUrl } from '../api'

function PlaylistEmbed() {
  const [playlistId, setPlaylistId] = useState<string | null | undefined>(undefined)

  useEffect(() => {
    getPlaylistUrl().then((url) => {
      setPlaylistId(url ? (url.split('/').pop() ?? null) : null)
    })
  }, [])

  if (playlistId === undefined) {
    return <p className="playlist-status">Loading playlist...</p>
  }

  if (playlistId === null) {
    return <p className="playlist-status">No entries yet</p>
  }

  return (
    <iframe
      className="playlist-embed"
      title="Spotify playlist"
      src={`https://open.spotify.com/embed/playlist/${playlistId}`}
      width="100%"
      height="480"
      allow="encrypted-media"
    />
  )
}

export default PlaylistEmbed
