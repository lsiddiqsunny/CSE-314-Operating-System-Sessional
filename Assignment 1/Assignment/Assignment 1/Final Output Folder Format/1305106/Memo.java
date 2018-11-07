import java.util.HashMap;
import java.util.Map;
import java.util.*;


public class Memo {

    //private HashMap<Integer, ArrayList<Integer> > fmap;
    public ArrayList<Integer>[] filelist;
    public ArrayList<Integer>[] fmap;

    public HashMap<Integer, String >  chunk;
    public int p,i;
    public int [] complete;
    public int [] chunksize;
    public int [] chunkvis;
    public int [] totfile;
    public int [] filesize;
    public String[] filename;


    Memo()
    {
        p=1;
        fmap = (ArrayList<Integer>[])new ArrayList[1000];
        filelist = (ArrayList<Integer>[])new ArrayList[1000];

        for (int i = 0; i < 1000; i++) {
            fmap[i] = new ArrayList<Integer>();
            filelist[i] = new ArrayList<Integer>();
        }

        //fmap = new HashMap<>();
        chunk = new HashMap<>();
        chunkvis = new int[1001];
        complete=new int[1001];
        chunksize = new int[1001];
        filesize=new int[1001];
        filename=new String[1001];

        totfile=new int[1001];
        for(i=0;i<1000;i++)
        {
            complete[i]=0;
            chunkvis[i]=0;
            chunksize[i]=0;
            totfile[i]=0;
            filesize[i]=0;
        }


    }

    public int Getfile(int roll,int siz,String s)
    {
        //System.out.println("file requested by roll "+roll);

        p++;
        filelist[roll].add(p-1);
        filesize[p-1]=siz;
        filename[p-1]=s;
        return p-1;
    }

    public int Getchunk(int id,int roll,String s,int size)
    {
        for(i=0;i<1000;i++)
        {
            if(chunkvis[i]==0)
                break;
        }
        if(i==1000)
            return 0;
        fmap[id].add(i);
        chunk.put(i,s);
        chunksize[i]=size;
        chunkvis[i]=1;
        //System.out.println("chunk "+i+" got by string "+s);

        return 1;
    }

    public void comp(int id,int roll)
    {
        //System.out.println("comp entered by roll "+roll);

        complete[id]=1;
        totfile[roll]++;

    }

    public void del(int id,int roll)
    {
        //System.out.println("del entered by roll "+roll);

        complete[id]=0;
        totfile[roll]--;
        for (int i = 0; i < fmap[id].size(); i++) {
            int p=fmap[id].get(i);
            chunkvis[p]=0;
            chunksize[p]=0;
        }

        fmap[id].clear();
        filelist[roll].remove(new Integer(id));


    }

    public void ck(int id)
    {
        for (int i = 0; i < fmap[id].size(); i++) {
            int p=fmap[id].get(i);
            //System.out.println("chunk "+i+" "+chunk.get(p));

        }
    }

    public int getfile(int roll)
    {
        if(filelist[roll].size()!=0)
             return filelist[roll].get(0);
        return -1;

    }

    public int getlen(int id)
    {
        return fmap[id].size();
    }
    public String getchunkat(int id,int i)
    {
        int ind=fmap[id].get(i);
        return chunk.get(ind);
    }









}
