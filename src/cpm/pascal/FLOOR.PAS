function floor (y:real): real;
 Var m:real; 
Begin 
 if y < 0 then m := int(y) - 1 
 else m := int(y); 
 floor := m; 
End;

