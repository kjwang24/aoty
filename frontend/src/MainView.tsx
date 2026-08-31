import { useCallback, useEffect, useState } from 'react'
import Header from './components/Header'
import Calendar from './components/Calendar'
import DayModal from './components/DayModal'
import PlaylistEmbed from './components/PlaylistEmbed'
import SeasonArt from './components/SeasonArt'
import { getEntries, type Entry, type SongSelection } from './api'

interface MainViewProps {
  displayName: string
  mockEntries?: Entry[]
  mockSuggestions?: SongSelection[]
  mockOpenDay?: string | null
}

type Tab = 'calendar' | 'playlist'

function MainView({ displayName, mockEntries, mockSuggestions, mockOpenDay }: MainViewProps) {
  const [tab, setTab] = useState<Tab>('calendar')
  const [entries, setEntries] = useState<Entry[]>([])
  const [selectedDate, setSelectedDate] = useState<string | null>(mockOpenDay ?? null)
  // Lives here rather than in Calendar because the seasonal page art keys off it too.
  const [viewDate, setViewDate] = useState(() => {
    const now = new Date()
    return new Date(now.getFullYear(), now.getMonth(), 1)
  })

  const loadEntries = useCallback(() => {
    if (mockEntries) {
      setEntries(mockEntries)
      return
    }
    getEntries()
      .then(setEntries)
      .catch(() => setEntries([]))
  }, [mockEntries])

  useEffect(() => {
    loadEntries()
  }, [loadEntries])

  const entriesByDate = new Map(entries.map((entry) => [entry.date, entry]))

  return (
    <div className="app-shell">
      <SeasonArt month={viewDate.getMonth()} />
      <Header displayName={displayName} />
      <div className="inset">
        <div className="tabs">
          <button
            type="button"
            className={tab === 'calendar' ? 'tab active' : 'tab'}
            onClick={() => setTab('calendar')}
          >
            Calendar
          </button>
          <button
            type="button"
            className={tab === 'playlist' ? 'tab active' : 'tab'}
            onClick={() => setTab('playlist')}
          >
            Playlist
          </button>
        </div>
        <div className="tab-content">
          {tab === 'calendar' ? (
            <Calendar
              entries={entriesByDate}
              onDayClick={setSelectedDate}
              viewDate={viewDate}
              onViewDateChange={setViewDate}
            />
          ) : (
            <PlaylistEmbed />
          )}
        </div>
      </div>
      {selectedDate && (
        <DayModal
          dateKey={selectedDate}
          entry={entriesByDate.get(selectedDate)}
          onClose={() => setSelectedDate(null)}
          onSaved={loadEntries}
          mockSuggestions={mockSuggestions}
        />
      )}
    </div>
  )
}

export default MainView
