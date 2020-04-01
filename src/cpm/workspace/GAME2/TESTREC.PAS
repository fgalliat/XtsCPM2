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

Begin
  InitPerso( player, 'Bishop' );
  player.hp   := 12;
  player.gp   := 100;
  player.att  := 12;
  player.def  := 12;

  InitItem( curItem, 'Sword of Moon', 6, 1);
  player.inventory[1] := curItem;
  player.inventory[1].active := true;
  InitItem( curItem, 'Sword of Sun', 7, 0);
  player.inventory[2] := curItem;
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
  ReadPerso( player );
  writeln('end of read--------');
  DispPerso( player );

  write('Press Enter.'); readln;
  MainMenu(player);

  { end of console app }
  write('Bye.'); readln;
End.
