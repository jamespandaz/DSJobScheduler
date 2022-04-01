import java.io.*;  
import java.net.*;  
public class MyClient {  
    public static void main(String[] args) {  
        try{      
            Socket socket=new Socket("localhost",50000); //new socket for connection with port 
            BufferedReader dis = new BufferedReader(new InputStreamReader(socket.getInputStream())); //buffer reader to read 
            DataOutputStream dout = new DataOutputStream(socket.getOutputStream());  //output stream to output text
            String str;
            String currJob;
            String getsData;

            dout.write(("HELO\n").getBytes());  //handshake start
            dout.flush(); 

            str = dis.readLine();  //recieve
            System.out.println("SERVER: "+str);
            dout.flush();

            dout.write(("AUTH 45961735\n").getBytes()); //auth username
            dout.flush();

            str = dis.readLine();  //recieve
            System.out.println("SERVER: "+str);
            dout.flush();

            while (!str.equals("NONE")){ // check if the message is not none
                dout.write(("REDY\n").getBytes()); //send ready handshake
                dout.flush();
    
                str = dis.readLine();  //receive
                System.out.println("SERVER: "+str);
                currJob = str;
                dout.flush();
                
                dout.write(("GETS All\n").getBytes()); // get server information
                dout.flush();
                str = dis.readLine();  //receive
                System.out.println("SERVER: "+str);
                
                getsData = str; // store the gets All data into a string
                System.out.println("getsData= "+getsData); // print the gets data check what it actaully is 
                dout.flush();

                dout.write(("OK\n").getBytes());
                dout.flush();

                str = dis.readLine();  //receive
                System.out.println("SERVER: "+str);
                dout.flush();

                dout.write(("OK\n").getBytes());
                dout.flush();
            }

            dout.write(("QUIT\n").getBytes());
            dout.flush();

            str = dis.readLine();  
            System.out.println("SERVER: "+str);
            dout.flush();

            dout.close();  
            socket.close();  
        }catch(Exception e){System.out.println(e);}  
    
    }  
}  