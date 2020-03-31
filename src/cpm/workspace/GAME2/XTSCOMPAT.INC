{ Xts PC Compatibility include }
uses Crt;

type xStr = string[255];

function Bdos(reg:integer; value:word): integer;
begin
 Bdos := 1;
end;
{ re-write of addr()->void_ptr }
function addr(var str:xStr): word;
begin
 addr := 123;
end;
