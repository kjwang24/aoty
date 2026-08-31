interface IconProps {
  className?: string
}

// One stroke treatment across the set: butt caps and mitred joins, so every line ends on a
// flat cut rather than a rounded terminal. Cut ends read as chiselled; round ones read as UI.
const STROKE = {
  stroke: 'currentColor',
  strokeWidth: 2.25,
  strokeLinecap: 'butt',
  strokeLinejoin: 'miter',
} as const

export function ChevronLeftIcon({ className }: IconProps) {
  return (
    <svg className={className} viewBox="0 0 24 24" fill="none" aria-hidden="true">
      <path d="M15 4 L8 12 L15 20" {...STROKE} />
    </svg>
  )
}

export function ChevronRightIcon({ className }: IconProps) {
  return (
    <svg className={className} viewBox="0 0 24 24" fill="none" aria-hidden="true">
      <path d="M9 4 L16 12 L9 20" {...STROKE} />
    </svg>
  )
}

export function CloseIcon({ className }: IconProps) {
  return (
    <svg className={className} viewBox="0 0 24 24" fill="none" aria-hidden="true">
      <path d="M6 6 L18 18 M18 6 L6 18" {...STROKE} />
    </svg>
  )
}

export function PlusIcon({ className }: IconProps) {
  return (
    <svg className={className} viewBox="0 0 24 24" fill="none" aria-hidden="true">
      <path d="M12 4 L12 20 M4 12 L20 12" {...STROKE} />
    </svg>
  )
}

export function PencilIcon({ className }: IconProps) {
  return (
    <svg className={className} viewBox="0 0 24 24" fill="none" aria-hidden="true">
      <path d="M16.5 3.5 L20.5 7.5 L8 20 L3.5 20.5 L4 16 Z M14.5 5.5 L18.5 9.5" {...STROKE} />
    </svg>
  )
}
