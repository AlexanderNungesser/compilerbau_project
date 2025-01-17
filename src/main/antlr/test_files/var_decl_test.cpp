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
 b = 3 + '1' + 2 + 5 + aclass.value;

// Destfall 3: Referenzdeklaration
int &ref = aclass.value;

// Destfall 4: Array-Deklaration ohne Initialisierung
int i = 5;
int arr[5];
int j = arr[0];

// Destfall 5: Array-Deklaration mit Initialisierung
int arr2[] = {1, 2, 3};

// Destfall 6: Mehrdimensionales Array
int matrix[3][3];

// Destfall 7: Mehrdimensionales Array mit Initialisierung
int matrix2[2][2] = {{1, 2}, {3, 4}};

// Destfall 8: Komplexe Referenz mit Array
int (&ref_arr)[5] = arr;

return 0;
}


