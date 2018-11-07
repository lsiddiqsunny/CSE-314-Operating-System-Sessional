/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package myassignment;

public class FileCopy {

    String fileName;
    int length;
    byte[] dataArray;

    public FileCopy(String fileName, byte[] array) {
        this.fileName = fileName;
        this.length = array.length;
        this.dataArray = new byte[this.length];
        System.arraycopy(array, 0, this.dataArray, 0, length);
    }

}