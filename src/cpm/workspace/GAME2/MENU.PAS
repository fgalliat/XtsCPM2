program MenuV1; { prgm must have #name than fcts }
{$I XTSCOMPT.INC} {compat.}

{ Xtase MenuSystem v1 @Apr 2020 }

{$I GAME_CL.INC} { classes }

{$I GAME_UI.INC} { UI routines }
{$I GAME_FS.INC} { Storage routines }
{$I GAME_MM.INC} { Main Menu routines }

var
  player: Character;
  ok : boolean;
begin
  ClrScr;
  ok := ReadPerso(player);
  if ( not ok ) then
    InitPerso(player, 'Rolph', 12);

  DispPersoSide(player);
  MainMenu(player);
end.
