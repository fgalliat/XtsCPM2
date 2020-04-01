{ Game MainMenu include @Mar 2020 Xtase-fgalliat }

procedure MainMenu(var perso : Character);
  var
    choice : integer;
begin
  choice := -1;
  repeat
   ClrScr;
   writeln(' -- Main Menu --');
   writeln('1. Save Player');
   writeln('2. Load Player');
   writeln('3. Disp Player');
   writeln('');
   writeln('0. Exit Menu');
   write('Choice ? '); readln( choice );

   if ( choice = 1 ) then
    begin
      SavePerso(perso);
    end
   else if ( choice = 2 ) then
    begin
      ReadPerso(perso);
    end
   else if ( choice = 3 ) then
    begin
      DispPerso(perso);
      readln;
    end;
  until ( choice = 0 );

  { writeln('Bye'); readln; }
end;


