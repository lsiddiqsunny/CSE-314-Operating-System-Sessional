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
public class test_main {
    public static void main(String args[]){
        String str="001111111000111111000111";
        bit_stuff bsf=new bit_stuff(str);
        String res;
        res=bsf.bit_stuffer();
        System.out.println(res);
    }
}
