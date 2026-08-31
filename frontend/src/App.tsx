import { useEffect, useState } from 'react'
import MainView from './MainView'
import SeasonArt from './components/SeasonArt'
import { getDisplayName } from './api'
import { MOCK_ENTRIES, MOCK_SUGGESTIONS } from './mockEntries'
import './App.css'

type AuthState = 'loading' | 'loggedOut' | 'loggedIn'

// ?mock=1 skips real auth entirely and renders MainView with fixture data, for design
// iteration without a live Spotify login.
//
// Gated on import.meta.env.DEV, which Vite replaces with a literal `false` at build time —
// so in a production bundle isMock is a constant false, the branch below is dead code, and
// mockEntries is tree-shaken out entirely. The harness stays available for future feature
// work without ever being reachable on the deployed site.
const params = new URLSearchParams(window.location.search)
const isMock = import.meta.env.DEV && params.has('mock')
// ?day=2026-08-20 opens that day's modal on load, so the picker can be inspected without
// having to drive a click.
const mockOpenDay = isMock ? params.get('day') : null

function App() {
  const [authState, setAuthState] = useState<AuthState>('loading')
  const [displayName, setDisplayName] = useState('')

  useEffect(() => {
    if (isMock) return
    fetch('/entries', { credentials: 'include' })
      .then((response) => {
        if (response.status === 401) {
          setAuthState('loggedOut')
          return
        }
        return getDisplayName().then((name) => {
          setDisplayName(name)
          setAuthState('loggedIn')
        })
      })
      .catch((err) => {
        console.error('failed to check login status', err)
        setAuthState('loggedOut')
      })
  }, [])

  // MainView renders its own SeasonArt, keyed to the month the calendar is showing; the
  // pre-login screens have no calendar, so they fall back to the real current month.
  const showsCalendar = isMock || authState === 'loggedIn'

  let content
  if (isMock) {
    content = (
      <MainView
        displayName="Preview"
        mockEntries={MOCK_ENTRIES}
        mockSuggestions={MOCK_SUGGESTIONS}
        mockOpenDay={mockOpenDay}
      />
    )
  } else if (authState === 'loading') {
    content = <p className="status-message">Loading…</p>
  } else if (authState === 'loggedOut') {
    content = (
      <div className="login-screen">
        <h1 className="login-title">Year in Music Journal</h1>
        {/* Same-origin, not an absolute localhost URL: in dev the Vite proxy forwards
            /oauth2 to :8080, and in production the SPA is served by the backend itself. */}
        <a className="login-link" href="/oauth2/authorization/spotify">
          Log in with Spotify
        </a>
      </div>
    )
  } else {
    content = <MainView displayName={displayName} />
  }

  return (
    <>
      {!showsCalendar && <SeasonArt month={new Date().getMonth()} />}
      {content}
    </>
  )
}

export default App
