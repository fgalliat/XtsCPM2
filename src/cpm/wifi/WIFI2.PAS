{ Wifi2 Test w/ memXchange }
{ Xtase @Mar 2020 }
Program Wifi2;

type aStr = string[255];

procedure sendToRam(text : aStr);
begin
 Bdos(229, addr(text) );
end;

procedure getIp;
begin
  Bdos(228, (65 shl 8) + 0 );
end;

var
  memX: string[255];
begin
  memX := 'Coucou';
  writeln( memX );
  {sendToRam( memX );}
  Bdos(229, addr(memX) );

  getIp;
  writeln( memX );
end.
