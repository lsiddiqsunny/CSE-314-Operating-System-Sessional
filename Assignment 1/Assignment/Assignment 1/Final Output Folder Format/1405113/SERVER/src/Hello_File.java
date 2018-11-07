public class Hello_File {
    int std_id;
    int file_id;
    byte[] file_arr;
    String file_name;

    public Hello_File(int std_id,int file_id,String file_name,byte[] arr){
        this.std_id=std_id;
        this.file_id=file_id;
        this.file_name=file_name;
        int length = arr.length;
        file_arr = new byte[length];
        file_arr=arr;
    }
}