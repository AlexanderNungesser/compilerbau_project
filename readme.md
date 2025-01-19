# Compiler Project
*Alexander Nungesser, Marius Rösner, Marie Jünke*

---

## Grammatik
Unsere Grammatik besteht aus den zwei Hauptteilen
**Statements** ``stmt`` und **Expressions** ``expr``.

+ **Statements**: <br/>
Statements sind alle Anweisungen, die allein stehen können.
Sie beinhalten Funktionsdeklarationen und -aufrufe, sowie Bedingungen, 
einfache Blöcke und weiteres.
+ **Expressions**: <br/>
Hier wird gerechnet, verglichen und aufgerufen.

### Besonderheiten
Wir haben sowohl in ``expr`` als auch in ``var_decl`` die im Parser generierten
visit-Methoden für jeden einzelnen Fall aufgebrochen. Dies ermöglicht uns insbesondere
bei ``var_decl`` jeden einzelnen Fall besser zu unterscheiden und individueller darauf zu
reagieren. Dies macht auch den Code besser lesbar.

 
---

## CppParseTreeVisitor
Die einzelnen Knoten sehen wie Folgt aus:

+ Funktionsdeklaration und -implementation:  
  ```
  (FN_DECL)  
    |   |-- (VOID)  
    |   |   '__ functionname (ID)  
    |   |-- (PARAMS)  
    |   '__ x (INT)  
    '__ (BLOCK)
  ```
  + Hierbei sind ``PARAMS`` sowie ``BLOCK`` optional 
    + mit ``BLOCK`` handelt es sich um eine Implementation, ohne um eine Deklaration
  + Bei einer Funktion mit anderem Rückgabetypen als ``void`` wird ein
  ```
    '__ (RETURN)  
    '__ x (ID)              
  ```
  + als letzten Knoten in ``BLOCK`` gehängt.
+ Main
  ````
  (MAIN)
  |-- (INT)
  '__ (BLOCK)
   ````
  + eine besondere Art von einer Funktionsdeklaration.
  + kann keine Argumente enthalten und ist immer vom Typ `INT`
+ Funktionsaufrufe:
  ```
    print_char (FN_CALL)  
    '__ (ARGS)  
    '__ f (CHAR)
  ```
  + Bei keinen Argumenten fällt ``ARGS`` weg
+ Variablen deklarieren:
  ```
    (VAR_DECL)
    '__ b (INT)
  ```
  + Hier können auch Klassen und sonstiges deklariert werden.
  + Array Deklaration sieht beinahe identisch aus
    + mehrdimensionale Arrays haben mindestens ein Integer als Kindknoten
+ Array Initialisierung und Array
  ```
    (ARRAY_INIT)
    |-- arr2 (INT)
    |   '__ 3 (INT)
    '__ (ARRAY)
    |-- 1 (INT)
    |-- 2 (INT)
    '__ 3 (INT)
  ```
  +  bei mehrdimensionalen Arrays hat ``ARRAY`` weitere ``ARRAY`` als Kinder
+ Array Referenz
  ```
    (ARRAY_REF)
    |-- ref_arr (INT)
    |   '__ five (ID)
    '__ arr (ID)
    ```
  + Array Referenzen sehen im ersten Teil aus wie Array Deklarationen.
  + Sie haben ein zweites Kind, welches das Array ist, worauf die Referenz zeigt.
+ Klassen
  ````
    A (CLASS)
    |-- A (CONSTRUCTOR)
    |-- copy_A (COPY_CONSTRUCTOR)
    |   '__ ref (CLASSTYPE)
    |       |-- A (ID)
    |       '__ (REF)
    |-- (DESTRUCTOR)
    |   '__ A (ID)
    '__ operator= (OPERATOR)
    |-- A (ID)
    |   '__ (REF)
    '__ (PARAMS)
    '__ ref (CLASSTYPE)
    |-- A (ID)
    '__ (REF)
  ````
  + Klassen erzeugen von alleine einen Konstruktor, Copy-Konstruktor und Operator,
    sofern sie nicht vorher implementiert wurden.
  + Sie können auch noch andere Knotenarten wie `FN_DECL` und `VAR_DECL` beinhalten.
+ Klassenobjekte
  ````
  aclass (CLASSTYPE)
  '__ A (ID)
  ````
  + Um darzustellen, dass eine Variable vom Typ einer Klasse ist und
  weitere Abfragen in der Methode ``visitClass`` zu verhindern, haben wir den Typ ``CLASSTYPE``
  hinzugefügt.
+ Zuweisungen
  ````
  = (ASSIGN)
  |-- i (ID)
  '__ == (EQUAL)
       |-- 5 (INT)
       '__ 4 (INT)
  ````
  + Eine Zuweisung besitzt immer genau zwei Kinder
  + In dem Hauptknoten ist der Zuweisungsoperator beschrieben (=, +=, etc.)
  + Das zweite Kind darf auch Rechnungen oder ähnliches beinhalten.
+ Objektbenutzung
  ````
  (OBJ_USAGE)
  |-- aclass (ID)
  '__ value (ID)
  ````
  + in diesem Beispiel ist ``aclass`` das Object und `value` eine Variable aus diesem
  + ``OBJ_USAGE`` kann Rekursiv erweitert werden.
  + Der Knoten geht nach dem Prinzip Objekt.Variable1.Variable2 usw. vor
+ Rechnungen
  ````
  + (ADD) 
  |-- * (MUL)
  |   |-- 5 (INT)
  |   '__ 3 (INT) 
  '__ 2 (INT) 
  ````
  + Unsere Rechnungsknoten haben jeweils ein Rechenzeichen, den dazugehörigen Begriff und zwei Kinder
  + Für längere Rechnungen werden weitere Rechnungsknoten angefügt
  + Rechnungsoperation mit höherer Priorität (Punkt vor Strich) befinden sich am tiefsten in dem Knoten
  + Die Knoten für Vergleichungen sind sehr ähnlich

Nicht alle unserer AST-Knoten sind hier beschrieben worden.
Dies waren die am häufigsten vorkommenden oder interessantesten unserer Knoten. 

---

## FirstScopeVisitor <br/>
In dem ``FirstScopeVisitor`` gehen wir sämtliche Deklarationen wie
die von Arrays, Funktionen, Klassen und Variablen. Dies tun wir, um die verschiedenen
Objekte zu speichern und in ihrem jeweiligen Scope zu speichern.

In dem globalen Scope erschaffen wir zusätzlich die Folgenden ``BuiltIn``'s
````
    globalScope.bind(new BuiltIn("int"));
    globalScope.bind(new BuiltIn("bool"));
    globalScope.bind(new BuiltIn("char"));
    globalScope.bind(new BuiltIn("void"));
    globalScope.bind(new BuiltIn("print_int"));
    globalScope.bind(new BuiltIn("print_bool"));
    globalScope.bind(new BuiltIn("print_char"));
````

### Besonderheiten
Wir speichern die Scopes nicht nur ineinander, sondern auch in dem jeweiligen Knoten ab.
Dadurch vergewissern wir uns, dass wir uns im korrekten Scope befinden und unser Projekt
weniger fehleranfällig wird.

Dies ist außerdem die Stelle wo wir nachträglich, wenn nötig, Konstruktoren und Operator einfügen.

### Schwierigkeiten
Wir haben zu einem früheren Punkt zu viel für die Überprüfung für Arrays hinzugefügt, womit wir uns das Leben einfacher 
machen wollten. Die Problematik ist dann aber in dem ``TypeCheckVisitor`` aufgefallen, da so der ``FirstScopeVisitor`` fehler geworfen hat,
die wir jedoch in dem `TypeCheckVisitor` als valide anerkannt haben.
Dieses Problem ist teilweise dadurch entstanden, dass wir Schwierigkeiten hatten wie viel und welche 
Überprüfungen in welchen konkreten Lauf gehören.

---

## SecondScopeVisitor
Der `SecondScopeVisitor` ist für die restlichen Knoten zuständig. Hier wird für die 
verbliebenen Knoten die Scopes gesetzt, aber auch viele Überprüfungen vorgenommen. 
Es wird zum Beispiel überprüft, ob der Aufruf einer Methode gleich viele Argumente hat,
wie die Deklaration Parameter hat, und ob alle gefundenen Variablen in der Symboltabelle existieren.

In diesem Schritt ergänzen wir auch, die im vorherigen Lauf hinzugekommenen Konstruktoren, Destruktoren und Operator.

### Schwierigkeiten
Es sind häufig Edge-Cases nachträglich aufgefallen, weshalb wir häufig zu diesem Lauf zurückkehren mussten und
Code ergänzten. Hierdurch sind manche Methoden unangenehm groß geworden.

---

## TypeCheckVisitor
In diesem Lauf überprüfen wir jegliche Typen von Objekten, die benutzt werden.
Es gibt jedoch auch einige Knoten, durch die wir nur durchiterieren müssen.

Der Fokus liegt auf den Zuweisungen, welche auch in den Deklarationen von Variablen passieren und auf Methodenaufrufen.
Ein großer Teil hiervon sind Rechnungen sowie Vergleiche.

### Besonderheiten
Wir benutzen zwei Hilfsmethoden als Ausgangspunkt in fast jeder Methode: ``typeIsValid`` und ``getEndType``:
+ ``typeIsValid`` gibt zurück ob es sich bei dem angegebenen Typen um einen `BuiltIn` Typen handelt,
  welcher jedoch nicht void ist, da man mit void nicht rechnen kann. Diese Methode ist besonders wichtig, da wir uns 
  dazu entschieden haben Typen so zu behandeln wie es auch C++ tut, d.h. die drei Typen ``int``, `bool` und
  ``char`` können untereinander verwendet werden ohne Typfehler zu bekommen.
  
  Die Methode ist zwar nicht groß, aber wird, dadurch dass wir nicht auf einen bestimmten Typen, sondern auf einen der drei
  prüfen, sehr häfig benutzt.
+ ``getEndType`` gibt den letztendlichen Typen eines Knotens zurück. Dies ist eher uninteressant bei normalen Werten, aber eine Hilfe
  bei Variablen. Sie löst ``OBJ_Usages`` und Klassenobjekte auf und beinhält, fast jeden Fall, der in unserer Symboltabelle
  auftreten kann.
  
---

## Interpreter
Hier Überprüfen wir die letzten Fehler wie z.B. ein ``out of bounds``. Hauptsächlich beginnen wir hier jedoch mit den wirklichen
Werten zu rechnen und diese auszugeben.

Wir geben zuerst die Ergebnisse der abgearbeiteten Befehle und dann das 
zugehörige ``Environment`` auf der Konsole aus

### Schwierigkeiten
Wir hatten Schwierigkeiten die Werte in die Arrays einzugeben und diese vernünftig zu auf den korrekten
Typ zu casten.