{ Game MainMenu include @Mar 2020 Xtase-fgalliat }

procedure MainMenu(var perso : Character);
  var
    choice : integer;
    ok : boolean;
    battleF: File;
begin
  choice := -1;
  repeat
   ClrScr;
   writeln(' -- Main Menu --');
   writeln('1. Save Player');
   writeln('2. Load Player');
   writeln('3. Disp Player');
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
      DispPerso(perso);
      readln;
    end
   else if ( choice = 4 ) then
    begin
      Assign( battleF, 'BATTLE.COM' );
      Execute( battleF );
    end;
  until ( choice = 0 );

  { writeln('Bye'); readln; }
end;


