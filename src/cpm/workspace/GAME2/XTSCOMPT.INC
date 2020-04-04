{ Xts Yatl Compatibility include }

type xStr = string[255];

procedure runPrg(str: xStr);
 var f : File;
begin
   str := concat( str, '.COM' );
   Assign(f, str);
   Execute( f );
end;



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
