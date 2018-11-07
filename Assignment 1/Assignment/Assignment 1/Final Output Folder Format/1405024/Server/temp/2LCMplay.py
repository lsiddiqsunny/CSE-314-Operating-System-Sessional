# Allah is the most gracious and the Most Marchiful
num,m,k=map(int,input().split())
na=num-m
if((na+1)*(k-1)>=m):
    print(m)
else:
    sa=na*(k-1)
    nsa=m-sa
    #print(nsa)
    M=(10**9)+9
    ans=(pow(2,(nsa//k)+1,M)-2)*k
    ans+=nsa%k
    ans+=sa
    print((ans+0)%1000000009)













