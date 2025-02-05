
void foo() {
    int b = 3;
    b = b + 5;
    print_char('f');// 'f'
}

void bar();

int wuppie(int x) {
    print_char('w');    // 'w'
    print_int(x);       // ...
    x = 99;
    print_int(x);       // 99
    return x;
}

//class A{
//    public:
//};
//
//A test() {
//    A a;
//    return a;
//}

int fluppie(int &x) {
    print_char('f');    // 'f'
    print_int(x);       // ...
    x = 99;
    print_int(x);       // 99
    return x;
}

int foobar(int x, int y, int z, int &r) {
    print_char('f');    // 'f'
    print_int(x);       // ...
    print_int(y);       // ...
    print_int(z);       // ...
    print_int(r);       // ...
    r = 99;
    return x + 'y' + z;
}

void f1(int x) {
    x = x + 2;
    wuppie(x);
}

int f95(int n) {
    int a = n - 1;
    int b = a - 1;
    int c = b - 1;
    int d = c - 1;
    int e = d--;
    if (n <= 0) {
        return 1;
    } else {
        if (n == 1) {
            return 1;
        } else {
            return f95(a) + f95(b) + f95(c) + f95(d) + f95(e);
        }
    }
}


int main() {
    // einfacher Funktionsaufruf, vorher definiert
    foo();      // 'f'

    // einfacher Funktionsaufruf, Vorwärtsdeklaration
    bar();      // 'b'

    // Funktionsaufruf mit Parameter
    int x = 7;
    int y = wuppie(x);  // w, 7, 99
    print_int(x);       // 7
    print_int(y);       // 99

    // Funktionsaufruf mit Referenz-Parameter
    int a = 7;
    int b = fluppie(a); // f, 7, 99
    print_int(a);       // 99
    print_int(b);       // 99

    // Funktionsaufruf mit vielen Parametern
    int h = 7;
    int j = foobar(1, 2, 3, h); // f, 1, 2, 3, 7
    print_int(h);               // 99
    print_int(j);               // 6

    // Funktionen können Funktionen aufrufen
    f1(9);  // w, 11, 99

    // rekursive Funktionen
    print_int(f95(0));      // 1
    print_int(f95(1));      // 1
    print_int(f95(2));      // 5
    print_int(f95(10));     // 977

    // diese Aufrufe dürfen nicht gehen
//    fluppie(1, 2, 3, h);
//    foobar(1, 2, 3, 8);


    return 0;
}


void bar() {
    print_char('b');    // 'b'
}
