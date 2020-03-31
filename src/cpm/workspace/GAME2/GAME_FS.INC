{ Game FS include @Mar 2020 Xtase-fgalliat }

{type mStr = string[255];}

const fileName = 'player.dat';

procedure SavePerso(perso: Character);
Var
  pBackup : file of Character;
begin
  Assign(pBackup, fileName);
  Rewrite(pBackup);
  Write( pBackup, perso );
  Close(pBackup);
end;

procedure ReadPerso(var perso: Character);
 var
   pBackup : file of Character;
begin
  Assign(pBackup,fileName);
  reset(pBackup);
  { beware w/ while ... }
  { while not eof(pBackup) do
  begin
      read(pBackup, perso);
  end; }
  read(pBackup, perso);
  close(pBackup);
end;
