program TestRecord;

{$I XTSCOMPAT.INC} {PC}
{ $I XTSCOMPT.INC} {YATL}

{ Xtase TestRecord v1 @Mar 2020 }

const
  BACKPAK_SIZE = 5;

Type 
  Item = Record
    name     : string[16];
    active   : Boolean; { 'cause pre-allocated }
    att, def : Byte;
  end;

  Character = Record
    name     : string[16];
    hp, gp   : Integer;
    lvl      : Byte;
    att, def : Byte;

    inventory: Array[1..BACKPAK_SIZE] of Item;
  end;

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

  player.inventory[1].active := true;
  player.inventory[1].name := 'Sword of Dragon';
  player.inventory[1].att := 5;
  player.inventory[1].def := 0;

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

  MainMenu(player);

  { end of console app }
  write('Bye.'); readln;
End.
