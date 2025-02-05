
int main() {
    // if-then-else
    bool a = true;
    int b = 42;

    if (a == true) {
        print_bool(true);   // 1
    }

    if (b) {
        print_bool(true);   // 1
        b = 7;
    }
    print_int(b);           // 7

    if (b) {
        print_bool(true);   // 1
        int b = 2;
        print_int(b);       // 2
    }
    print_int(b);           // 7

    if (false) {
        print_char('1');    // keine Ausgabe
    } else {
        print_char('2');    // 2
    }

    if (false) {
        print_char('x');        // keine Ausgabe
    } else {
        if (a == true) {
            print_bool(true);   // 1
        }
        print_char('y');        // y
    }

    if (false) {
        print_char('x');
    } else if (true) {
        print_char('z');
    } else {
        print_char('y');
    }


    return 0;
}
