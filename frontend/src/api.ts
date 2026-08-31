export interface Entry {
  id: number
  date: string
  spotifyId: string
  songName: string
  songArtist: string
  songCoverArt: string
  note: string | null
}

export interface SongSelection {
  spotifyId: string
  songName: string
  songArtist: string
  songCoverArt: string
}

function csrfToken(): string {
  const match = document.cookie.match(/(?:^|; )XSRF-TOKEN=([^;]*)/)
  return match ? decodeURIComponent(match[1]) : ''
}

async function apiFetch(path: string, options: RequestInit = {}): Promise<Response> {
  return fetch(path, {
    credentials: 'include',
    ...options,
    headers: {
      'X-XSRF-TOKEN': csrfToken(),
      ...(options.headers ?? {}),
    },
  })
}

export async function getDisplayName(): Promise<string> {
  const res = await apiFetch('/me')
  if (!res.ok) throw new Error('failed to load user name')
  const data = await res.json()
  return data.display_name as string
}

export async function getPfpUrl(): Promise<string> {
  const res = await apiFetch('/me')
  if (!res.ok) throw new Error('failed to load user profile picture')
  const data = await res.json()
  return data.pfp_url as string
}

export async function getEntries(): Promise<Entry[]> {
  const res = await apiFetch('/entries')
  if (!res.ok) throw new Error('failed to load entries')
  return res.json()
}

export async function getPlaylistUrl(): Promise<string | null> {
  const res = await apiFetch('/playlist')
  if (res.status === 204) return null
  if (!res.ok) throw new Error('failed to load playlist')
  return res.text()
}

export async function getSuggestions(): Promise<SongSelection[]> {
  const res = await apiFetch('/suggestions')
  if (!res.ok) throw new Error('failed to load suggestions')
  return res.json()
}

export async function searchTracks(query: string): Promise<SongSelection[]> {
  const res = await apiFetch(`/search?q=${encodeURIComponent(query)}`)
  if (!res.ok) throw new Error('search failed')
  return res.json()
}

async function throwForStatus(res: Response, fallback: string): Promise<never> {
  const text = await res.text().catch(() => '')
  if (!text) throw new Error(fallback)
  let message = text
  try {
    const body = JSON.parse(text)
    if (typeof body.message === 'string') message = body.message
  } catch {
    // not JSON — use the raw text as-is
  }
  throw new Error(message)
}

export async function createEntry(date: string, song: SongSelection, note: string): Promise<void> {
  const res = await apiFetch('/entries', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      date,
      spotify_id: song.spotifyId,
      song_name: song.songName,
      song_artist: song.songArtist,
      song_cover_art: song.songCoverArt,
      note: note || null,
    }),
  })
  if (!res.ok) await throwForStatus(res, 'failed to create entry')
}

export async function updateEntry(date: string, song: SongSelection | null, note: string): Promise<void> {
  const body: Record<string, unknown> = { note: note || null }
  if (song) {
    body.spotify_id = song.spotifyId
    body.song_name = song.songName
    body.song_artist = song.songArtist
    body.song_cover_art = song.songCoverArt
  }
  const res = await apiFetch(`/entries/${date}`, {
    method: 'PATCH',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body),
  })
  if (!res.ok) await throwForStatus(res, 'failed to update entry')
}
