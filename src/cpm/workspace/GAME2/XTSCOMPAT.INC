{ Xts PC Compatibility include }
uses Crt;

type xStr = string[255];

procedure runPrg(str: xStr);
begin
   str := concat( str, '.EXE' );
   writeln( 'Exec : ', str, ' (NYI)' );
   readln;
end;


function Bdos(reg:integer; value:word): integer;
begin
 Bdos := 1;
end;
{ re-write of addr()->void_ptr }
function addr(var str:xStr): word;
begin
 addr := 123;
end;

procedure Execute(var f : File);
begin
  writeln('ASK TO LAUNCH A .COM !!!');
  write('Press Enter');
  readln;
end;

