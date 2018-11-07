#print("Allah is the most gracious and the Most Marchiful")

num,l,r,ql,qr,=map(int, input().split())
ara=[int(i) for i in input().split()]
for i  in range(1,num,1):
    ara[i]=ara[i]+ara[i-1]

ans=10**11

for i in range(0,num+1,1):
   tr=num-i
   cl=ara[i-1] if i>0  else 0
   cr=ara[num-1]-cl
   pl=max(0,i-tr-1)
   pr=max(0,tr-i-1)
   tans=pl*ql+pr*qr+cl*l+cr*r
   ans=min(ans,tans)

print(ans)
