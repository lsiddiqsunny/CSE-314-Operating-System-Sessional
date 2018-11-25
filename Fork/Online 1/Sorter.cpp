#include "Data.h"
#include <sys/shm.h>
#include<unistd.h>
#include<stdlib.h>
#include<stdio.h>
#include<string.h>
#include "semutil.h"
int main(int argc, char *argv[]){

	int i;
	sem_id = semget((key_t) 1234, 1, 0666 | IPC_CREAT);
	if (argc > 1) {
		if (!set_semvalue()) {
			printf("Failed to initialize semaphore\n");
			exit(EXIT_FAILURE);
		}
		sleep(2);
	}

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
	while(running) {
	
            
            if(strcmp(shared_stuff->command,"Sort")==0){
				//printf("%s\n",shared_stuff->command);
				int x[shared_stuff->Size];
            for(int i=0;i<shared_stuff->Size;i++){
                x[i]=shared_stuff->v[i];
            }
           // sort(shared_stuff->v,shared_stuff->v+shared_stuff->Size);
		   int sz=shared_stuff->Size;
		   for(int i=0;i<shared_stuff->Size;i++){
			   for(int j=0;j<shared_stuff->Size;j++){
				   if(shared_stuff->v[i]<shared_stuff->v[j]){
					   int a=shared_stuff->v[i];
					shared_stuff->v[i]=shared_stuff->v[j];
					shared_stuff->v[j]=a;
				}
			   }

		   }
            int ok=1;
            for(int i=0;i<shared_stuff->Size;i++){
                if(x[i]!=shared_stuff->v[i]){
					//printf("%d " ,x[i]);
                    ok=0;
                }
            }
			//printf("\n");
			printf("sorted\n");
            if(ok==1){
                strncpy(shared_stuff->command,"end",MAX_SZ);
                running=0;
            }else{
				strncpy(shared_stuff->command,"show",MAX_SZ);
				sleep(1);
                strncpy(shared_stuff->command,"Sorted",MAX_SZ);
            }
			sleep(1);
            }
			
		
	}
	if (shmdt(shared_memory) == -1) {
		printf("shmdt failed\n");
		exit(EXIT_FAILURE);
	}
	
	exit(EXIT_SUCCESS);
}