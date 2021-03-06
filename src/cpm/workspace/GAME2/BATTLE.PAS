program BattleV1; { prgm must have #name than fcts }
{$I XTSCOMPT.INC} {compat.}

{ Xtase BattleSystem v1 @Mar 2020 }

{$I GAME_CL.INC} { classes }

{$I GAME_UI.INC} { UI routines }
{$I GAME_FS.INC} { Storage routines }
{$I GAME_MM.INC} { Main Menu routines }


function meetMonster(var perso: Character; var monsters: mnstAry): integer;
 var
   found, i : integer;
   curMonster: Monster;
   pLvl : integer;
begin
  { use perso to determin his level .. }
  pLvl := perso.lvl;
  found := 0;
  repeat
     i := RANDOM( ALLMONSTER_SIZE ) + 1;
     curMonster := monsters[i];
     if ( curMonster.active ) then
     begin
       { find an active monster }
       if ( curMonster.lvl >= pLvl-3 ) and ( curMonster.lvl <= pLvl+3 ) then
       begin
         { find a playerLvl friendly Monster }
         found := i;
       end;
     end;
  until (found > 0);
  meetMonster := found;
end;

procedure monsterStrike(var monster: Monster; var perso: Character; attMod, defMod : Byte);
var damage: byte;
begin
  ClrEol;
  writeln( ') Monster strikes' );
  damage := RANDOM( monster.att );

  { critical hit }
  if ( RANDOM(50) > 35 ) then
    damage := damage * 2;

  { else as a Byte : make 255-damage }
  if ( damage > defMod ) then damage := damage - defMod
  else damage := 0;

  if ( damage = 0 ) then
   begin
     ClrEol;
     writeln( ' ) He missed you.' );
   end
  else
   begin
     ClrEol;
     writeln( ' ) He hurts you by ', damage, 'HP' );
   end;
  perso.hp := perso.hp - damage;
end;

procedure playerStrike(var perso: Character; var monster: Monster; attMod, defMod : Byte);
var damage: byte;
begin
  ClrEol;
  writeln( ') Player strikes' );
  damage := RANDOM( attMod );

  { critical hit }
  if ( RANDOM(50) > 35 ) then
    damage := damage * 2;

  { else as a Byte : make 255-damage }
  if ( damage > monster.def ) then damage := damage - monster.def
  else damage := 0;

  if ( damage = 0 ) then
   begin
     ClrEol;
     writeln( ' ) You missed him.' );
   end
  else
   begin
     ClrEol;
     writeln( ' ) You hurts him by ', damage, 'HP' );
   end;

  monster.hp := monster.hp - damage;
end;

function battle(var perso: Character; var monster: Monster): boolean;
 var
   attMod, defMod : byte;
   i : Integer;
   curItem: Item;
   mnstStrk1st : boolean;
begin
  { init }
  monster.hp := monster.hpMax;
  { compute player mods. }
  attMod := perso.att;
  defMod := perso.def;
  for i := 1 to BACKPAK_SIZE do
  begin
    curItem := perso.inventory[i];
    if curItem.active then
     begin
      attMod := attMod + curItem.att;
      defMod := defMod + curItem.def;
     end;
  end;
  ClrEol;
  writeln('ATT: ', attMod, ' DEF: ', defMod);

  DispPersoSide(perso); GotoXY(1,9);
  ClrEol;
  writeln('You will fight : '); 
  ClrEol;
  DispMonster(monster);
  ClrEol;
  
  mnstStrk1st := false;
  if ( monster.lvl > perso.lvl ) then
    mnstStrk1st := true
  else
    if ( random(6) > 4 ) then
      mnstStrk1st := true;

  if ( mnstStrk1st ) then
    monsterStrike(monster, perso, attMod, defMod);

  repeat
    DispPersoSide(perso); GotoXY(1,11);
    ClrEol;

    playerStrike(perso, monster, attMod, defMod);
    if ( monster.hp > 0 ) then
      monsterStrike(monster, perso, attMod, defMod);

    DispPersoSide(perso); GotoXY(1,11);
    ClrEol;
    readln;
  until (perso.hp <= 0) or (monster.hp <= 0);

  if (perso.hp > 0) then
  begin
    perso.exp := perso.exp + monster.expMax;
    { level Up ?? }
    perso.gp := perso.gp + monster.gpMax;
    if (perso.gp > perso.gpMax) then perso.gp := perso.gpMax;
  end;

  if ( perso.hp < 0 ) then
    perso.hp := 0;

  DispPersoSide(perso); GotoXY(1,11);
  ClrEol;

  battle := (perso.hp > 0);
end;

const BUILDER_MSG = 'run TESTREC.COM';

Var
  player  : Character;
  curItem : Item;
  i : Integer;
  itemAvail : itmAry;
  ok : boolean; { fctResult MUST be read in TP3 }
  monsters : mnstAry;
Begin
  ClrScr;
  Randomize;

  if ( not ReadItems(itemAvail) ) then
  begin
    writeln('Can''t read items !');
    writeln(BUILDER_MSG);
    exit;
  end;

  if ( not ReadPerso(player) ) then
  begin
    writeln('Can''t read player !');
    writeln(BUILDER_MSG);
    exit;
  end;

  if ( not ReadMonsters(monsters) ) then
  begin
    writeln('Can''t read monsters !');
    writeln(BUILDER_MSG);
    exit;
  end;

  DispPerso( player );
  if ( player.hp <= 0 ) then
  begin
    writeln('You are DEAD ...');
    exit;
  end;  

  i := meetMonster( player, monsters );
  ok := battle( player, monsters[i] );

  GotoXY(1, 1); ClrEol;
  if ( not ok ) then
    writeln('You are DEAD ...')
  else
    DispPersoSide( player );

  SavePerso( player );

  GotoXY(1,11); ClrEol;

  { end of console app }
  write('Bye.'); readln;
End.
