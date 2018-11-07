#include <pthread.h>
#include <stdio.h>
#include <time.h>
#include<stdlib.h>
#include<unistd.h>
#include<math.h>
#include<semaphore.h>


#define STEPS_AB 10
#define STEPS_CD 15
#define STEPS_DD 15
#define STEPS_BC 10

void * BA (void *arg) ;
void * AB (void *arg) ;
void * DD (void *arg) ;
void * CD (void *arg) ;
void * DC (void *arg) ;
void * BC (void *arg) ;

//int speed[] ={3,2,3,4,1} ;
FILE * fp;
int N=0;
int speed[100];
pthread_mutex_t mutexBC ;
pthread_mutex_t mutexCB ;
pthread_mutex_t mutexCD  ;
pthread_mutex_t mutexDC ;

sem_t bridge ;
int count_cd=0 ;
int count_dc=0 ;

int staircaseBC[STEPS_BC+1]={0} ;
int staircaseCB[STEPS_BC+1]={0} ;
int narrowBridge [STEPS_CD+1] = {0} ;


void * AB(void *arg){

int i;
int id = *((int *) arg);

for(i=0 ; i< STEPS_AB ;i=i+speed[id]){
    if(i+speed[id] > STEPS_AB ){
        printf("Person %d moved along AB from  %d to %d\n",id,i,STEPS_AB);
    }
    else printf("Person %d moved along AB from  %d to %d\n",id,i,i+speed[id]);
    sleep(1);
}

 //   CD(arg);
     BC(arg) ;

}



void * BC(void *arg){

int i,j,currP=1,distance=0,nextP;
int id = *((int *) arg);

     //   printf("| Person %d Entering BC |\n",id);
    while ( 1 ){

        pthread_mutex_lock (&mutexBC) ; // enter critical region

        for( i = currP +1 ; i<= ( currP+speed[id] ) ; i++ ){
               // printf("%d ",staircase[i]);
                if(staircaseBC[i]) break ;

        }

        staircaseBC[i-1] = 1 ;// one step behind
        nextP =  i - 1  ;
        staircaseBC [ currP ] = 0 ; // ager jaygay nai akhn
        if(currP == nextP){ printf("Person %d  Don't move\n",id) ; }

        else { printf("Person %d moved along BC from  %d to %d\n",id,currP,nextP);
        currP = nextP  ;

        }


        pthread_mutex_unlock (&mutexBC) ;

        sleep(1) ;

        if(currP >= STEPS_BC) {

                staircaseBC [ STEPS_BC ] = 0 ;
                break ; }
    }

        CD(arg);
    // DD(arg);

}

void * CB(void *arg){

int i,j,currP=STEPS_BC,distance=0,nextP;
int id = *((int *) arg);


     //   printf("| Person %d Entering CB | \n",id);
    while ( 1 ){


        pthread_mutex_unlock (&mutexCB) ; // enter critical region

        for( i = currP -1 ; i>= ( currP-speed[id] ) ; i-- ){

                if(staircaseCB[i]) break ;

        }

        staircaseCB[i+1] = 1 ;// one step behind
        nextP =  i + 1  ;
        staircaseCB [ currP ] = 0 ; // ager jaygay nai akhn
        if(currP == nextP){ printf("person %d  daraiya ase\n",id) ; }

        else { printf("Person %d moved along CB from  %d to %d\n",id,currP,nextP);
        currP = nextP  ;

        }


        pthread_mutex_unlock (&mutexCB) ;
    //    printf("Person %d berHoise code theke \n",id);
        sleep(1) ;

        if(currP <= 1) {
              //  printf("Person %d Leaving CB",id) ;

                staircaseCB [ 1 ] = 0 ;
                break ; }
    }

    BA(arg);

}

void * CD ( void * arg){

    int i,j,currP=1,distance=0,nextP;
    int id = *((int *) arg);
    pthread_mutex_lock(&mutexCD) ;
            count_cd++ ;
        //    printf("\nCD te ase %d jon\n",count_cd);
            if(count_cd==1) sem_wait(&bridge);
    pthread_mutex_unlock(&mutexCD) ;

    while(1){
      //  printf("Person %d Dhukse cd te \n",id);
        pthread_mutex_lock(&mutexCD) ;


        for( i = currP +1 ; i<= ( currP+speed[id] ) ; i++ ){
               // printf("%d ",staircase[i]);
                if(narrowBridge[i]) break ;

        }
        narrowBridge[i-1] = 1 ;// one step behind
        nextP =  i - 1  ;
        narrowBridge [ currP ] = 0 ; // ager jaygay nai akhn

        if(currP == nextP){ printf("Person %d  Don't move\n",id) ; }
        else { printf("Person %d moved along CD from  %d to %d\n",id,currP,nextP);
        currP = nextP  ;
       // distance+= nextP ;
        }
        pthread_mutex_unlock (&mutexCD) ;

     //   printf("Person %d berHoise cd theke \n",id);
        sleep(1) ;

        if(currP >= STEPS_CD) {
           //     printf("Person %d Leaving CD",id) ;

                narrowBridge[ STEPS_CD ] = 0 ;
                break ; }

    }
     pthread_mutex_lock(&mutexCD) ;
            count_cd-- ;
            printf("\nCD te ase %d jon\n",count_cd);
            if(count_cd==0) sem_post(&bridge);
     pthread_mutex_unlock(&mutexCD) ;

     DD(arg) ;

}

void * DC (void * arg){

    int i,j,currP=STEPS_CD,distance=0,nextP;
    int id = *((int *) arg);
    pthread_mutex_lock(&mutexDC) ;
        count_dc++ ;
        printf("\nDC te ase %d jon\n\n",count_dc);
        if(count_dc==1) sem_wait(&bridge);
    pthread_mutex_unlock(&mutexDC) ;

    while(1){
     //  printf("Person %d Dhukse cd te \n",id);
        pthread_mutex_lock(&mutexDC) ;


        for( i = currP -1 ; i>= ( currP-speed[id] ) ; i-- ){
               // printf("%d ",staircase[i]);
               if( i == 0 ){ i=i-1;break ; }
               if(narrowBridge[i]) break ;

        }
        narrowBridge[i+1] = 1 ;// one step behind
        nextP =  i + 1  ;
        narrowBridge [ currP ] = 0 ; // ager jaygay nai akhn

        if(currP == nextP){ printf("Person %d  Don't move\n",id) ; }
        else { printf("Person %d moved along DC from  %d to %d\n",id,currP,nextP);
        currP = nextP  ;
       // distance+= nextP ;
        }
        pthread_mutex_unlock (&mutexDC) ;

     //   printf("Person %d berHoise cd theke \n",id);
        sleep(1) ;

        if(currP <= 1) {
           //     printf("Person %d Leaving DC",id) ;
                //last pos 0 kor
                narrowBridge [1] = 0 ;
                break ; }

    }
    pthread_mutex_lock(&mutexDC) ;
        count_dc-- ;
        printf("\nDC te ase %d jon\n\n",count_dc);
        if(count_dc==0) sem_post(&bridge);
    pthread_mutex_unlock(&mutexDC) ;
    CB(arg);
}

void * BA (void *arg){
    int i;
    int id = *((int *) arg);

    for( i = STEPS_AB ; i > 0 ; i = i - speed[id]  ){
        if(i-speed[id] < 0 ){
        printf("Person %d moved along BA from  %d to %d\n",id,i,0);
        }
        else printf("Person %d moved along BA from  %d to %d\n",id,i,i-speed[id]);
        sleep(1);
    }

    printf("\nPerson %d has completed his walk\n\n",id);


}

void * DD(void * arg){
    int i;
    int id = *((int *) arg);
   // pthread_mutex_lock(&mutex);  //experiment
    for(i=1; i< STEPS_DD ;i=i+speed[id]){
        if(i+speed[id] > STEPS_DD ){
        printf("Person %d moved along DD from  %d to %d\n",id,i,STEPS_DD);
            }
        else printf("Person %d moved along DD from  %d to %d\n",id,i,i+speed[id]);
        sleep(1);
    }
  //  pthread_mutex_unlock(&mutex); // exp
     //  DC(arg) ;
     DC(arg) ;
    //CB(arg);
}

void readFile(){

    char * line = NULL;
    size_t len = 0;
    int _size = 0;
    int digit ,i=0;
    ssize_t read;

    fp = fopen("1405050.txt", "r");
    if (fp == NULL)
        exit(EXIT_FAILURE);

    while ((read = getline(&line, &len, fp)) != -1) {
        if(i==0) {_size = *line -'0' ;N=_size ;}
        digit = *line -'0' ;
        speed[i-1]=digit ;
        i++;
    }
  //  for ( i =0 ;i<N ;i++)printf("%d ",speed[i]);

}


int     main()
{

    readFile();
    pthread_t person[N];
    pthread_mutex_init(&mutexBC, NULL);
    pthread_mutex_init(&mutexCB, NULL);
    pthread_mutex_init(&mutexDC, NULL);
    pthread_mutex_init(&mutexCD, NULL);
    int i;
    void * result ;
    sem_init(&bridge , 0, 1 )  ; //initialize semaphore



    for (i = 0; i < N; i++ ) {
        int *arg = malloc(sizeof(*arg));
        if ( arg == NULL ) {
            fprintf(stderr, "Couldn't allocate memory for thread arg.\n");
            exit(EXIT_FAILURE);
        }

        *arg = i;
        pthread_create(&person[i], NULL, AB, arg);
    }
    for (i = 0; i < N; i++ ) {
    pthread_join(person[i],NULL) ;


    }
    pthread_mutex_destroy(&mutexBC);
    pthread_mutex_destroy(&mutexCB);
    pthread_mutex_destroy(&mutexCD);
    pthread_mutex_destroy(&mutexDC);

    fclose(fp);

    return 0;
}
