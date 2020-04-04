program TestRecord;
{$I XTSCOMPT.INC} {compat.}

{ Xtase TestRecord v1 @Mar 2020 }

{$I GAME_CL.INC} { classes }

{$I GAME_UI.INC} { UI routines }
{$I GAME_FS.INC} { Storage routines }
{$I GAME_MM.INC} { Main Menu routines }

Var
  player  : Character;
  curItem : Item;
  i : Integer;
  itemAvail : itmAry;
  monsters : mnstAry;
  ok : boolean; { fctResult MUST be read in TP3 }

Begin

  if ( not ReadItems(itemAvail) ) then
  begin
    InitItem( itemAvail[1], 'Sword of Moon', 6, 1);
    InitItem( itemAvail[2], 'Sword of Sun' , 7, 0);
    InitItem( itemAvail[3], 'Def rune',      0, 2);
    InitItem( itemAvail[4], 'Att rune',      6, 0);
    SaveItems( itemAvail );
  end;
  DispItems( itemAvail );

  if ( not ReadMonsters(monsters) ) then
  begin
    InitMonster( monsters[1], 'Lizard',  1, 10,   6,  6);
    InitMonster( monsters[2], 'Gobelin', 1, 10,   7,  3);
    InitMonster( monsters[3], 'Orc',     1, 15,   9,  8);
    InitMonster( monsters[4], 'Dragon',  3, 50, 128, 64);
    SaveMonsters( monsters );
  end;
  DispMonsters( monsters );

  if ( not ReadPerso(player) ) then
  begin
    writeln('NO PLAYER IN FILE');
    InitPerso( player, 'Bishop' );
    player.hp   := 12;
    player.gp   := 100;
    player.att  := 12;
    player.def  := 12;
    
    player.inventory[1] := itemAvail[1];
    player.inventory[1].active := true;
    player.inventory[2] := itemAvail[3];
    player.inventory[2].active := true;
    SavePerso(player);
  end;
  DispPerso( player );

  {
  player.name := 'Rolph mod';
  player.inventory[1].name := 'Soap ball';
  DispPerso( player );

  writeln('begin of read');
  ok := ReadPerso( player );
  writeln('end of read--------');
  DispPerso( player );
  }
  write('Press Enter.'); readln;
  ClrScr;
  MainMenu(player);

  { end of console app }
  write('Bye.'); readln;
End.
