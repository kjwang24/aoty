export function toDateKey(date: Date): string {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

export function todayKey(): string {
  return toDateKey(new Date())
}

const DISPLAY_MONTHS = [
  'January', 'February', 'March', 'April', 'May', 'June',
  'July', 'August', 'September', 'October', 'November', 'December',
]

export function formatDisplayDate(dateKey: string): string {
  const [year, month, day] = dateKey.split('-').map(Number)
  return `${DISPLAY_MONTHS[month - 1]} ${day}, ${year}`
}

export const MONTH_NAMES = DISPLAY_MONTHS

export type Season = 'winter' | 'spring' | 'summer' | 'fall'

const SEASON_BY_MONTH: Season[] = [
  'winter', 'winter', // Jan, Feb
  'spring', 'spring', 'spring', // Mar–May
  'summer', 'summer', 'summer', // Jun–Aug
  'fall', 'fall', 'fall', // Sep–Nov
  'winter', // Dec
]

// `month` is 0-indexed, matching Date.getMonth().
export function getSeason(month: number): Season {
  return SEASON_BY_MONTH[month]
}

// One flat tint per season — no drift between days and no blend across the boundary, so a
// month is a single sheet of colour. The four are pitched to the seasonal margin art: washed
// and pale rather than saturated, which is why summer is a pale honey and not a gold.
const SEASON_TINT_VARS: Record<Season, string> = {
  winter: 'var(--tint-winter)',
  spring: 'var(--tint-spring)',
  summer: 'var(--tint-summer)',
  fall: 'var(--tint-fall)',
}

// `month` is 0-indexed, matching Date.getMonth(). Returns a `var()` reference rather than a
// literal so the four hexes stay declared once, in the token layer.
export function getSeasonTint(month: number): string {
  return SEASON_TINT_VARS[getSeason(month)]
}
