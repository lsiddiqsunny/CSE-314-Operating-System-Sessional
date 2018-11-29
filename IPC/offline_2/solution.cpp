#include<stdio.h>
#include<queue>
#include<pthread.h>
#include<semaphore.h>
#include <sys/shm.h>
#include<unistd.h>
#define sz 5
using namespace std;

sem_t choco;
sem_t vanila;
sem_t choco1;
sem_t vanila1;
queue<int> q1,q2,q3;
pthread_mutex_t lock;

void init_semaphore()
{
	sem_init(&choco,0,0);
    sem_init(&vanila,0,0);
    sem_init(&choco1,0,0);
    sem_init(&vanila1,0,0);
	pthread_mutex_init(&lock,0);
}

void * chef1(void * arg)
{	
	    printf("%s\n",(char*)arg);

        while(1){
        sem_post(&choco);	
		pthread_mutex_lock(&lock);	
        if(q1.size()<5)
		{
            q1.push(1);
		    printf("chef x produces chocolate cake \n");
        }
		pthread_mutex_unlock(&lock);
        sleep(2);
        }
	
}
void * chef2(void * arg)
{	
	    printf("%s\n",(char*)arg);
	
		while(1){
        sem_post(&vanila);	
		pthread_mutex_lock(&lock);
        if(q1.size()<5)
		{
            q1.push(2);
		    printf("chef y produces vanila cake \n");
        }
		
		pthread_mutex_unlock(&lock);
         sleep(2);
        }	
}

void * chef3(void * arg)
{	
	    printf("%s\n",(char*)arg);
        while(1){
		pthread_mutex_lock(&lock);
        if(q1.size()!=0){		
		int x=q1.front();
        q1.pop();
        if(x==1){
            sem_wait(&choco);
		    printf("chef z decorates chocolate cake \n");
            q2.push(1);
            sem_post(&choco1);
        }else{
             sem_wait(&vanila);
            printf("chef z decorates vanila cake \n");
            q3.push(1);
            sem_post(&vanila1);

        }
        }
		pthread_mutex_unlock(&lock);
         sleep(2);	
        }
}
void * waiter1(void * arg)
{	
	    printf("%s\n",(char*)arg);
        while(1){
        sem_wait(&choco1);
		pthread_mutex_lock(&lock);	
       	if(q2.size()!=0)
		{q2.pop();
		printf("waiter 1 serves chocolate cake \n");
        }
		pthread_mutex_unlock(&lock);
         sleep(2);
        }
	
}
void * waiter2(void * arg)
{	
	    printf("%s\n",(char*)arg);
        while(1){
        sem_wait(&vanila1);	
		pthread_mutex_lock(&lock);	
        if(q3.size()!=0)
		{q3.pop();
		printf("waiter 2 serves vanila cake \n");
        }
		pthread_mutex_unlock(&lock);
         sleep(2);
        }
}





int main(void)
{	
	pthread_t thread1;
	pthread_t thread2;
    pthread_t thread3;
	pthread_t thread4;
    pthread_t thread5;

	
	init_semaphore();
	
	char * message1 = "i am chef X";
	char * message2 = "i am chef Y";	
    char * message3 = "i am chef Z";
    char * message4 = "i am waiter 1";
	char * message5 = "i am waiter 2";	
	
	pthread_create(&thread1,NULL,chef1,(void*)message1 );
	pthread_create(&thread2,NULL,chef2,(void*)message2 );
    pthread_create(&thread1,NULL,chef3,(void*)message3 );
	pthread_create(&thread2,NULL,waiter1,(void*)message4 );
    pthread_create(&thread2,NULL,waiter2,(void*)message5 );


	
	while(1);
	return 0;
}