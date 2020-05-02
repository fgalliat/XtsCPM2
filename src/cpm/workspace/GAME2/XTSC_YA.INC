{ Xts Yatl Compatibility include }

type xStr = string[255];

procedure runPrg(str: xStr);
 var f : File;
begin
   str := concat( str, '.COM' );
   Assign(f, str);
   Execute( f );
end;
