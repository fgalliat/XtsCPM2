{ Game Classes include @Mar 2020 Xtase-fgalliat }

const
  BACKPAK_SIZE = 5;
  ALLITEM_SIZE = 12;
  ALLMONSTER_SIZE = 12;

Type 
  Item = Record
    name     : string[16];
    active   : Boolean; { 'cause pre-allocated }
    att, def : Byte;
  end;

  Character = Record
    name      : string[16];
    hp, hpMax : Integer;
    lvl       : Byte;
    exp       : Integer;
    att, def  : Byte;
    gp, gpMax : Integer;
    inventory : Array[1..BACKPAK_SIZE] of Item;
  end;

  Monster = Record
    name      : string[16];
    active    : Boolean;
    hp, hpMax : Integer;
    lvl       : Byte;
    att, def  : Byte;
    expMax    : integer;
    gpMax     : Integer;
  end;


type mStr   = string[255];
type itmAry = Array[1..ALLITEM_SIZE] of Item;
type mnstAry = Array[1..ALLMONSTER_SIZE] of Monster;

procedure InitPerso(var perso: Character; name: mStr; hp:Integer);
 var i:integer;
begin
  perso.name := name;
  perso.hp := hp;
  perso.hpMax := hp;
  perso.gpMax := 1000;
  perso.gp    := 500;
  perso.exp   := 0;
  perso.lvl   := 1;
  for i := 1 to BACKPAK_SIZE do
  begin
    perso.inventory[i].active := false;
  end;
end;

procedure InitItem(var itm: Item; name: mStr; att,def: byte);
begin
  itm.name := name;
  { itm.active := false; }
  itm.active := true;
  itm.att := att;
  itm.def := def;
end;

procedure InitMonster(var monster: Monster; name: mStr; lvl,hp,att,def: Byte);
 var i:integer;
begin
  monster.name := name;
  monster.active := true;
  monster.hp := hp;
  monster.att := att;
  monster.def := def;
  monster.lvl := lvl;
end;
