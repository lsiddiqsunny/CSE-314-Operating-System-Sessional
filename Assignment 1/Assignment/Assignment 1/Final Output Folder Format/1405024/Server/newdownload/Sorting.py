num=int(input())
a=[int(i) for i in input().split()]
a.sort()

for i in range(0,num,1):

    if(i==num-1 and a[i]==1):
        print('2',end=' ')
    elif (i == 0):
        print('1', end=' ')
    else:
        print(a[i-1],end=' ')