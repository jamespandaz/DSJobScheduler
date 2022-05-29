package src;
import java.io.*;  
import java.net.*;
import java.io.File;
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

                NodeList nList = doc.getElementsByTagName("servers"); // find server names in xml
                String[][] servers = getServerList(nList); //put them in list
                
                int max_core_count = Integer.MIN_VALUE; // storage for max core count
                String server_max = "";
                String server_id = "";

                for(int i = 0; i< servers.length - 1; i++) { //go through list and try to find the maximum amount of cores
                    System.out.println(servers[i][1]);
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
                String[] currJob = str.split(" ",0);
                dout.flush();


                while (currJob[0].equals("JCPL")) {    // check if jcpl if there is 
                    dout.write(("REDY\n".getBytes()));   // then ignore
                    str = dis.readLine();
                    currJob = str.split(" ");
                }

                if (str.equals("NONE")) { //if there are no more jobs left then leave while loop and go to exit
                    break;
                }

                avail(currJob, dis, dout); //schedule a job

                str = dis.readLine();  //receive
                System.out.println("SERVER: "+str);
                dout.flush();
            }

            dout.write(("QUIT\n").getBytes()); // quit after all jobs are done
            dout.flush();

            str = dis.readLine();  
            System.out.println("SERVER: "+str);
            dout.flush();

            dout.close();  
            socket.close();  
        }catch(Exception e){System.out.println(e);}  
    
    }

    public static Document readFile(String path) { // to read the system.xml file (uses document builder to parse)
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

    public static String[][] getServerList(NodeList nList){ // create a server list using a node list
        String[][] xml = new String[nList.getLength()][2];
            for(int i =0 ; i< nList.getLength(); i++){
                Node nNode = nList.item(i);
                if(nNode.getNodeType()== Node.ELEMENT_NODE){
                    Element eElement = (Element) nNode;
                    xml[i][0]=eElement.getAttribute("type");
                    xml[i][1]= eElement.getAttribute("coreCount").toString(); // parses corecount into stirng and places in the node list
                }
            }


        return xml;
    }

    public static void avail(String [] job,BufferedReader din, DataOutputStream dout) { // the schd command
        String in;
        String [] inarr;

        try{

            String avail="GETS Avail "+job[4]+" "+job[5]+" "+job[6]+"\n"; //look at availble servers
            dout.write(avail.getBytes());
            in=din.readLine(); 
            inarr=in.split(" "); //put into array

            if(inarr[1].equals("0")){ // checks if no servers are avaible, then passes it onto largest server again
                dout.write("OK\n".getBytes());
                String check=din.readLine();//removes the . reply from server
                handleGETS(job, din, dout);
                return;
            }

            inarr=in.split(" "); //places data into array

            dout.write("OK\n".getBytes());

            String [] capableArray = new String [Integer.parseInt(inarr[1])]; //populate the capable server list
            for(int i=0;i<Integer.parseInt(inarr[1]);i++){
               capableArray[i]=din.readLine();
            }

            int bestIDX=0;
            int bestCore=0;


            dout.write("OK\n".getBytes());

            String check=din.readLine();// removes . from server reply

            String []capableServer=capableArray[bestIDX].split(" "); //splits input into something useable
            String job_schedule = "SCHD" + " " + job[2] + " " + capableServer[0] + " " + capableServer[1] + "\n"; // schdules job
            dout.write(job_schedule.getBytes());



        }
        catch (Exception e) {
            System.out.println(e);
        }
    }

        public static void handleGETS(String [] job,BufferedReader din, DataOutputStream dout) { //If there are no available servers, put job onto server with the most cores
            String in;
            String [] inarr;
    
            try{
                String capable="GETS Capable "+job[4]+" "+job[5]+" "+job[6]+"\n";
                dout.write(capable.getBytes());
                in=din.readLine();
    
                inarr=in.split(" ");
                dout.write("OK\n".getBytes());
                String [] capableArray = new String [Integer.parseInt(inarr[1])];
    
                for(int i=0;i<Integer.parseInt(inarr[1]);i++){
                   capableArray[i]=din.readLine();
                }
    
                dout.write("OK\n".getBytes());
    
                String check=din.readLine();// removes . server reply
    
                int bestIDX=0;
                int bestCore=0;
                for(int j=0;j<Integer.parseInt(inarr[1]);j++){  //Find server with largest core count 
                    String [] test=capableArray[j].split(" ");
                    if(Integer.parseInt(test[4])>bestCore||j==0){
                        bestIDX=j;
                        bestCore=Integer.parseInt(test[4]);
                    }
                 }

                String []capableServer=capableArray[bestIDX].split(" ");
                String job_schedule = "SCHD" + " " + job[2] + " " + capableServer[0] + " " + capableServer[1] + "\n"; //schdules the job 
                dout.write(job_schedule.getBytes());
            }
    
            catch (Exception e) {
                System.out.println(e);
            }
        }
}  