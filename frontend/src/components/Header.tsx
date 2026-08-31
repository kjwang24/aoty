interface HeaderProps {
  displayName: string
}

function Header({ displayName }: HeaderProps) {
  return (
    <header className="app-header">
      <span className="header-name">{displayName}'s</span>
      <span className="header-subtitle">year in music</span>
    </header>
  )
}

export default Header
