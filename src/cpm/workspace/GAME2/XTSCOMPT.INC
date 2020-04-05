{PC}
{ $I XTSC_PC.INC}
{YATL}
{ $I XTSC_YA.INC the Yatl include}

{ Yatl CP/M doesn't support 2Lvl .inc ? }

type xStr = string[255];

procedure runPrg(str: xStr);
 var f : File;
begin
   str := concat( str, '.COM' );
   Assign(f, str);
   Execute( f );
end;
