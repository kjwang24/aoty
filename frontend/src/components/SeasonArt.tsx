import { getSeason } from '../dateUtils'
import fallLeft from '../assets/fall_left.png'
import fallRight from '../assets/fall_right.png'
import springLeft from '../assets/spring_left.png'
import springRight from '../assets/spring_right.png'
import summerLeft from '../assets/summer_left.png'
import summerRight from '../assets/summer_right.png'
import winterLeft from '../assets/winter_left.png'
import winterRight from '../assets/winter_right.png'
import type { Season } from '../dateUtils'

const SEASON_ART: Record<Season, { left: string; right: string }> = {
  winter: { left: winterLeft, right: winterRight },
  spring: { left: springLeft, right: springRight },
  summer: { left: summerLeft, right: summerRight },
  fall: { left: fallLeft, right: fallRight },
}

interface SeasonArtProps {
  /** 0-indexed, matching Date.getMonth() — decides which season's pair is shown. */
  month: number
}

// Decoration in the margins either side of the 900px card: a 175px-wide band running from
// the calendar's edge out toward the screen edge, with the art pinned to the far end of the
// band. The bands are laid out in App.css off the same measurements the card uses, and
// shrink with the margin once the viewport is too narrow to hold them at full width.
function SeasonArt({ month }: SeasonArtProps) {
  const art = SEASON_ART[getSeason(month)]

  return (
    <>
      <div className="season-art season-art-left" aria-hidden="true">
        <img src={art.left} alt="" />
      </div>
      <div className="season-art season-art-right" aria-hidden="true">
        <img src={art.right} alt="" />
      </div>
    </>
  )
}

export default SeasonArt
