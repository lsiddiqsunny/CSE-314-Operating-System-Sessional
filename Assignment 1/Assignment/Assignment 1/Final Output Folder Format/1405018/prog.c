#include <bits/stdc++.h>

using namespace std;

typedef long long ll;
typedef vector<int> vint;
typedef vector<long long> vll;
typedef vector<double>vlf;
typedef vector<string> vstring;
typedef pair<int,int> pint;
typedef pair<long long,long long> pll;
typedef pair<double,double> plf;
typedef vector<int>::iterator vit;
typedef vector<long long>::iterator Vit;

#define FastIO() ios_base::sync_with_stdio(0);cin.tie(0);cout.tie(0)

template <class T> inline T bigmod(T a,T r,T mod)    {
    if(!r)    return 1;
    T ret=bigmod(a,r/2,mod);
    ret=(ret*ret)%mod;
    if(r%2)    ret=((a%mod)*ret)%mod;
    return ret;
}

template <class T> inline T modInv(T x)    {return bigmod(x,MOD-2,MOD);}

bool FermatPrimecheck(ll x,int it)
{
    if(x==1)    return false;
    srand (time(NULL));
    for(int i=0;i<it;i++)    {
        ll a=rand()%(x-1)+1;
        if(bigmod(a,x-1,x)!=1)    return false;
    }
    return true;
}

int main()
{
    FastIO();
    return 0;
}
