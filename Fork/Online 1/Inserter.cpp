#include "Data.h"
#include <sys/shm.h>
#include<unistd.h>
#include<stdlib.h>
#include<stdio.h>
#include<string.h>

int main(){

    int running = 1;
	void *shared_memory = (void *)0;
	struct shared_use_st *shared_stuff;
	int shmid;
	shmid = shmget((key_t)1234, sizeof(struct shared_use_st), 0666 | IPC_CREAT);
	if (shmid == -1) {
		printf("shmget failed\n");
		exit(EXIT_FAILURE);
	}
	shared_memory = shmat(shmid, (void *)0, 0);
	if (shared_memory == (void *)-1) {
		printf("shmat failed\n");
		exit(EXIT_FAILURE);
	}
	printf("Memory attached at %p\n", shared_memory);	
	shared_stuff = (struct shared_use_st *)shared_memory;
	shared_stuff->written_by_you = 0;
    //shared_stuff->v.clear();
    shared_stuff->Size=0;;
    //shared_stuff->command="";
	strncpy(shared_stuff->command,"",MAX_SZ);
	while(running) {
            int n;
            printf("Enter a nurmber : ");
            scanf("%d",&n);
            int prev_sz=shared_stuff->Size;
            for(int i=0;i<n;i++){
                int x;
                scanf("%d",&x);
                shared_stuff->v[prev_sz+i]=x;
                shared_stuff->Size++;
            }
            for(int i=0;i<shared_stuff->Size;i++){
                printf("%d ",shared_stuff->v[i]);
            }
            printf("\n");
            strncpy(shared_stuff->command,"Sort",MAX_SZ);

			sleep(4); /* make the other process wait for us ! */
			if (strncmp(shared_stuff->command,"end",3)==0) {
				running = 0;
			}else{
				strncpy(shared_stuff->command,"Inserting",MAX_SZ);
                for(int i=0;i<shared_stuff->Size;i++){
                printf("%d ",shared_stuff->v[i]);
                }
                printf("\n");
            }
		
	}
	if (shmdt(shared_memory) == -1) {
		printf("shmdt failed\n");
		exit(EXIT_FAILURE);
	}
	if (shmctl(shmid, IPC_RMID, 0) == -1) {
		printf("shmctl(IPC_RMID) failed\n");
		exit(EXIT_FAILURE);
	}
	exit(EXIT_SUCCESS);
}