{ Xts Yatl Compatibility include }
{ uses Crt; }

{
function Bdos(reg:integer; value:word): integer;
begin
 Bdos := 1;
end;
}
{ re-write of addr()->void_ptr }
{
function addr(var str:mStr): word;
begin
 addr := 123;
end;
}
