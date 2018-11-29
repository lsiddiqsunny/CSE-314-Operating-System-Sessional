#include<stdio.h>
#include<queue>
#include<pthread.h>
#include<semaphore.h>
#include <sys/shm.h>
#include<unistd.h>
#define sz 5
using namespace std;

sem_t chefempty;
sem_t cheffull;
sem_t chocoempty;
sem_t vanilaempty;
sem_t chocofull;
sem_t vanilafull;
queue<int> q1,q2,q3;
pthread_mutex_t lock;

void init_semaphore()
{
	sem_init(&chefempty,0,5);
    sem_init(&cheffull,0,0);

    sem_init(&chocoempty,0,5);
    sem_init(&chocofull,0,0);

    sem_init(&vanilaempty,0,5);
    sem_init(&vanilafull,0,0);

	pthread_mutex_init(&lock,0);
}

void * chef1(void * arg)
{	    pthread_mutex_lock(&lock);	
	    printf("%s\n",(char*)arg);
        pthread_mutex_unlock(&lock);

        while(1){
       

        sem_wait(&chefempty); 
        pthread_mutex_lock(&lock);
        q1.push(1);
        printf("Chef x produces chocolate cake.\n");
        sem_post(&cheffull);
        
        pthread_mutex_unlock(&lock); 
        sleep(1);
        }
        
}
	
void * chef2(void * arg)
{	
	    pthread_mutex_lock(&lock);	
	    printf("%s\n",(char*)arg);
        pthread_mutex_unlock(&lock);
	
		while(1){
        

        sem_wait(&chefempty);
        pthread_mutex_lock(&lock);
        q1.push(2);
        printf("Chef y produces vanila cake.\n");
        sem_post(&cheffull);
        
        pthread_mutex_unlock(&lock); 
        sleep(1);
        }
        
        
}	


void * chef3(void * arg)
{	
	    pthread_mutex_lock(&lock);	
	    printf("%s\n",(char*)arg);
        pthread_mutex_unlock(&lock);
        while(1){
		
        sem_wait(&cheffull);
        pthread_mutex_lock(&lock);
        printf("Before: %d %d %d\n",q1.size(),q2.size(),q3.size());	
        int x=q1.front();
        if(x==1){
            sem_wait(&chocoempty);
            q1.pop();
            q2.push(1);
            printf("Chef z decorates chocolate cake.\n");
            sem_post(&chocofull);


        }else{
            sem_wait(&vanilaempty); 
            q1.pop();
            q3.push(1);
            printf("Chef z decorates vanila cake.\n");
           
            sem_post(&vanilafull);

        }
        printf("After: %d %d %d\n",q1.size(),q2.size(),q3.size());
        sem_post(&chefempty);
        pthread_mutex_unlock(&lock);


          
        sleep(1);	
		
        }
}
void * waiter1(void * arg)
{	
	    pthread_mutex_lock(&lock);	
	    printf("%s\n",(char*)arg);
        pthread_mutex_unlock(&lock);
        while(1){
        
        sem_wait(&chocofull);
        pthread_mutex_lock(&lock);	
        q2.pop();
        printf("Waiter 1 serves chocolate cake.\n");
        
        sem_post(&chocoempty);
        pthread_mutex_unlock(&lock);
        
        sleep(1);
        }
}
void * waiter2(void * arg)
{	
	    pthread_mutex_lock(&lock);	
	    printf("%s\n",(char*)arg);
        pthread_mutex_unlock(&lock);
        
        while(1){
       
        sem_wait(&vanilafull); 
        pthread_mutex_lock(&lock);
        q3.pop();
        printf("Waiter 2 serves vanila cake.\n");
        
        sem_post(&vanilaempty);
        pthread_mutex_unlock(&lock);
        sleep(1);
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