{ Game FS include @Mar 2020 Xtase-fgalliat }

const fileName = 'player.dat';
const fileNameITM = 'items.dat';

procedure SavePerso(perso: Character);
Var
  pBackup : file of Character;
begin
  Assign(pBackup, fileName);
  Rewrite(pBackup);
  Write( pBackup, perso );
  Close(pBackup);
end;

function ReadPerso(var perso: Character) : boolean;
 var
   pBackup : file of Character;
   exists: boolean;
begin
  Assign(pBackup,fileName);
  {$I-} { no fail on error }
  reset(pBackup);
  {$I+} { fail back on error }
  exists := (IOresult = 0);
  if (not exists) or ( eof(pBackup) ) then
   begin
    ReadPerso := false;
   end
  else
    begin
      read(pBackup, perso);
      ReadPerso := true;
    end;
  close(pBackup);
end;

procedure SaveItems(var itms: itmAry);
Var
  pItems : file of Item;
  i : integer;
begin
  Assign(pItems, fileNameITM);
  Rewrite(pItems);
  for i := 1 to ALLITEM_SIZE do
    Write( pItems, itms[i] );
  Close(pItems);
end;
