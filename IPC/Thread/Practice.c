#include <pthread.h>
#include <stdio.h>
pthread_mutex_t mutex;
int sum_value;
void *doSum (void *arg) {
int sum = (int) arg;
	for(int i=0;i<10;i++){
		pthread_mutex_lock(&mutex);
		sum_value += sum;
		printf("After sum in thread %d = %d\n", sum, sum_value);
		pthread_mutex_unlock (&mutex);
		sleep(1);
	}
}
int main (int argc, char *argv[]) {
pthread_t thread1, thread2;
int t1 = 1, t2 = 2;
pthread_mutex_init(&mutex, NULL);
pthread_create(&thread1, NULL, doSum, (void *)t1);
pthread_create(&thread2, NULL, doSum, (void *)t2);
pthread_join (thread1, NULL);
pthread_join (thread2, NULL);
return  0;
}
