program BattleV1; { prgm must have #name than fcts }

{$I XTSCOMPAT.INC} {PC}
{ $I XTSCOMPT.INC} {YATL}

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
  { TODO : better }
  writeln( ') Monster strikes' );
  damage := RANDOM( monster.att );
  { else as a Byte : make 255-damage }
  if ( damage > defMod ) then damage := damage - defMod
  else damage := 0;

  if ( damage = 0 ) then
   begin
     writeln( ' ) He missed you.' );
   end
  else
     writeln( ' ) He hurts you by ', damage, 'HP' );
  perso.hp := perso.hp - damage;
end;

procedure playerStrike(var perso: Character; var monster: Monster; attMod, defMod : Byte);
begin
  { TODO : better }
  monster.hp := monster.hp - RANDOM( attMod );
end;

function battle(var perso: Character; var monster: Monster): boolean;
 var
   attMod, defMod : byte;
   i : Integer;
   curItem: Item;
   mnstStrk1st : boolean;
begin
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

  writeln('ATT: ', attMod, ' DEF: ', defMod);
  write('You will fight : '); DispMonster(monster);
  mnstStrk1st := false;
  if ( monster.lvl > perso.lvl ) then
    mnstStrk1st := true
  else
    if ( random(6) > 3 ) then
      mnstStrk1st := true;

  if ( mnstStrk1st ) then
    monsterStrike(monster, perso, attMod, defMod);

  repeat
    playerStrike(perso, monster, attMod, defMod);
    if ( monster.hp > 0 ) then
      monsterStrike(monster, perso, attMod, defMod);
  until (perso.hp <= 0) or (monster.hp <= 0);

  battle := (perso.hp > 0);
end;

const BUILDER_MSG = 'run TESTREC.COM';

Var
  player  : Character;
  curItem : Item;
  i : Integer;
  attMod, defMod : Byte; { @ before battle }
  {itemAvail : Array[1..4] of Item; }
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

  { DispItems( itemAvail ); }
  DispPerso( player );

  i := meetMonster( player, monsters );
  ok := battle( player, monsters[i] );
  DispPerso( player );

  { end of console app }
  write('Bye.'); readln;
End.
