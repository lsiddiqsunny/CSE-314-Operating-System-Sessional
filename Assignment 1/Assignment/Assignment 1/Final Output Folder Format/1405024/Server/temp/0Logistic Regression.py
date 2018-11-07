import math as M
def delcost(a,b):

    sum=0
    for i in range(4):
        sum+=a[i]*b[i]
    if(sum<-100):
        ans=0.0001
    elif(sum>1000):
        ans=0.95
    else :
        ans=1+M.exp(-1*sum)
        ans=1/ans
    return ans-a[4]
def delcost1(a,b):

    sum=0
    for i in range(4):
        sum+=a[i]*b[i]
    if(sum<-100):
        ans=0.0001
    elif(sum>10000):
        ans=0.99
    else :
        ans=1+M.exp(-1*sum)
        ans=1/ans
    return ans
num=int(input())
inputs=[[] for i in range(num)]
for i in range(num):
    inputs[i]=inputs[i]+[float(i) for i in input().split()]
n=0.1

w=[0,0,0,0]

itr=1


while(itr<20000):
    itr+=1
    update=[0,0,0,0]
    check=0
    for i in range(4):
        for j in range(num):
            temp=delcost(inputs[j],w)
            check+=temp
            update[i]+=temp*inputs[j][i]

    update[i]=update[i]/num
    if(temp/num<.1):
        break;
    for i in range(4):
        w[i]-=n*update[i]


c=[float(i) for i in input().split()]
c.append(0)
res=delcost1(c,w)
if(res>=.5):
    print("1.0")
else :
    print('0.0')
