#include "Data.h"
#include <sys/shm.h>
#include<unistd.h>
#include<stdlib.h>
#include<stdio.h>
#include<string.h>
#include<time.h>
#include "semutil.h"
int main(int argc, char *argv[]){
srand(time(NULL));

    
	// sem_id = semget((key_t) 1234, 1, 0666 | IPC_CREAT);
	// if (argc > 1) {
	// 	if (!set_semvalue()) {
	// 		printf("Failed to initialize semaphore\n");
	// 		exit(EXIT_FAILURE);
	// 	}
	// 	sleep(2);
	// }
    int running = 1;
	void *shared_memory = (void *)0;
	struct catalog *shared_stuff;
	int shmid;
	shmid = shmget((key_t)1234, sizeof(struct catalog), 0666 | IPC_CREAT);
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
	shared_stuff = (struct catalog *)shared_memory;
    
    strcpy(shared_stuff->a.name,"A");
    strcpy(shared_stuff->b.name,"B");
    strcpy(shared_stuff->c.name,"C");
	shared_stuff->a.cur=5;
    shared_stuff->b.cur=5;
    shared_stuff->c.cur=5;

    shared_stuff->a.borrow=0;
    shared_stuff->b.borrow=0;
    shared_stuff->c.borrow=0;
    shared_stuff->id=0;
	strcpy(shared_stuff->text,"");
    int number=5;

    int num=0;
	while(running) {//printf("%d\n",shared_stuff->id);
		if(shared_stuff->id==0){
		//	printf("here");
        if(fork()==0){
            shared_stuff->id++;
            num=shared_stuff->id;
            if(fork()==0){
                shared_stuff->id++;
                num=shared_stuff->id;
                if(fork()==0){
                shared_stuff->id++;
                num=shared_stuff->id;
                if(fork()==0){
                shared_stuff->id++;
                num=shared_stuff->id;
                if(fork()==0){
                shared_stuff->id++;
                num=shared_stuff->id;
				if(fork()==0){
                shared_stuff->id++;
                num=shared_stuff->id;
				if(fork()==0){
                shared_stuff->id++;
                num=shared_stuff->id;
				if(fork()==0){
                shared_stuff->id++;
                num=shared_stuff->id;
				if(fork()==0){
                shared_stuff->id++;
                num=shared_stuff->id;
				if(fork()==0){
                shared_stuff->id++;
                num=shared_stuff->id;
                }
                }
                }
                }

                }
                }
                }
                }
            }
        }else{
           // printf("here");
            shared_stuff->id++;
            num=shared_stuff->id;
        }
		}

	
		if(num==11){
			if(shared_stuff->id<=0) break;
			//puts(shared_stuff->text);
			//if (!semaphore_p()) exit(EXIT_FAILURE);
			if(strcmp(shared_stuff->text,"A -1")==0){
				shared_stuff->a.cur--;
				shared_stuff->a.borrow++;strcpy(shared_stuff->text,"Done");
				printf("%d %d %d\n",shared_stuff->a.cur,shared_stuff->b.cur,shared_stuff->c.cur);

			}else if(strcmp(shared_stuff->text,"B -1")==0){
				shared_stuff->b.cur--;
				shared_stuff->b.borrow++;

				strcpy(shared_stuff->text,"Done");
								printf("%d %d %d\n",shared_stuff->a.cur,shared_stuff->b.cur,shared_stuff->c.cur);


			}
			else if(strcmp(shared_stuff->text,"C -1")==0){
				shared_stuff->c.cur--;
				shared_stuff->c.borrow++;
				strcpy(shared_stuff->text,"Done");
								printf("%d %d %d\n",shared_stuff->a.cur,shared_stuff->b.cur,shared_stuff->c.cur);


			}else if(strcmp(shared_stuff->text,"A 1")==0){
				shared_stuff->a.cur++;
				shared_stuff->a.borrow--;
				strcpy(shared_stuff->text,"Done");
								printf("%d %d %d\n",shared_stuff->a.cur,shared_stuff->b.cur,shared_stuff->c.cur);


			}else if(strcmp(shared_stuff->text,"B 1")==0){
				shared_stuff->b.cur++;
				shared_stuff->b.borrow--;
				strcpy(shared_stuff->text,"Done");
								printf("%d %d %d\n",shared_stuff->a.cur,shared_stuff->b.cur,shared_stuff->c.cur);

			}
			else if(strcmp(shared_stuff->text,"C 1")==0){
				shared_stuff->c.cur++;
				shared_stuff->c.borrow--;
				strcpy(shared_stuff->text,"Done");
								printf("%d %d %d\n",shared_stuff->a.cur,shared_stuff->b.cur,shared_stuff->c.cur);

			}
			

	//	if (!semaphore_v()) exit(EXIT_FAILURE);	

			sleep(1);
			//printf("hello");

		}
		else{	//printf("%d\n",num);
				number--;
			
			
			int ch=rand()%2;
			//printf("%d %d\n",num,ch);
			if(ch==0){
				int w=rand()%3;
				//printf("%d %d %d\n",num,ch,w);
				if(w==0){
					if(shared_stuff->a.cur==0){
						while(shared_stuff->a.cur==0){

						}
						


					}strcpy(shared_stuff->text,"A -1");
						printf("%d borrowed From Writer A\n",num);

				}
				else if(w==1){
					if(shared_stuff->a.cur==0){
						while(shared_stuff->a.cur==0){

						}
						
						

					}strcpy(shared_stuff->text,"B -1");
						printf("%d borrowed From Writer B\n",num);

				}
				else{
					if(shared_stuff->a.cur==0){
						while(shared_stuff->a.cur==0){

						}
						
						

					}strcpy(shared_stuff->text,"C -1");
						printf("%d borrowed From Writer C\n",num);

				}
			}
			else {
				int w=rand()%3;
				if(w==0){
					if(shared_stuff->a.borrow!=0){
						
						strcpy(shared_stuff->text,"A 1");
						printf("%d returned From Writer A\n",num);

					}



				}
				else if(w==1){
					if(shared_stuff->a.borrow!=0){
						
						strcpy(shared_stuff->text,"B 1");
						printf("%d returned From Writer B\n",num);
						

					}

				}
				else{
					if(shared_stuff->a.borrow!=0){
						
						strcpy(shared_stuff->text,"C 1");
						printf("%d returned From Writer C\n",num);
						

					}
					
				}
				
			}
			if(!number){
				shared_stuff->id--;
				break;
			}
			
			sleep(1);


		}

        
		
	}
    // if (argc > 1) {
	// 	sleep(6);
	// 	del_semvalue();
	// }
	if (shmdt(shared_memory) == -1) {
		printf("shmdt failed\n");
		exit(EXIT_FAILURE);
	}
	// if (shmctl(shmid, IPC_RMID, 0) == -1) {
	// 	printf("shmctl(IPC_RMID) failed\n");
	// 	exit(EXIT_FAILURE);
	// }
	exit(EXIT_SUCCESS);
}