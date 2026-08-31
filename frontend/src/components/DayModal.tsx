import { useEffect, useRef, useState, type CSSProperties } from 'react'
import type { Entry, SongSelection } from '../api'
import { createEntry, updateEntry, searchTracks, getSuggestions } from '../api'
import { formatDisplayDate, todayKey, getSeasonTint } from '../dateUtils'
import { CloseIcon, PencilIcon } from '../icons'

interface DayModalProps {
  dateKey: string
  entry: Entry | undefined
  onClose: () => void
  onSaved: () => void
  /** Dev-only, from ?mock=1 — stands in for the /suggestions call. */
  mockSuggestions?: SongSelection[]
}

function entryToSongSelection(entry: Entry): SongSelection {
  return {
    spotifyId: entry.spotifyId,
    songName: entry.songName,
    songArtist: entry.songArtist,
    songCoverArt: entry.songCoverArt,
  }
}

function DayModal({ dateKey, entry, onClose, onSaved, mockSuggestions }: DayModalProps) {
  const isToday = dateKey === todayKey()
  const month = Number(dateKey.split('-')[1])
  const modalStyle = { '--season-tint': getSeasonTint(month - 1) } as CSSProperties
  const [mode, setMode] = useState<'view' | 'edit'>(entry ? 'view' : 'edit')
  const [selectedSong, setSelectedSong] = useState<SongSelection | null>(
    entry ? entryToSongSelection(entry) : null,
  )
  const [note, setNote] = useState(entry?.note ?? '')
  const [query, setQuery] = useState('')
  const [results, setResults] = useState<SongSelection[]>([])
  const [searching, setSearching] = useState(false)
  const [suggestions, setSuggestions] = useState<SongSelection[]>([])
  const [suggestionsLoaded, setSuggestionsLoaded] = useState(false)
  const [showConfirm, setShowConfirm] = useState(false)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const modalRef = useRef<HTMLDivElement>(null)

  // The search box used to take focus on open, which lit its amber ring and made the fallback
  // the loudest thing on screen while the three songs sat quietly above it. Focus the dialog
  // itself instead: focus still lands inside the modal, and tabbing runs close -> songs ->
  // search, which is the order the screen is meant to be read in.
  useEffect(() => {
    modalRef.current?.focus()
  }, [])

  // Fetched once per open, not once per visit to the picker — re-rolling the three every time
  // the user backs out of a selection would make the choice feel like it was slipping away.
  useEffect(() => {
    if (mode !== 'edit' || suggestionsLoaded) {
      return
    }
    setSuggestionsLoaded(true)
    if (mockSuggestions) {
      setSuggestions(mockSuggestions)
      return
    }
    // Suggestions are a convenience; if the history isn't there, the search box still is.
    getSuggestions().then(setSuggestions).catch(() => setSuggestions([]))
  }, [mode, suggestionsLoaded, mockSuggestions])

  useEffect(() => {
    if (mode !== 'edit' || selectedSong) {
      return
    }
    if (!query.trim()) {
      setResults([])
      setSearching(false)
      return
    }
    setSearching(true)
    const timer = setTimeout(() => {
      searchTracks(query)
        .then(setResults)
        .catch(() => setResults([]))
        .finally(() => setSearching(false))
    }, 1000)
    return () => clearTimeout(timer)
  }, [query, mode, selectedSong])

  // Once the user starts typing they've left the shortlist behind, and keeping it on screen
  // stacks three cards above a scrolling result list in one 500px box.
  const showSuggestions = !selectedSong && !query.trim() && suggestions.length > 0
  const isPicking = mode === 'edit' && !selectedSong

  function startEditing() {
    if (entry) {
      setSelectedSong(entryToSongSelection(entry))
      setNote(entry.note ?? '')
    }
    setMode('edit')
  }

  async function actuallySave() {
    if (!selectedSong) return
    setSaving(true)
    setError(null)
    try {
      if (entry) {
        await updateEntry(dateKey, selectedSong, note)
      } else {
        await createEntry(dateKey, selectedSong, note)
      }
      onSaved()
      onClose()
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Something went wrong saving this entry.')
    } finally {
      setSaving(false)
    }
  }

  function handleSaveClick() {
    if (!isToday) {
      setShowConfirm(true)
    } else {
      actuallySave()
    }
  }

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div
        ref={modalRef}
        tabIndex={-1}
        role="dialog"
        aria-modal="true"
        aria-label={formatDisplayDate(dateKey)}
        className={isPicking ? 'modal picking' : 'modal'}
        style={modalStyle}
        onClick={(event) => event.stopPropagation()}
      >
        <div className="modal-header">
          <button type="button" className="modal-close" onClick={onClose} aria-label="Close">
            <CloseIcon />
          </button>
          <span className="modal-date">{formatDisplayDate(dateKey)}</span>
          {mode === 'edit' && selectedSong && (
            <button type="button" className="modal-save" onClick={handleSaveClick} disabled={saving}>
              {saving ? 'Saving…' : 'Save'}
            </button>
          )}
          {mode === 'view' && isToday && (
            <button type="button" className="modal-edit" onClick={startEditing} aria-label="Edit today's entry">
              <PencilIcon />
            </button>
          )}
        </div>

        {error && <p className="modal-error">{error}</p>}

        {mode === 'view' && entry && (
          <div className="modal-body view">
            <img src={entry.songCoverArt} alt="" className="cover-art large" />
            <div className="song-name">{entry.songName}</div>
            <div className="song-artist">{entry.songArtist}</div>
            {entry.note && <p className="note">{entry.note}</p>}
          </div>
        )}

        {mode === 'edit' && (
          <div className="modal-body edit">
            {showSuggestions && (
              <div className="suggestions">
                <p className="suggestions-label">Pick from your history...</p>
                <div className="suggestion-row">
                  {suggestions.map((suggestion) => (
                    <button
                      type="button"
                      key={suggestion.spotifyId}
                      className="suggestion"
                      onClick={() => setSelectedSong(suggestion)}
                    >
                      <img src={suggestion.songCoverArt} alt="" className="cover-art suggestion-art" />
                      <span className="song-name">{suggestion.songName}</span>
                      <span className="song-artist">{suggestion.songArtist}</span>
                    </button>
                  ))}
                </div>
              </div>
            )}

            {!selectedSong && (
              <div className="search">
                <input
                  type="text"
                  className="search-input"
                  placeholder="or search for a song…"
                  value={query}
                  onChange={(event) => setQuery(event.target.value)}
                />
                {searching && <p className="search-status">Searching…</p>}
                <ul className="search-results">
                  {results.map((result) => (
                    <li key={result.spotifyId}>
                      <button type="button" onClick={() => setSelectedSong(result)}>
                        <img src={result.songCoverArt} alt="" className="cover-art small" />
                        <span className="result-text">
                          <span className="song-name">{result.songName}</span>
                          <span className="song-artist">{result.songArtist}</span>
                        </span>
                      </button>
                    </li>
                  ))}
                </ul>
              </div>
            )}

            {selectedSong && (
              <>
                <img src={selectedSong.songCoverArt} alt="" className="cover-art large" />
                <div className="song-name">{selectedSong.songName}</div>
                <div className="song-artist">{selectedSong.songArtist}</div>
                <button type="button" className="change-song" onClick={() => setSelectedSong(null)}>
                  Change song
                </button>
                <textarea
                  className="note-input"
                  placeholder="Leave a note for your future self…"
                  value={note}
                  onChange={(event) => setNote(event.target.value)}
                />
              </>
            )}
          </div>
        )}

        {showConfirm && (
          <div className="confirm-overlay" onClick={() => setShowConfirm(false)}>
            <div className="confirm-box" onClick={(event) => event.stopPropagation()}>
              <p>You can't change this entry once it's saved, since it's not today. Save anyway?</p>
              <div className="confirm-actions">
                <button
                  type="button"
                  onClick={() => {
                    setShowConfirm(false)
                    actuallySave()
                  }}
                >
                  Yes, save
                </button>
                <button type="button" onClick={() => setShowConfirm(false)}>
                  Cancel
                </button>
              </div>
            </div>
          </div>
        )}
      </div>
    </div>
  )
}

export default DayModal
