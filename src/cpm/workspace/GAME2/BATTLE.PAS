program Battle;

{$I XTSCOMPAT.INC} {PC}
{ $I XTSCOMPT.INC} {YATL}

{ Xtase BattleSystem v1 @Mar 2020 }

{$I GAME_CL.INC} { classes }

{$I GAME_UI.INC} { UI routines }
{$I GAME_FS.INC} { Storage routines }
{$I GAME_MM.INC} { Main Menu routines }

Var
  player  : Character;
  curItem : Item;
  i : Integer;
  attMod, defMod : Byte; { @ before battle }
  {itemAvail : Array[1..4] of Item; }
  itemAvail : itmAry;
  ok : boolean; { fctResult MUST be read in TP3 }

Begin
  ClrScr;

  if ( not ReadItems(itemAvail) ) then
  begin
    writeln('Can''t read items !');
    writeln('run TESTREC.COM');
    exit;
  end;
  DispItems( itemAvail );

  if ( not ReadPerso(player) ) then
  begin
    writeln('Can''t read player !');
    writeln('run TESTREC.COM');
    exit;
  end;
  DispPerso( player );

  { end of console app }
  write('Bye.'); readln;
End.
