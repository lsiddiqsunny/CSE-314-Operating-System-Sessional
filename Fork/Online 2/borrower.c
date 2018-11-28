#include "Data.h"
#include <sys/shm.h>
#include<unistd.h>
#include<stdlib.h>
#include<stdio.h>
#include<string.h>

int main(){

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
	shared_stuff->a.cur=10;
    shared_stuff->b.cur=8;
    shared_stuff->c.cur=6;

    shared_stuff->a.borrow=0;
    shared_stuff->b.borrow=0;
    shared_stuff->c.borrow=0;
    
    int number=5;
     printf("Enter your id :");
     int id;
    scanf("%d",&id);
	while(running) {
        number--;

           
            int ch;
            printf("Enter your option: 1.Borrow 2.Return : ");
            scanf("%d",&ch);
            if(ch==1){
                int w;
                printf("Enter your option: 1.A 2.B 3.C : ");
                scanf("%d",&w);
                int num;
                printf("Enter Number of book : ");
                scanf("%d",&num);
                if(w==1){
                    if((shared_stuff->a.cur-shared_stuff->a.borrow)>=num){
                        printf("Book borrowed from writer A , number of  books %d by %d\n",num,id);
                        shared_stuff->a.borrow+=num;

                    }else{
                         printf("Book can not be borrowed from writer A  by %d\n",id);
                    }

                }
                else if(w==2){
                    if((shared_stuff->b.cur-shared_stuff->b.borrow)>=num){
                        printf("Book borrowed from writer B , number of  books %d by %d\n",num,id);
                         shared_stuff->b.borrow+=num;
                    }else{
                         printf("Book can not be borrowed from writer B  by %d\n",id);
                    }

                }else {
                    if((shared_stuff->c.cur-shared_stuff->c.borrow)>=num){
                        printf("Book borrowed from writer C, number of  books %d by %d\n",num,id);
                         shared_stuff->c.borrow+=num;
                    }else{
                         printf("Book can not be borrowed from writer B  by %d\n",id);
                    }

                }
            }
            else{
                int w;
                printf("Enter your option: 1.A 2.B 3.C : ");
                scanf("%d",&w);
                int num;
                printf("Enter Number of book : ");
                scanf("%d",&num);
                if(w==1){
                    if((shared_stuff->a.borrow)>=num){
                        printf("Book returned from writer A , number of  books %d by %d\n",num,id);
                        shared_stuff->a.borrow-=num;

                    }else{
                         printf("Book can not be borrowed from writer A  by %d\n",id);
                    }

                }
                else if(w==2){
                    if((shared_stuff->b.borrow)>=num){
                        printf("Book returned from writer B , number of  books %d by %d\n",num,id);
                         shared_stuff->b.borrow-=num;
                    }else{
                         printf("Book can not be borrowed from writer B  by %d\n",id);
                    }

                }else {
                    if((shared_stuff->c.borrow)>=num){
                        printf("Book returned from writer C, number of  books %d by %d\n",num,id);
                         shared_stuff->c.borrow-=num;
                    }else{
                         printf("Book can not be borrowed from writer B  by %d\n",id);
                    }

                }
                

            }
            if(number==0) running=0;
		
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