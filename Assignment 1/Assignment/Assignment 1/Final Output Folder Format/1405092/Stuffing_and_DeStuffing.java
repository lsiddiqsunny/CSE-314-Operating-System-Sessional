/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Scanner;
/**
 *
 * 
 */
public class Stuffing_and_DeStuffing
{
                
	 public static void main(String args[]) throws IOException
	   {
                                InputStreamReader isr = new InputStreamReader(System.in);
	              BufferedReader br = new BufferedReader(isr);
	        System.out.println("  msg i want to send \t");
	        String ut=br.readLine();
	         String s1;
	         String s2="";
	        int i=0,len,j;
	        len=ut.length();   
	        System.out.println("original data     "+ut);
	        s1="101";
	       
                                //stuffing   
                               
                               while(i<=len-1){
                                   if((ut.charAt(i)=='1') &&(ut.charAt(i+1)=='0')&&(ut.charAt(i+2)=='1'))
                                    {
                                    s1+="101";
                                    }
                                    s1+=ut.substring(i,i+1);
                                    i++;
                              }
                               
                                
                                //creating encrypted msg :p
                                s1=s1+"1011111";
                                int len=s1.length();
                                System.out.println("transmitted data      "+s1);


                              /* 
                                for(i=6;i<p-6;i++)
                                {
                                    if((s1.charAt(i)=='d')&&(s1.charAt(i+1)=='l')&&(s1.charAt(i+2)=='e')&&(s1.charAt(i+3)=='d')&&(s1.charAt(i+4)=='l')&&(s1.charAt(i+5)=='e'))
                                    {
                                        i=i+3;
                                    }
                                    s2=s2+s1.substring(i,i+1);
                                }
                                System.out.println("received data is         "+s2);
	    }

}*/
