import type { Entry, SongSelection } from './api'

// Dev-only fixture for visually iterating on the calendar without a real Spotify login.
// Delete this file (and its usage in App.tsx/MainView.tsx) once design work is done.
export const MOCK_ENTRIES: Entry[] = [
  { id: 1, date: '2026-01-08', spotifyId: 'm1', songName: 'Skinny Love', songArtist: 'Bon Iver', songCoverArt: 'https://picsum.photos/seed/aoty1/300', note: null },
  { id: 2, date: '2026-01-22', spotifyId: 'm2', songName: 'Holocene', songArtist: 'Bon Iver', songCoverArt: 'https://picsum.photos/seed/aoty2/300', note: null },
  { id: 3, date: '2026-02-05', spotifyId: 'm3', songName: 'Motion Sickness', songArtist: 'Phoebe Bridgers', songCoverArt: 'https://picsum.photos/seed/aoty3/300', note: null },
  { id: 4, date: '2026-02-19', spotifyId: 'm4', songName: 'I Know the End', songArtist: 'Phoebe Bridgers', songCoverArt: 'https://picsum.photos/seed/aoty4/300', note: null },
  { id: 5, date: '2026-03-03', spotifyId: 'm5', songName: 'Constellations Are Just Distant Suns That Have Not Told Us They Are Dying Yet', songArtist: 'A Band With A Very Long Name', songCoverArt: 'https://picsum.photos/seed/aoty5/300', note: null },
  { id: 6, date: '2026-03-17', spotifyId: 'm6', songName: 'Liability', songArtist: 'Lorde', songCoverArt: 'https://picsum.photos/seed/aoty6/300', note: null },
  { id: 7, date: '2026-04-02', spotifyId: 'm7', songName: 'Green Light', songArtist: 'Lorde', songCoverArt: 'https://picsum.photos/seed/aoty7/300', note: null },
  { id: 8, date: '2026-04-21', spotifyId: 'm8', songName: 'Cherry Blossom Girl', songArtist: 'Air', songCoverArt: 'https://picsum.photos/seed/aoty8/300', note: null },
  { id: 9, date: '2026-05-06', spotifyId: 'm9', songName: 'Circles', songArtist: 'Mac Miller', songCoverArt: 'https://picsum.photos/seed/aoty9/300', note: null },
  { id: 10, date: '2026-05-29', spotifyId: 'm10', songName: 'Self Care', songArtist: 'Mac Miller', songCoverArt: 'https://picsum.photos/seed/aoty10/300', note: null },
  { id: 11, date: '2026-06-01', spotifyId: 'm11', songName: 'Sunflower', songArtist: 'Post Malone', songCoverArt: 'https://picsum.photos/seed/aoty11/300', note: null },
  { id: 12, date: '2026-06-15', spotifyId: 'm12', songName: 'Blinding Lights', songArtist: 'The Weeknd', songCoverArt: 'https://picsum.photos/seed/aoty12/300', note: null },
  { id: 13, date: '2026-07-04', spotifyId: 'm13', songName: 'Golden Hour', songArtist: 'Kacey Musgraves', songCoverArt: 'https://picsum.photos/seed/aoty13/300', note: null },
  { id: 14, date: '2026-07-19', spotifyId: 'm14', songName: 'Slow Burn', songArtist: 'Kacey Musgraves', songCoverArt: 'https://picsum.photos/seed/aoty14/300', note: null },
  { id: 15, date: '2026-08-03', spotifyId: 'm15', songName: 'Redbone', songArtist: 'Childish Gambino', songCoverArt: 'https://picsum.photos/seed/aoty15/300', note: null },
  { id: 16, date: '2026-08-14', spotifyId: 'm16', songName: 'Feels Like Summer', songArtist: 'Childish Gambino', songCoverArt: 'https://picsum.photos/seed/aoty16/300', note: null },
  { id: 17, date: '2026-08-26', spotifyId: 'm17', songName: '3WW', songArtist: 'FKA twigs', songCoverArt: 'https://picsum.photos/seed/aoty17/300', note: 'today, for testing' },
  { id: 18, date: '2026-09-09', spotifyId: 'm18', songName: 'Harvest Moon', songArtist: 'Neil Young', songCoverArt: 'https://picsum.photos/seed/aoty18/300', note: null },
  { id: 19, date: '2026-09-24', spotifyId: 'm19', songName: 'Autumn Leaves', songArtist: 'Eva Cassidy', songCoverArt: 'https://picsum.photos/seed/aoty19/300', note: null },
  { id: 20, date: '2026-10-11', spotifyId: 'm20', songName: 'A Sky Full of Stars', songArtist: 'Coldplay', songCoverArt: 'https://picsum.photos/seed/aoty20/300', note: null },
  { id: 21, date: '2026-10-28', spotifyId: 'm21', songName: 'Everybody Wants to Rule the World', songArtist: 'Tears for Fears', songCoverArt: 'https://picsum.photos/seed/aoty21/300', note: null },
  { id: 22, date: '2026-11-11', spotifyId: 'm22', songName: 'Holocene II', songArtist: 'Bon Iver', songCoverArt: 'https://picsum.photos/seed/aoty22/300', note: null },
  { id: 23, date: '2026-11-27', spotifyId: 'm23', songName: 'Iris', songArtist: 'Goo Goo Dolls', songCoverArt: 'https://picsum.photos/seed/aoty23/300', note: null },
  { id: 24, date: '2026-12-10', spotifyId: 'm24', songName: 'Winter Song', songArtist: 'Sara Bareilles', songCoverArt: 'https://picsum.photos/seed/aoty24/300', note: null },
  { id: 25, date: '2026-12-24', spotifyId: 'm25', songName: 'River', songArtist: 'Joni Mitchell', songCoverArt: 'https://picsum.photos/seed/aoty25/300', note: null },
]

// The three suggestions a new day offers. Deliberately includes an overlong title and an
// overlong artist, since three cards across a 420px modal is where those break.
export const MOCK_SUGGESTIONS: SongSelection[] = [
  { spotifyId: 's1', songName: 'Night Shift', songArtist: 'Lucy Dacus', songCoverArt: 'https://picsum.photos/seed/aotys1/300' },
  { spotifyId: 's2', songName: 'Constellations Are Just Distant Suns', songArtist: 'A Band With A Very Long Name', songCoverArt: 'https://picsum.photos/seed/aotys2/300' },
  { spotifyId: 's3', songName: 'Pink + White', songArtist: 'Frank Ocean', songCoverArt: 'https://picsum.photos/seed/aotys3/300' },
]
