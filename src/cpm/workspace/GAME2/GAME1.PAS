program Game1;

{ Xtase YATL AdventureGame @Jul 2019 }

var 
 i : integer;

{$I XTSYATLG.INC the GFX include}
{$I XTSYATLM.INC the SFX include}
{$I GAME_R.INC the game routines}
{$I GAME_S.INC the game generated scenes}

const
 nbTruc = 4;

var
 pack : string[8];
 choice : string[32];
 tmp: string[16];
 zz,err : integer;
 curPlace : integer;
 curMusic : integer;

begin
  initScenes;
  curPlace := 1;
  choice := '*';
  curMusic := -1;

  stopMp3;
  volMp3(10);

  {* ------ *}

  repeat
    clrscr;
    drawPakImage(pakNam[ curPlace ], pakNum[ curPlace ], 0, 0);

    { isPlaying() + sameTrackNum }
    if ( musNum[ curPlace ] <> curMusic ) or not ( doesMp3Play ) then
    begin
      curMusic := musNum[ curPlace ];
      playMp3( curMusic );
    end;

    gotoXY(0,0);
    writeln( placeNam[ curPlace ] );
    writeln( txt[ curPlace ] );
    writeln( caract[ curPlace ] );

    parseCapas( capas[ curPlace ] );
    { icap : nbOfChoices }
    { lcap : choiceLabel }
    
    for i:=1 to icap do
    begin
      writeln( chr(97 + i -1)+'. @ '+lcap[i] );
    end;

    
    write( 'Your choice ? ' );
    readln(choice);

    { if 1 dest : any key will be ok }
    i := 0;
    if ( length(choice) >= 1 ) then
      i := getChoiceNum(choice);
    if (i >= 1) and (i <= icap) then 
    begin
      curPlace := acap[i];
    end
    else if ( icap = 1 ) then
      curPlace := acap[1];

  until ( choice = 'q' );

  stopMp3;
end.
