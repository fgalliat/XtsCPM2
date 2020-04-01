{ Game FS include @Mar 2020 Xtase-fgalliat }

const fileName = 'PLAYER.DAT';
const fileNameITM = 'ITEMS.DAT';
const fileNameMST = 'MONSTERS.DAT';

{ ------- Player ------- }
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

{ ------- Items ------- }
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

function ReadItems(var itms: itmAry) : boolean;
 var
   pItems : file of Item;
   exists: boolean;
   curItm: Item;
   i : integer;
begin
  for i := 1 to ALLITEM_SIZE do
    itms[i].active := false;

  Assign(pItems,fileNameITM);
  {$I-} { no fail on error }
  reset(pItems);
  {$I+} { fail back on error }
  exists := (IOresult = 0);
  if (not exists) or ( eof(pItems) ) then
   begin
    ReadItems := false;
   end
  else
    begin
      i := 1;
      repeat
        seek(pItems, i-1 );
        read(pItems, curItm);
        itms[i] := curItm;
        i := i + 1;
      until ( eof(pItems) );
      ReadItems := true;
    end;
  close(pItems);
end;

{ ------- Monster ------- }
procedure SaveMonsters(var mnstr: mnstAry);
Var
  f : file of Monster;
  i : integer;
begin
  Assign(f, fileNameMST);
  Rewrite(f);
  for i := 1 to ALLMONSTER_SIZE do
    Write( f, mnstr[i] );
  Close(f);
end;

function ReadMonsters(var mnstr: mnstAry) : boolean;
 var
   f : file of Monster;
   exists: boolean;
   curItm: Monster;
   i : integer;
begin
  for i := 1 to ALLMONSTER_SIZE do
    mnstr[i].active := false;

  Assign(f,fileNameMST);
  {$I-} { no fail on error }
  reset(f);
  {$I+} { fail back on error }
  exists := (IOresult = 0);
  if (not exists) or ( eof(f) ) then
   begin
    ReadMonsters := false;
   end
  else
    begin
      i := 1;
      repeat
        seek(f, i-1 );
        read(f, curItm);
        mnstr[i] := curItm;
        i := i + 1;
      until ( eof(f) );
      ReadMonsters := true;
    end;
  close(f);
end;
