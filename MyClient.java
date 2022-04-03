import java.io.*;  
import java.net.*;
import java.io.File;
import java.util.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;


public class MyClient {  
    public static void main(String[] args) {  
        try{      
            Socket socket=new Socket("localhost",50000); //new socket for connection with port 
            BufferedReader dis = new BufferedReader(new InputStreamReader(socket.getInputStream())); //buffer reader to read 
            DataOutputStream dout = new DataOutputStream(socket.getOutputStream());  //output stream to output text
            String str;
            String[] currJob;
            String getsData;
            int nRecs;

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

            if (str.equals("OK")){
                Document doc = readFile("./ds-system.xml"); // read the ds system for server info

                NodeList nList = doc.getElementsByTagName("server"); // find server names
                String[][] servers = getServerList(nList); //put them in list
                
                int max_core_count = Integer.MIN_VALUE; // storage for max core count
                String server_max = "";
                String server_id = "";
                
                for(int i = 0; i< servers.length; i++) { //go through list and try to find which is biggest
                    if (Integer.parseInt(servers[i][1]) > max_core_count) {
                        max_core_count = Integer.parseInt(servers[i][1]);
                        server_max = servers[i][0];
                        server_id = "0";
                    }
                }
            }

            while (!str.equals("NONE")){ // check if the message is not none
                dout.write(("REDY\n").getBytes()); //send ready handshake
                dout.flush();
    
                str = dis.readLine();  //receive
                System.out.println("SERVER: "+str);
                currJob = str.split(" ",0);
                dout.flush();


                while (currJob[0].equals("JCPL")) {    // check if jcpl if there is 
                    dout.write(("REDY\n".getBytes()));   // then ignore
                    str = dis.readLine();
                    currJob = str.split(" ");
                }

                if (str.equals("NONE")) { //if there are no more jobs left then leave while loop
                    break;
                }

                dout.write(("OK\n").getBytes()); // acknlowedge the data
                dout.flush();

                str = dis.readLine();  //receive
                System.out.println("SERVER: "+str);
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

    public static Document readFile(String path) {
        try {
            File file = new File(path);
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document document = db.parse(file);
            document.getDocumentElement().normalize();
            return document;
        }

        catch (Exception e) {
            System.out.println(e);
            return null;
        }
    }

    public static String[][] getServerList(NodeList nList){
        String[][] xml = new String[nList.getLength()][2];
            for(int i =0 ; i< nList.getLength(); i++){
                Node nNode = nList.item(i);
                if(nNode.getNodeType()== Node.ELEMENT_NODE){
                    Element eElement = (Element) nNode;
                    xml[i][0]=eElement.getAttribute("type");
                    xml[i][1]= eElement.getAttribute("coreCount").toString();
                }
            }


        return xml;
    }
}  