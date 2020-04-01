{ Game Classes include @Mar 2020 Xtase-fgalliat }

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

  Monster = Record
    name     : string[16];
    hp, gpMax: Integer;
    lvl      : Byte;
    att, def : Byte;
  end;


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

procedure InitItem(var itm: Item; name: mStr; att,def: byte);
begin
  itm.name := name;
  itm.active := false;
  itm.att := att;
  itm.def := def;
end;
