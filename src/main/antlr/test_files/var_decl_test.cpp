class A {
public:     // es reicht, wenn alles public ist (hier nur, damit das Beispiel mit g++ kompiliert)
    int value;
};

int main(){
// Destfall 1: Standard-Deklaration
int a;

print_char('A');        // 'A'

A aclass;
// Destfall 2: Initialisierte Deklaration
int b;
 b = 5 * 3 + 2;
 b = 5 + 3 * 2;

// Destfall 3: Referenzdeklaration
int &ref = aclass.value;

// Destfall 4: Array-Deklaration ohne Initialisierung
int i;
 i = 5 == 4;
int arr[5];
int j = arr[6];
A u = aclass;
 i = true <= 5;

// Destfall 5: Array-Deklaration mit Initialisierung
int arr2[] = {1, 2, 3};

// Destfall 6: Mehrdimensionales Array
int matrix[3][3];

int five = 5;
char cc = 'c';
// Destfall 7: Mehrdimensionales Array mit Initialisierung
int matrix2[2][2] = {{4, 2}, {3, 4}};

// Destfall 8: Komplexe Referenz mit Array
int (&ref_arr)[five] = arr;

return 0;
}


