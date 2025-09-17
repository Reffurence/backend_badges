grammar Expr;

@header {
   package net.foxboi.mlem.parser;
}

INT: [0-9]+;
FLOAT: [0-9]+ ([eE][-+]?[0-9]+)
     | [0-9]+ '.' ([eE][-+]?[0-9]+)?
     | '.' [0-9]+ ([eE][-+]?[0-9]+)?
     | [0-9]+ '.' [0-9]+ ([eE][-+]?[0-9]+)?;
VAR: '$' [a-zA-Z_0-9]+;
COL: '#' [a-fA-F0-9]+;
IDENT: [a-zA-Z_][a-zA-Z_0-9]*;

fragment STR_CHAR: '\\' 'u' [a-fA-F0-9] [a-fA-F0-9] [a-fA-F0-9] [a-fA-F0-9]
                 | '\\' (~'u')
                 | (~'\\');

STR: '\'' STR_CHAR*? '\''
   | '"' STR_CHAR*? '"';

WS: [\r\n\t ]+ -> skip;

number: v=(INT|FLOAT) IDENT  #unitNumber
      | v=(INT|FLOAT) '%'    #percentNumber
      | FLOAT                #pureNumber;

expr: '(' expr ')'                                                      #parExpr
    | op=('+'|'-'|'!'|'~') rhs=expr                                     #unaryExpr
    | lhs=expr op=('*'|'/'|'%') rhs=expr                                #mulExpr
    | lhs=expr op=('+'|'-') rhs=expr                                    #addExpr
    | lhs=expr op=('<<'|'>>') rhs=expr                                  #shiftExpr
    | lhs=expr op=('<='|'>='|'<'|'>') rhs=expr                          #cmpExpr
    | lhs=expr op=('=='|'==='|'!='|'!==') rhs=expr                      #eqExpr
    | lhs=expr ('&') rhs=expr                                           #bitAndExpr
    | lhs=expr ('|') rhs=expr                                           #bitOrExpr
    | lhs=expr ('^') rhs=expr                                           #bitXorExpr
    | lhs=expr ('&&') rhs=expr                                          #logAndExpr
    | lhs=expr ('||') rhs=expr                                          #logOrExpr
    | lhs=expr ('^^') rhs=expr                                          #logXorExpr
    | 'if' cond=expr 'then' yes=expr 'else' no=expr                     #condExpr
    | number                                                            #numExpr
    | INT                                                               #intExpr
    | name=IDENT '(' (args+=expr (',' args+=expr) * ','?)? ')'          #callExpr
    | var=VAR                                                           #varExpr
    | col=COL                                                           #colExpr
    | str=STR                                                           #strExpr
    | val=('true'|'false'|'null'|'nan'|'inf')                           #constExpr
    ;

input: expr EOF;