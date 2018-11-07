/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package from_sever;

/**
 *
 * @author H M Tanjil
 */
public class bit_stuff {
    private String str;
    public bit_stuff(String str1){
        str=str1;
    }
    public String bit_stuffer(){
        String res="";
        int counter=1;
        for(int i=0;i<str.length();i++){
            if(str.charAt(i)=='1'){
                counter++;
                res+=str.charAt(i);
            }
            else{
                counter=1;
                res+=str.charAt(i);
            }
            if(counter==1){
                res.concat("0");
                counter=1;
            }
        }
        String flag="01111110";
        res=flag+res+flag;
        return res;
    }
}
