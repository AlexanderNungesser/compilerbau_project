
int main(){
// Destfall 1: Standard-Deklaration
int a;

// Destfall 2: Initialisierte Deklaration
int b = 42;

// Destfall 3: Referenzdeklaration
int &ref = a;

// Destfall 4: Array-Deklaration ohne Initialisierung
int arr[5];

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


