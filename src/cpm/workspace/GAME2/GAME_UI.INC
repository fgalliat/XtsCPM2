{ Game UI include @Mar 2020 Xtase-fgalliat }

procedure DispItem(itm: Item);
begin
  writeln( 'Item: ', itm.name, ' ATT:', itm.att, ' DEF:', itm.def );
end;

procedure DispItems(var itms: itmAry);
 var
   i : Integer;
   curItem: Item;
begin
  writeln( '=============' );
  for i := 1 to ALLITEM_SIZE do
  begin
    curItem := itms[i];
    if curItem.active then
     begin
      write( ' ', i, ' ' );
      DispItem( curItem );
     end;
  end;
  writeln('-eol-');
end;

procedure DispMonster(monster: Monster);
begin
  writeln( 'Monster: ', monster.name, ' LVL:', monster.lvl, ' ATT:', monster.att, ' DEF:', monster.def );
end;

procedure DispMonsters(var itms: mnstAry);
 var
   i : Integer;
   curItem: Monster;
begin
  writeln( '=============' );
  for i := 1 to ALLMONSTER_SIZE do
  begin
    curItem := itms[i];
    if curItem.active then
     begin
      write( ' ', i, ' ' );
      DispMonster( curItem );
     end;
  end;
  writeln('-eol-');
end;


procedure DispPerso(perso: Character);
 var
   attMod, defMod : byte;
   i : Integer;
   curItem: Item;
begin
  writeln( '----------------' );
  writeln( 'Name : ', perso.name );
  writeln( 'HP   : ', perso.hp );
  writeln( 'GP   : ', perso.gp );

  attMod := perso.att;
  defMod := perso.def;
  for i := 1 to BACKPAK_SIZE do
  begin
    curItem := perso.inventory[i];
    if curItem.active then
     begin
      writeln( ' Item#', i, ' ', curItem.name, ' ATT:', curItem.att, ' DEF:', curItem.def );
      attMod := attMod + curItem.att;
      defMod := defMod + curItem.def;
     end;
  end;
  writeln('-eoi-');
  writeln('ATT: ', attMod, ' DEF: ', defMod);
end;

const SIDE_X = 54;

procedure DispPersoSide(perso: Character);
 var
   attMod, defMod : byte;
   i,y : Integer;
   curItem: Item;
begin
  y := 1;
  GotoXY( SIDE_X, y ); y := y +1;
  writeln( 'o----------------' );
  GotoXY( SIDE_X, y ); y := y +1;
  writeln( '| Name : ', perso.name );
  GotoXY( SIDE_X, y ); y := y +1;
  writeln( '| HP   : ', perso.hp, '/', perso.hpMax, '  ' );
  GotoXY( SIDE_X, y ); y := y +1;
  writeln( '| GP   : ', perso.gp );
  GotoXY( SIDE_X, y ); y := y +1;
  writeln( '|  ' );
  attMod := perso.att;
  defMod := perso.def;
  for i := 1 to BACKPAK_SIZE do
  begin
    curItem := perso.inventory[i];
    if curItem.active then
     begin
      { writeln( ' Item#', i, ' ', curItem.name, ' ATT:', curItem.att, ' DEF:', curItem.def ); }
      attMod := attMod + curItem.att;
      defMod := defMod + curItem.def;
     end;
  end;
  { writeln('-eoi-'); }
  GotoXY( SIDE_X, y ); y := y +1;
  writeln('| ATT: ', attMod, ' DEF: ', defMod);
  GotoXY( SIDE_X, y ); y := y +1;
  writeln( '| XP   : ', perso.exp );
  GotoXY( SIDE_X, y ); y := y +1;
  writeln( '| Level: ', perso.lvl );
  GotoXY( SIDE_X, y ); y := y +1;
  writeln( 'o----------------' );
end;
