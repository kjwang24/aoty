import { type CSSProperties } from 'react'
import type { Entry } from '../api'
import { toDateKey, todayKey, getSeasonTint, MONTH_NAMES } from '../dateUtils'
import { ChevronLeftIcon, ChevronRightIcon, PlusIcon } from '../icons'

interface CalendarProps {
  entries: Map<string, Entry>
  onDayClick: (dateKey: string) => void
  /** Owned by MainView so the seasonal page art can follow the month on show. */
  viewDate: Date
  onViewDateChange: (date: Date) => void
}

const WEEKDAYS = ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat']

const PLUS_JEWELS = [
  'var(--jewel-red)',
  'var(--jewel-gold)',
  'var(--jewel-green)',
  'var(--jewel-blue)',
]

// Deterministic per-day pseudo-randomness (FNV-1a) so the same day always frames its
// cover art the same way, but no two cells share the same crop/rotation.
function seedFrom(key: string): number {
  let hash = 2166136261
  for (let i = 0; i < key.length; i++) {
    hash ^= key.charCodeAt(i)
    hash = Math.imul(hash, 16777619)
  }
  // FNV alone barely moves the high bits in response to the final character, so keys that
  // differ only in their last byte — dateKey + 'r' vs 's' vs 'x' — land within a hundredth
  // of each other, and every cell ends up framed almost identically. This murmur3 finalizer
  // avalanches the low bits upward before the magnitude is read as a fraction.
  hash ^= hash >>> 16
  hash = Math.imul(hash, 2246822507)
  hash ^= hash >>> 13
  hash = Math.imul(hash, 3266489909)
  hash ^= hash >>> 16
  return (hash >>> 0) / 4294967295
}

function Calendar({ entries, onDayClick, viewDate, onViewDateChange }: CalendarProps) {
  const year = viewDate.getFullYear()
  const month = viewDate.getMonth()
  const firstWeekday = new Date(year, month, 1).getDay()
  const daysInMonth = new Date(year, month + 1, 0).getDate()
  const today = todayKey()

  const cells: (number | null)[] = []
  for (let i = 0; i < firstWeekday; i++) cells.push(null)
  for (let day = 1; day <= daysInMonth; day++) cells.push(day)
  // Always pad to a full 6 rows (42 cells) so row height stays constant across months —
  // otherwise a 4-row February would stretch its cells taller than a 6-row month.
  while (cells.length < 42) cells.push(null)

  return (
    <div className="calendar">
      <div className="calendar-header">
        <button
          type="button"
          className="calendar-nav"
          onClick={() => onViewDateChange(new Date(year, month - 1, 1))}
          aria-label="Previous month"
        >
          <ChevronLeftIcon />
        </button>
        <h2>{MONTH_NAMES[month]}</h2>
        <button
          type="button"
          className="calendar-nav"
          onClick={() => onViewDateChange(new Date(year, month + 1, 1))}
          aria-label="Next month"
        >
          <ChevronRightIcon />
        </button>
      </div>
      <div className="calendar-grid">
        {WEEKDAYS.map((weekday) => (
          <div key={weekday} className="calendar-weekday">
            {weekday}
          </div>
        ))}
        {cells.map((day, index) => {
          if (day === null) {
            return <div key={`blank-${index}`} className="calendar-day pane-void" />
          }
          const cellDate = new Date(year, month, day)
          const dateKey = toDateKey(cellDate)
          const isFuture = dateKey > today
          const isToday = dateKey === today
          const entry = entries.get(dateKey)

          const state = entry ? 'filled' : isFuture ? 'locked' : 'fillable'
          // Set per cell rather than per grid: the leading and trailing cells of a month
          // belong to it, so the whole sheet stays one colour.
          const style: CSSProperties & Record<string, string> = {
            '--season-tint': getSeasonTint(month),
          }
          if (state === 'fillable') {
            // Which stone an empty pane lights up in is the day's own, drawn off its date and
            // fixed there — so hovering across the grid turns up all four, but a given day
            // always answers in the same colour.
            style['--plus-jewel'] = PLUS_JEWELS[
              Math.floor(seedFrom(`${dateKey}#plus`) * PLUS_JEWELS.length) % PLUS_JEWELS.length
            ]
          }

          let glassStyle: (CSSProperties & Record<string, string>) | undefined
          if (entry) {
            // The cell's background is the day's own cover art — cropped, zoomed, rotated and
            // desaturated per-day (seeded off the date) so it reads as an abstract wash of color
            // rather than a recognizable thumbnail, while the roundel below shows the real art.
            const rotation = (seedFrom(dateKey + 'r') - 0.5) * 60
            const scale = 1.6 + seedFrom(dateKey + 's') * 0.5
            const posX = 20 + seedFrom(dateKey + 'x') * 60
            const posY = 20 + seedFrom(dateKey + 'y') * 60
            glassStyle = {
              '--art': `url(${entry.songCoverArt})`,
              '--art-rot': `${rotation.toFixed(1)}deg`,
              '--art-scale': scale.toFixed(2),
              '--art-pos': `${posX.toFixed(0)}% ${posY.toFixed(0)}%`,
            }
          }

          return (
            <button
              type="button"
              key={dateKey}
              className={`calendar-day pane-${state}${isToday ? ' pane-today' : ''}`}
              style={style}
              onClick={() => onDayClick(dateKey)}
              disabled={isFuture}
              title={entry ? `${entry.songName} — ${entry.songArtist}` : undefined}
            >
              {entry && (
                <>
                  <div className="cell-glass" style={glassStyle} />
                  <div className="cell-sheen" />
                </>
              )}
              <span className="day-number">{day}</span>
              {entry && (
                <div className="cell-roundel">
                  <img src={entry.songCoverArt} alt="" className="cover-art cell" />
                </div>
              )}
              {state === 'fillable' && (
                <PlusIcon className="pane-plus" />
              )}
            </button>
          )
        })}
      </div>
    </div>
  )
}

export default Calendar
