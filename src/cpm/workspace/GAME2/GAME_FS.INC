{ Game FS include @Mar 2020 Xtase-fgalliat }

{type mStr = string[255];}

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

{
procedure SaveItem(itm: Item; erase: boolean);
Var
  pItems : file of Item;
begin
  Assign(pItems, fileNameITM);
  if (erase) then Rewrite(pItems);
  Write( pItems, itm );
  Close(pItems);
end;
}
procedure SaveItems(itms: Array of Item; nbItm: integer);
Var
  pItems : file of Item;
  i : integer;
begin
  Assign(pItems, fileNameITM);
  Rewrite(pItems);
  for i := 1 to nbItm do
    Write( pItems, itms[i] );
  Close(pItems);
end;
