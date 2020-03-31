{ Game UI include @Mar 2020 Xtase-fgalliat }

type mStr = string[255];

procedure InitPerso(var perso: Character; name: mStr);
 var i:integer;
begin
  perso.name := name;
  for i := 1 to BACKPAK_SIZE do
  begin
    perso.inventory[i].active := false;
  end;
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
