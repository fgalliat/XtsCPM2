program TestRecord;

{$I XTSCOMPAT.INC} {PC}
{ $I XTSCOMPT.INC} {YATL}

{ Xtase TestRecord v1 @Mar 2020 }

{$I GAME_CL.INC} { classes }

{$I GAME_UI.INC}
{$I GAME_FS.INC}
{$I GAME_MM.INC}

Var
  player  : Character;
  curItem : Item;
  i : Integer;
  attMod, defMod : Byte; { @ before battle }
  {itemAvail : Array[1..4] of Item; }
  itemAvail : itmAry;
  ok : boolean; { fctResult MUST be read in TP3 }

Begin

  InitItem( itemAvail[1], 'Sword of Moon', 6, 1);
  InitItem( itemAvail[2], 'Sword of Sun' , 7, 0);
  InitItem( itemAvail[3], 'Def rune', 0, 2);
  InitItem( itemAvail[4], 'Att rune', 6, 0);
  SaveItems( itemAvail );

  if ( not ReadPerso(player) ) then
  begin
    writeln('NO PLAYER IN FILE');
    InitPerso( player, 'Bishop' );
    player.hp   := 12;
    player.gp   := 100;
    player.att  := 12;
    player.def  := 12;
  end;

  player.inventory[1] := itemAvail[1];
  player.inventory[1].active := true;
  player.inventory[2] := itemAvail[3];
  player.inventory[2].active := true;
  DispPerso( player );

  { backup file - write }
  writeln('begin of write');
  SavePerso(player);
  writeln('end of write--------');

  player.name := 'Rolph mod';
  player.inventory[1].name := 'Soap ball';
  DispPerso( player );

  { backup file - read }
  writeln('begin of read');
  ok := ReadPerso( player );
  writeln('end of read--------');
  DispPerso( player );

  write('Press Enter.'); readln;
  MainMenu(player);

  { end of console app }
  write('Bye.'); readln;
End.
