tournament

pairing
id = gameid
tourId = String
users = string



masa

round
id = gameid
masaId = String
players: Sides[Option[playerId]]

player
playerId
masaId
Option[userId]
score: Int

pairing
id = gameId
masaId
status
players: Sides[playerId]
scores: Option[Sides[RoundScore]]

masa 1
  select side
  ready -> redirect to next round
  endmatch
    select side
    rematch -> redirect to next masa round
round 1/side
round 2/side
  ingame
  nextround -> redirect to next round
  abort -> redirect to masa

/join
player join(Option[userId])

/show masaId
MasaRepo.byId(masaId)
playablePovRefForReq(masa)
jsonView(masa, povref)

previous pairings

recentPairing povRef

PovRef

player -> pairing(ref.side)
left -> pairing(ref.left)
right -> pairing(ref.right)


masa
created
  select side -> pairing
  begin
in progress
  notify game
  standings
aborted / ended
  select side -> pairing
  rematch
  standings

masa view

recentPairing
  :playing?
    notify game
  :created?
    select side
  :done?
    show winner

1. playerA  10 10 10 30 110
2. playerB  20 20 20 40 100
3. playerC  20 30 30 50 90
4. playerD  20 30 30 50 80
5. playerE  0   0  0 10 10

playerA (10) playerB (20) playerC (30) playerD (40)
playerA (10) playerB (20) playerC (30) playerD (40)
playerA (10) playerB (20) playerC (30) playerE (40)


How does a user join masa?

playerId?
userId?

join side playerId userId
  withdraw playerId
  playerId ?? newPlayer pId
    join playerId side


## Round UI

What to show?

Min Open Serie
Min Open Pair
Open Status for each player
last move indicator / oyun ozeti
How to play hopscotch
