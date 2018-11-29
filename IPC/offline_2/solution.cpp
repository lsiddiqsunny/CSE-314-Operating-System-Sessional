#include<stdio.h>
#include<queue>
#include<pthread.h>
#include<semaphore.h>
#include <sys/shm.h>
#include<unistd.h>
#define sz 5
using namespace std;

sem_t chef;
sem_t choco1;
sem_t vanila1;
queue<int> q1,q2,q3;
pthread_mutex_t lock;

void init_semaphore()
{
	sem_init(&chef,0,0);
    sem_init(&choco1,0,0);
    sem_init(&vanila1,0,0);
	pthread_mutex_init(&lock,0);
}

void * chef1(void * arg)
{	    pthread_mutex_lock(&lock);	
	    printf("%s\n",(char*)arg);
        pthread_mutex_unlock(&lock);
        while(1){
        pthread_mutex_lock(&lock);
        if(q1.size()==5){
            sem_init(&chef,0,0);
            pthread_mutex_unlock(&lock);
            sem_wait(&chef);

        }else{
        q1.push(1);
        printf("chef x produces chocolate cake \n");
        pthread_mutex_unlock(&lock); 
        sleep(2);
        }
       
        }
	
}
void * chef2(void * arg)
{	
	    pthread_mutex_lock(&lock);	
	    printf("%s\n",(char*)arg);
        pthread_mutex_unlock(&lock);
	
		while(1){
        pthread_mutex_lock(&lock);
        if(q1.size()==5){
            sem_init(&chef,0,0);
            pthread_mutex_unlock(&lock);
            sem_wait(&chef);

        }else{
        q1.push(2);
		printf("chef y produces vanila cake \n");
		pthread_mutex_unlock(&lock);
        sleep(2);
        }
        
        
        }	
}

void * chef3(void * arg)
{	
	    pthread_mutex_lock(&lock);	
	    printf("%s\n",(char*)arg);
        pthread_mutex_unlock(&lock);
        while(1){
		
       
        printf("Before: %d %d %d\n",q1.size(),q2.size(),q3.size());
        pthread_mutex_lock(&lock);	
        if(q1.size()==0){
           // sem_init(&chef,0,0);
            sem_post(&chef);
            pthread_mutex_unlock(&lock);

        }else{
            
            int x=q1.front();
        if(x==1){
            if(q2.size()==5){
                sem_init(&choco1,0,0);
                pthread_mutex_unlock(&lock);

                sem_wait(&choco1);
            }
            
            q1.pop();
            q2.push(1);
            printf("chef z decorates chocolate cake \n");
            pthread_mutex_unlock(&lock);
		    
            }else{
            
            if(q3.size()==5){
                sem_init(&vanila1,0,0);
                pthread_mutex_unlock(&lock);
                sem_wait(&vanila1);
            }
            
            q1.pop();
            q3.push(1);
            printf("chef z decorates vanila cake \n");
            pthread_mutex_unlock(&lock);

        }
        printf("After: %d %d %d\n",q1.size(),q2.size(),q3.size());
        
         

        }	
        sleep(2);	
		
        }
}
void * waiter1(void * arg)
{	
	    pthread_mutex_lock(&lock);	
	    printf("%s\n",(char*)arg);
        pthread_mutex_unlock(&lock);
        while(1){
        pthread_mutex_lock(&lock);	
        if(q2.size()==0){
            sem_post(&choco1);
            pthread_mutex_unlock(&lock);
        }
        else{
        	
       	q2.pop();
        printf("waiter 1 serves chocolate cake \n");
        pthread_mutex_unlock(&lock);
        sleep(2);
        }
        
        }
	
}
void * waiter2(void * arg)
{	
	    pthread_mutex_lock(&lock);	
	    printf("%s\n",(char*)arg);
        pthread_mutex_unlock(&lock);
        while(1){
        pthread_mutex_lock(&lock);
        if(q3.size()==0){
            sem_post(&vanila1);
            pthread_mutex_unlock(&lock);
        }
        else{
        	
       	q3.pop();
        printf("waiter 2 serves vanila cake \n");
        pthread_mutex_unlock(&lock);
        sleep(2);
        }
        
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