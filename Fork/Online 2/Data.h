
#define MAX_SZ 2048
#include<stdlib.h>
struct Writers{
     char name[4];
     int cur;
     int borrow;
     

};
struct catalog {
struct Writers a,b,c;
int id;
char text[100];
};

