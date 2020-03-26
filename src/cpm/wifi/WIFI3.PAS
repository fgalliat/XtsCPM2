{ Wifi3 Test w/ memXchange }
{ Xtase @Mar 2020 }
Program Wifi3;

type aStr = string[255];

{var -> pass as ref}
{ else addr use a copy value }
procedure sendToRam(var text : aStr);
begin
 Bdos(229, addr(text) );
end;

procedure fixMemXSize(len: integer; var seg : aStr);
begin
  fillchar(seg,len+1,'_');
  seg[0]:=chr(len);
end;

procedure getIp;
begin
  Bdos(228, (65 shl 8) + 0 );
end;

var
  memX: string[255];
begin
  fixMemXSize(20, memX);
  writeln( memX );
  sendToRam( memX );

  getIp;
  writeln( memX );
end.
