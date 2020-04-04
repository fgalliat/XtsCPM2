Program TestPC1;

uses Crt, Windows, SysUtils;

type mStr = string[255];

procedure runPRG(prgName: mStr);
  var finStr : mStr;
      pStr : pchar;

      {AProcess: TProcess;}
begin
  finStr := concat(prgName,'.EXE');
  pStr := strAlloc( length(finStr)+1 );
  strPcopy( pStr, finStr );
  { WinExec( pStr ,SW_NORMAL); } { NON BLOCKING }
  { ShellExecute( 0, nil, pStr, nil, nil, SW_NORMAL); } { Start a new Win }
    {
   AProcess := TProcess.Create(nil);
   AProcess.CommandLine := pStr;
   AProcess.Options := AProcess.Options + [poWaitOnExit];
   AProcess.Execute;
   AProcess.Free;
     }
    {ShellExecute( 0, nil, pStr, nil, nil, SW_HIDE);}
    {Execute('MENU.EXE');}
    ShellExecute( 0, nil, pStr, nil, nil, SW_SHOW);
end;

{$macro on}
begin

  { writeln( FPC_FULLVERSION ); }

  runPRG('MENU');
end.
