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
pthread_mutex_t lock1;
pthread_mutex_t lock2;
pthread_mutex_t lock3;
pthread_mutex_t con;
void init_semaphore()
{
	sem_init(&chefempty,0,5);
    sem_init(&cheffull,0,0);

    sem_init(&chocoempty,0,5);
    sem_init(&chocofull,0,0);

    sem_init(&vanilaempty,0,5);
    sem_init(&vanilafull,0,0);

	pthread_mutex_init(&lock1,0);
    pthread_mutex_init(&lock2,0);
    pthread_mutex_init(&lock3,0);
    pthread_mutex_init(&con,0);
}

void * chef1(void * arg)
{	    pthread_mutex_lock(&con);	
	    printf("%s\n",(char*)arg);
        pthread_mutex_unlock(&con);

        while(1){
       

        sem_wait(&chefempty); 
        pthread_mutex_lock(&lock1);

        q1.push(1);

        pthread_mutex_lock(&con);
        printf("Chef x produces chocolate cake.\n");
        pthread_mutex_unlock(&con);

        pthread_mutex_unlock(&lock1); 
        sem_post(&cheffull);
        
        
        sleep(1);
        }
        
}
	
void * chef2(void * arg)
{	
	    pthread_mutex_lock(&con);	
	    printf("%s\n",(char*)arg);
        pthread_mutex_unlock(&con);
	
		while(1){
        

        sem_wait(&chefempty); 
        pthread_mutex_lock(&lock1);

        q1.push(2);

        pthread_mutex_lock(&con);
        printf("Chef y produces vanila cake.\n");
        pthread_mutex_unlock(&con);

        pthread_mutex_unlock(&lock1); 

        sem_post(&cheffull);

        sleep(1);
        }
        
        
}	


void * chef3(void * arg)
{	
	    pthread_mutex_lock(&con);	
	    printf("%s\n",(char*)arg);
        pthread_mutex_unlock(&con);
        while(1){
		
       
       
        // pthread_mutex_lock(&con);
        // printf("Before: %d %d %d\n",q1.size(),q2.size(),q3.size());
        // pthread_mutex_unlock(&con);

        int x=q1.front();
        if(x==1){
            sem_wait(&cheffull);
            sem_wait(&chocoempty);  
            
            

            pthread_mutex_lock(&lock1);
            q1.pop();
            pthread_mutex_unlock(&lock1);

            pthread_mutex_lock(&lock2);
            q2.push(1);

            pthread_mutex_lock(&con);
            printf("Chef z decorates chocolate cake.\n");
            pthread_mutex_unlock(&con);
            pthread_mutex_unlock(&lock2);
           
            sem_post(&chocofull);
            sem_post(&chefempty);


        }else{
            sem_wait(&cheffull);
            sem_wait(&vanilaempty);  
            

            pthread_mutex_lock(&lock1);
            q1.pop();
            pthread_mutex_unlock(&lock1);

            pthread_mutex_lock(&lock3);
            q3.push(1);

            pthread_mutex_lock(&con);
            printf("Chef z decorates vanila cake.\n");
            pthread_mutex_unlock(&con);

            pthread_mutex_unlock(&lock3);
            
            sem_post(&vanilafull);
            sem_post(&chefempty);

        }
        // pthread_mutex_lock(&con);
        // printf("After: %d %d %d\n",q1.size(),q2.size(),q3.size());
        // pthread_mutex_unlock(&con);
        sleep(1);	
		
        }
}
void * waiter1(void * arg)
{	
	    pthread_mutex_lock(&con);	
	    printf("%s\n",(char*)arg);
        pthread_mutex_unlock(&con);
        while(1){
        
        sem_wait(&chocofull);

        pthread_mutex_lock(&lock2);	
        q2.pop();

        pthread_mutex_lock(&con);	
        printf("Waiter 1 serves chocolate cake.\n");
        pthread_mutex_unlock(&con);	

        pthread_mutex_unlock(&lock2);
        sem_post(&chocoempty);
        sleep(1);
        }
}
void * waiter2(void * arg)
{	
	    pthread_mutex_lock(&con);	
	    printf("%s\n",(char*)arg);
        pthread_mutex_unlock(&con);
        
        while(1){
       
        sem_wait(&vanilafull); 

        pthread_mutex_lock(&lock3);
        q3.pop();

        pthread_mutex_lock(&con);
        printf("Waiter 2 serves vanila cake.\n");
        pthread_mutex_unlock(&con);
       
        pthread_mutex_unlock(&lock3); 
        sem_post(&vanilaempty);
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