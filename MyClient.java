import java.io.*;  
import java.net.*;  
public class MyClient {  
    public static void main(String[] args) {  
        try{      
            Socket socket=new Socket("localhost",50000);  
            BufferedReader dis = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            DataOutputStream dout=new DataOutputStream(socket.getOutputStream());  
            
            dout.write(("HELO\n").getBytes());  
            dout.flush(); 
            
            String  str=(String)dis.readLine();  
            System.out.println("Server message= "+str);
            dout.flush();

            dout.write(("AUTH 45961735\n").getBytes());
            dout.flush();

            str = dis.readLine();  
            System.out.println("Server message= "+str);
            dout.flush();

            dout.write(("REDY\n").getBytes());
            dout.flush();

            str = dis.readLine();  
            System.out.println("Server message= "+str);
            dout.flush();

            dout.write(("QUIT\n").getBytes());
            dout.flush();

            str = dis.readLine();  
            System.out.println("Server message= "+str);
            dout.flush();

            dout.close();  
            socket.close();  
        }catch(Exception e){System.out.println(e);}  
    
    }  
}  