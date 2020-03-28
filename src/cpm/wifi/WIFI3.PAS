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

procedure getIp; {use memX}
begin
  Bdos(228, (65 shl 8) + 0 );
end;
procedure getSSID; {use memX}
begin
  Bdos(228, (66 shl 8) + 0 );
end;
procedure getKnownAP; {use memX} { chr(10) sepa }
begin
  Bdos(228, (68 shl 8) + 0 );
end;
function connectAp(conf: integer) : integer;
begin
  connectAp := Bdos(228, (67 shl 8) + conf );
end;
function isAtHome : integer;
begin
  isAtHome := Bdos(228, (71 shl 8) + 0 );
end;

{===========}
function strCopy(var src: aStr) : aStr;
begin
  strCopy := copy( src, 1, length(src) );
end;
{===========}

var
  memX: string[255];
  confNum: integer;
  ok: integer;

  ip: string[17];
  wgetReq: string[255];
begin
  fixMemXSize(20, memX);
  sendToRam( memX );

  write('IP   : '); getIp; writeln( memX ); 
  ip := strCopy( memX );
  write('SSID : '); getSSID; writeln( memX );
  write('APs  : '); getKnownAP; writeln( memX );

  { getIpStr(..) can't share memX in routine }  
  write('IP was : '); writeln( ip );

  { 8000 if @Home / 8090 else }
  {           123456789 }
  wgetReq := '$home;8090;sensors;/sensors/sensor/1';
  wgetReq := '$home;8090;sensors;/rss/titles/1/arduino';
  if ( isAtHome = 1 ) then
    wgetReq[9] := '0';
  writeln( wgetReq );

  write('ConnectTo  : '); readln(confNum);
  if ( confNum < 0) then exit;
  ok := connectAp( confNum );
  writeln('connected : ', ok);

  


end.
