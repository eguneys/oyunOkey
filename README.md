## TODO


## Features

Team
Q&A
Forum
Message
Follow
Timeline
Game stats
Leaderboard
Theme
staging.oyunkeyf



### Site
Add icons / logo / images
Add sounds

### Player
Add leaderboards

### Lobby
not allow more masa create
Fix masa reminder spam
+ match similar masa create / find compatible

## Extra
fix autolink / strip lang
differantiate anonymous users

### Masa
[bug] [rare] extra pairing is done after masa finish
[bug] [rare] game dissapears pairing stays
Check masa for removal / masa clock
Add masa perfs
Allow invite friends
+Fix withdraw masa interrupt
+Fix masa socket version update
+ improve game end reasons add to masa pairings
+ Add rated on masa view
+ Add rated/anonymous/bot masa setup
+ not allow anon create rated mode
+ not allow anon/bot enter rated

### Round
Rotate sides each round
Add round ingame penalty scores
Add uci piece icons/fonts
+ Fix Draw middle uci / uci translations
+ Fix chat room position
+ Invalidate cache on pov not found
+ Fix round score area
+ Add round open group scores
+ Add round spinner on buttons
~Fix round initial layout scroll
~ Fix titivate
~ Add round watcher view / improve move events

### Variant
101 Abort hand on middle end

DuzOkey perfs
DuzOkey scoring system/game end winner
DuzOkey end by series / pairs
DuzOkey game end show groups
+ DuzOkey don't show opens / add torr
+ DuzOkey variant docs
+ DuzOkey round end by zero score

## Ground

Add group drag / visual
Add last move animation
Notify duz okey end status
Add animations/fadings
[bug] force draw vs manual draw mix
[bug] dont allow twice discard
[bug] open series some unsync

## Ôyunkeyf Skor Sistemi

Her oyuncuya başlangıçta 1500 puan verilir. Bir masada oynanan her el oyuncunun puanını etkiler.


~~ Bir elde  ~~

~~ 1. olan oyuncu 1 puan alır.  ~~
~~ 2. olan oyuncu 0.5 puan alır. ~~
~~ 3. olan oyuncu 0.5 puan kaybeder. ~~
~~ 4. olan oyuncu 1 puan kaybeder. ~~

~~ El bozulursa puanlar değişmez. El bitmeden ayrılan bir oyuncu 2 puan kaybeder. ~~


Masaya katilan her oyuncudan el sayisi kadar puan dusulur. Masa sonunda birinci olan oyuncu toplam puanlarin %50 sini, ikinci olan oyuncu %25'ini, ucuncu olan oyuncu %15'ini, dorduncu olan oyuncu %10'unu alir. Masa bitmeden ayrilan oyuncu puan alamaz.

Ornegin 10 ellik bir oyunda oyuncularin puanlari 1500 olsun.

Masaya katildiklarinda puanlari 1490 olur. Ortada toplam 40 puan vardir.

Birinci olan oyuncu 20 puan alir ve 1510 olur.
Ikinci olan oyuncu 10 puan alir ve 1500 olur.
Ucuncu olan oyuncu 6 puan alir ve 1496 olur.
Dorduncu olan oyuncu 4 puan alir ve 1494 olur.
