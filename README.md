tournament

pairing
id = gameid
tourId = String
users = string



match

round
id = gameid
matchId = String
users = Sides[option[string]]
scores: Option[Sides[RoundScore]]

match 1
  select side
  ready -> redirect to next ingame
  endmatch
    select side
    rematch -> redirect to next match ingame
round 1/side
round 2/side
  ingame
  nextround -> redirect to next round
  abort -> redirect to match


