#include<sys/types.h>
#include <stdio.h>
#include<unistd.h>

int main() {
	pid_t new_pid;
	printf("Hello World\n");
	new_pid = fork();
	
	switch(new_pid) {
	case -1 :
	/* Error */
		printf("Error\n");
	break;
	case 0 :
	/* We are child */
		printf("%d\n", new_pid);
		printf("Child\n");
	break;
	default :
	/* We are parent */
		printf("%d\n", new_pid);
		printf("Parent\n");
	break;
	}
	return 0;
}
