#include<stdio.h>
#include<iostream>
#include<unistd.h>
#include<semaphore.h>
#include<pthread.h>
#include<queue>
#include<time.h>
#include<stdlib.h>

#define vanila_cake 1
#define chocolate_cake 2

using namespace std;

sem_t producer_empty;
sem_t producer_full;
sem_t vanila_empty;
sem_t vanila_full;
sem_t chocolate_empty;
sem_t chocolate_full;

pthread_mutex_t q_mutex;
pthread_mutex_t console_mutex;

queue<int> producer_queue;
queue<int> vanila_queue;
queue<int> chocolate_queue;

bool sleep_on=true;
int v_produced;
int c_produced;
int v_consumed;
int c_consumed;

void init_sem(){
    //initialize with 5
    //Whole buffer is empty
    sem_init(&producer_empty, 0, 5);
    sem_init(&producer_full,0, 0);

    sem_init(&vanila_empty, 0, 5);
    sem_init(&vanila_full,0,0);

    sem_init(&chocolate_empty, 0, 5);
    sem_init(&chocolate_full,0,0);

    pthread_mutex_init(&q_mutex,0);
    pthread_mutex_init(&console_mutex,0);
}

void * produce_van(void * args){

    while(true){
        //Check if producer's queue is empty or not
        pthread_mutex_lock(&q_mutex);
        sem_wait(&producer_empty);

        int cake= vanila_cake;
        producer_queue.push(cake);

        pthread_mutex_lock(&console_mutex);
        cout<< "Producing Cake: ";
        cout << "Vanila Cake Produced"<<endl;
        cout << producer_queue.size()<< " items in prod q"<<endl;
        pthread_mutex_unlock(&console_mutex);

        //Produced 1 item
        sem_post(&producer_full);
        pthread_mutex_unlock(&q_mutex);
        int s_time=rand()%3 +1;
        if(sleep_on) sleep(s_time);
    }
}

void * produce_cho(void * args){

    while(true){
        //Check if producer's queue is empty or not
        pthread_mutex_lock(&q_mutex);
        sem_wait(&producer_empty);
        int cake= chocolate_cake;

        producer_queue.push(cake);

        pthread_mutex_lock(&console_mutex);
        cout << "Producing Cake: Chocolate Cake Produced"<<endl;
        cout << producer_queue.size()<< " items in prod q"<<endl;
        pthread_mutex_unlock(&console_mutex);

        //Produced 1 item
        sem_post(&producer_full);
        pthread_mutex_unlock(&q_mutex);

        int s_time=rand()%3 +1;
        if(sleep_on) sleep(s_time);
    }
}

void * decorate(void * args){

    while(true){

        //Check if there is anything on the queue
        //Wait if there is no item in producers queue
        sem_wait(&producer_full);

        pthread_mutex_lock(&console_mutex);
        cout << "Decorating Cake: ";
        pthread_mutex_unlock(&console_mutex);

        int cake= producer_queue.front();
        //producer_queue.pop();

        if(cake==vanila_cake){
            
            //Can't take from producers queue if vanila queue is not empty
            sem_wait(&vanila_empty);
            vanila_queue.push(vanila_cake);
            producer_queue.pop();

            pthread_mutex_lock(&console_mutex);
            cout << "Got Vanila Cake!"<<endl;
            cout << producer_queue.size()<< " items in prod q and "<< vanila_queue.size()<< " in vanila q"<<endl;
            pthread_mutex_unlock(&console_mutex);

            sem_post(&vanila_full);
        }else if(cake==chocolate_cake){
            sem_wait(&chocolate_empty);
            
            chocolate_queue.push(chocolate_cake);
            producer_queue.pop();

            pthread_mutex_lock(&console_mutex);
            cout<< "Got Chocoate Cake!"<<endl;
            cout<< producer_queue.size()<< " items in prod q and "<< chocolate_queue.size()<< " in choco q"<<endl;
            pthread_mutex_unlock(&console_mutex);

            sem_post(&chocolate_full);
        }

        sem_post(&producer_empty);
        
        int s_time=rand()%3 +1;
        if(sleep_on) sleep(s_time);
    }
}

void * waiter_v(void * args){

    while(true){
        sem_wait(&vanila_full);
        vanila_queue.pop();
        
        pthread_mutex_lock(&console_mutex);
        cout<< "Consuming a vanila Cake"<<endl;
        cout<< vanila_queue.size()<< " items in vanila q"<<endl;
        pthread_mutex_unlock(&console_mutex);

        sem_post(&vanila_empty);

        int s_time=rand()%3 +1;
        if(sleep_on) sleep(s_time);
    }

}

void * waiter_c(void * args){

    while(true){
        sem_wait(&chocolate_full);
        chocolate_queue.pop();

        pthread_mutex_lock(&console_mutex);
        cout<< "Consuming a chocolate cake"<<endl;
        cout<< chocolate_queue.size()<< " items in choco q"<<endl;
        pthread_mutex_unlock(&console_mutex);

        sem_post(&chocolate_empty);

        int s_time=rand()%3 +1;
        if(sleep_on) sleep(s_time);
    }

}

int main(){

    init_sem();
    sleep_on=true;

    pthread_t prod_van;
    pthread_t prod_cho;
    pthread_t dec_th;
    pthread_t wait_vth;
    pthread_t wait_cth;

    pthread_create(&prod_cho, NULL, produce_cho, (void *) 0);
    pthread_create(&prod_van, NULL, produce_van, (void *) 0);
    pthread_create(&dec_th, NULL, decorate, (void *) 0);
    pthread_create(&wait_vth, NULL, waiter_v, (void *) 0);
    pthread_create(&wait_cth, NULL, waiter_c, (void*) 0);

    cout<< "main"<<endl;

    while(1);

    return 0;
}