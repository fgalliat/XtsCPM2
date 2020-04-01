program BattleV1; { prgm must have #name than fcts }

{$I XTSCOMPAT.INC} {PC}
{ $I XTSCOMPT.INC} {YATL}

{ Xtase BattleSystem v1 @Mar 2020 }

{$I GAME_CL.INC} { classes }

{$I GAME_UI.INC} { UI routines }
{$I GAME_FS.INC} { Storage routines }
{$I GAME_MM.INC} { Main Menu routines }

function battle(var perso: Character): boolean;
 var
   attMod, defMod : byte;
   i : Integer;
   curItem: Item;
begin
  { compute player mods. }
  attMod := perso.att;
  defMod := perso.def;
  for i := 1 to BACKPAK_SIZE do
  begin
    curItem := perso.inventory[i];
    if curItem.active then
     begin
      attMod := attMod + curItem.att;
      defMod := defMod + curItem.def;
     end;
  end;

  writeln('ATT: ', attMod, ' DEF: ', defMod);

  battle := true;
end;

const BUILDER_MSG = 'run TESTREC.COM';

Var
  player  : Character;
  curItem : Item;
  i : Integer;
  attMod, defMod : Byte; { @ before battle }
  {itemAvail : Array[1..4] of Item; }
  itemAvail : itmAry;
  ok : boolean; { fctResult MUST be read in TP3 }
  monsters : mnstAry;
Begin
  ClrScr;

  if ( not ReadItems(itemAvail) ) then
  begin
    writeln('Can''t read items !');
    writeln(BUILDER_MSG);
    exit;
  end;

  if ( not ReadPerso(player) ) then
  begin
    writeln('Can''t read player !');
    writeln(BUILDER_MSG);
    exit;
  end;

  if ( not ReadMonsters(monsters) ) then
  begin
    writeln('Can''t read monsters !');
    writeln(BUILDER_MSG);
    exit;
  end;

  DispItems( itemAvail );
  DispPerso( player );

  ok := battle( player );
  DispPerso( player );

  { end of console app }
  write('Bye.'); readln;
End.
