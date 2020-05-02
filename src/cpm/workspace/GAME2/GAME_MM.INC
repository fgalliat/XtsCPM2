{ Game MainMenu include @Mar 2020 Xtase-fgalliat }

procedure MainMenu(var perso : Character);
  var
    choice : integer;
    ok : boolean;
begin
  choice := -1;
  repeat
   { ClrScr; }
   DispPersoSide(perso);

   GotoXY(1,1);

   writeln(' -- Main Menu --');
   writeln('1. Save Player');
   writeln('2. Load Player');
   writeln('3. Init Files');
   writeln('4. Start Battle');
   writeln('');
   writeln('0. Exit Menu');
   write('Choice ? '); readln( choice );

   if ( choice = 1 ) then
    begin
      SavePerso(perso);
    end
   else if ( choice = 2 ) then
    begin
      ok := ReadPerso(perso);
    end
   else if ( choice = 3 ) then
    begin
      runPRG( 'TESTREC' );
    end
   else if ( choice = 4 ) then
    begin
      runPRG( 'BATTLE' );
    end;
  until ( choice = 0 );

  { writeln('Bye'); readln; }
end;


