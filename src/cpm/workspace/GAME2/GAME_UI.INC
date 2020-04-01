{ Game UI include @Mar 2020 Xtase-fgalliat }

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
      writeln( ' Item#', i, ' ', curItem.name, ' ATT:', curItem.att, ' DEF:', curItem.def );
     end;
  end;
  writeln('-eol-');
end;
