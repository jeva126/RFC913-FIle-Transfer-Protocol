package Server;
/**
 * Code is taken from Computer Networking: A Top-Down Approach Featuring 
 * the Internet, second edition, copyright 1996-2002 J.F Kurose and K.W. Ross, 
 * All Rights Reserved.
 **/

 /**
  * @author Jordy Evans 
  * Implementation of RFC913 Protocol
 */


import java.io.*; 
import java.net.*;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.text.SimpleDateFormat;




class TCPServer { 

	// static variables to be used throughout the server

	// authentication variables
	static String user;
	static String password;
	static String account;

	static Boolean userAuth;
	static Boolean passwordAuth;
	static Boolean accountAuth;

	// directory variables
	static String currentDirectory = System.getProperty("user.dir") + "/files";
	static String previousDirectory ;

	// rename variables
	static String oldName;
	static Boolean rename = false;

	// send type variable. B by default
	static String sendType = "B";

	// retrieval variables
	static String fileToSend;
	static Boolean retrieval = false;

	// records total btye transaction of session
	static int totalBytesSent = 0;

	// used in binary mode B
	static DataOutputStream binaryToClient;
	static DataInputStream binaryFromClient;

	// STOR variables
	static String storMode;
	static Boolean storing = false;
	static String storName;
	static Integer storSize;

    
    public static void main(String args[]) throws Exception 
    { 

		// ensure no user is logged in
		userAuth = false;
		passwordAuth = false;
		accountAuth = false;
		
		Boolean done = false;
		// Check program arguments for port 
		if (args.length == 1){

			System.out.println("Greetings from server side. You want to use port number " + args[0]);
		} else {
			System.out.println("ARG ERROR: Wrongs argument. Needs to have 1 argument: PORT ");
			return;
		}

		String clientSentence; 
			
		System.out.println("Opening Socket ..");
		ServerSocket welcomeSocket = new ServerSocket(Integer.parseInt(args[0])); 
		System.out.println("Now Listening on Port " + args[0] + " !");

			
		Socket connectionSocket = welcomeSocket.accept(); 
	
		BufferedReader inFromClient = 
		new BufferedReader(new
		InputStreamReader(connectionSocket.getInputStream())); 
		
		DataOutputStream  outToClient = 
		new DataOutputStream(connectionSocket.getOutputStream()); 

		// used in binary mode
		binaryToClient = new DataOutputStream(new BufferedOutputStream(connectionSocket.getOutputStream()));
		binaryFromClient = new DataInputStream(new BufferedInputStream(connectionSocket.getInputStream()));
			
		while(true) { 

			// read client sentence
			clientSentence = inFromClient.readLine(); 
			// seperate argument from command
			String[] clientCmd = clientSentence.split(" "); 
			// print command on server console (used for debugging and info)
			System.out.println(clientCmd[0]);

			// for commands USER to DONE below, no need for authentication
			// all other commands require authentication
			switch(clientCmd[0]){
				case "USER":
					outToClient.writeBytes(USER(clientCmd[1]));
					break;
				case "PASS":
					outToClient.writeBytes(PASS(clientCmd[1]));
					break;
				case "ACCT":
					outToClient.writeBytes(ACCT(clientCmd[1]));
					break;
				case "DONE":
					outToClient.writeBytes("+Closing Connection. Goodbye \nTotal of " + totalBytesSent + " bytes sent\n\0");
					done = true;
					break;
				case "TYPE":
					// check authentication using thie checkAuthenticated() function
					if (checkAuthenticated()){
						outToClient.writeBytes(TYPE(clientCmd[1]));
					} else {
						outToClient.writeBytes("-Cannot use " + clientCmd[0] + ". Not logged in\n\0");
					}
					break;
				case "LIST":
					if (checkAuthenticated()){
						outToClient.writeBytes(LIST(clientCmd[1]));
					} else {
						outToClient.writeBytes("-Cannot use " + clientCmd[0] + ". Not logged in\n\0");
					}
					break;
				case "CDIR":
					if (checkAuthenticated()){
						outToClient.writeBytes(CDIR(clientCmd[1]));
					}else{
						outToClient.writeBytes("-Cannot use " + clientCmd[0] + ". Not logged in\n\0");
					}
					break;
				case "KILL":
					if (checkAuthenticated()){
						outToClient.writeBytes(KILL(clientCmd[1]));
					}else{
						outToClient.writeBytes("-Cannot use " + clientCmd[0] + ". Not logged in\n\0");
					}
					break;
				case "NAME":
					if (checkAuthenticated()){
						outToClient.writeBytes(NAME(clientCmd[1]));
					}else{
						outToClient.writeBytes("-Cannot use " + clientCmd[0] + ". Not logged in\n\0");
					}
					break;
				case "TOBE":
					if (checkAuthenticated()){
						outToClient.writeBytes(TOBE(clientCmd[1]));
					}else{
						outToClient.writeBytes("-Cannot use " + clientCmd[0] + ". Not logged in\n\0");
					}
					break;
				case "RETR":
					if (checkAuthenticated()){
						outToClient.writeBytes(RETR(clientCmd[1]));
					}else{
						outToClient.writeBytes("-Cannot use " + clientCmd[0] + ". Not logged in\n\0");
					}
					break;
				case "SEND":
					if (checkAuthenticated()){
						if (retrieval){
							SEND(outToClient);
							// hold() function below is only required for running the test
							// still works with manual use though
							hold(1000); 
						       	outToClient.writeBytes("+File sent\0");
						} else{
							outToClient.writeBytes("-No file selected. Use RETR <file>");
						}
					}else{
						outToClient.writeBytes("-Cannot use " + clientCmd[0] + ". Not logged in\n\0");
					}
					break;
				case "STOP":
					if (checkAuthenticated()){
						outToClient.writeBytes(STOP());
					}else{
						outToClient.writeBytes("-Cannot use " + clientCmd[0] + ". Not logged in\n\0");
					}
					break;
				case "STOR":
					if (checkAuthenticated()){
						outToClient.writeBytes(STOR(clientCmd[1], clientCmd[2]));
					}else{
						outToClient.writeBytes("-Cannot use " + clientCmd[0] + ". Not logged in\n\0");
					}
					break;
				case "SIZE":
					if (checkAuthenticated()){
						outToClient.writeBytes(SIZE(clientCmd[1]));
						outToClient.writeBytes(receiveFile(inFromClient));
					}else{
						outToClient.writeBytes("-Cannot use " + clientCmd[0] + ". Not logged in\n\0");
					}
					break;
				default:
					outToClient.writeBytes("Invalid Command\n\0");
				break;
			}
			if(done){break;}
		}
		
		// when DONE command is sent the while loop breaks and program finishes here
		System.out.println("Closing socket. Goodbye");
		connectionSocket.close();
    } 


	// USER command
	public static String USER(String userAttempt){
        String response = null;
		Boolean testUser = false;

		// checkUserInfo() reads user information from authorization.txt
		userAuth = false;
		checkUserInfo();

		if (user.equals(userAttempt)){
			userAuth = true;
		}
		// this can be uncommented to enable test user for easy testing
		// else if(userAttempt.equals("testUser")){
		// 	userAuth = true;
		// 	accountAuth = true;
		// 	passwordAuth = true;
		// 	testUser = true;
		// 	response = "! Test user logged in \n\0";
		// }

		if (!userAuth){
			response = "-Invalid user-id, try again\n\0";
		} else{
			if( !testUser){
				response = "+User-id valid, send account and password\n\0";
			}	
		}
		return response;
	}

	// PASS command
	public static String PASS(String userAttempt){
		String response = null;
		passwordAuth = false;
		checkUserInfo();

		if (password.equals(userAttempt)){
			passwordAuth = true;
		}

		if (!passwordAuth){
			response = "-Wrong password, try again\n\0";
		} else if (!accountAuth){
			response = "!Send account\n\0";
		} else{
			response = "!Logged in as " + user + "\n\0";
		}
		return response;
	}

	// ACCT command
	public static String ACCT(String userAttempt){
		String response = null;
		accountAuth = false;
		checkUserInfo();

		if (account.equals(userAttempt)){
			accountAuth = true;
		}

		if (!accountAuth){
			response = "-Invalid account, try again\n\0";
		} else if (!passwordAuth){
			response = "+Account valid, send password\n\0";
		} else{
			response = "!Logged in as " + user + "\n\0";
		}
		return response;
	}

	// TYPE command 
	public static String TYPE(String userType){
		String response = "";

		switch (userType){
			case "A":
				sendType = "A";
				response = "+Using Ascii mode";
				break;
			case "B":
				sendType = "B";
				response = "+Using Binary mode";
				break;
			case "C":
				sendType = "C";
				response = "+Using Continuous mode";
				break;
			default:
				response = "-Type not valid";
		}
		response += '\0';
		return response;
	}

	// LIST command
	public static String LIST(String userListMode){
		String response = "";
		File file = new File(currentDirectory);
		File[] files = file.listFiles();
		
		
		if ("F".equals(userListMode)){	// F mode
			response = "+" + currentDirectory + "\n";
			for (File f : files){
				response += f.getName() + "\n";
			}	
		}else if("V".equals(userListMode)){ // V mode
			response = "+" + currentDirectory + "\n";

			// formatter for the date
			SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:MM:SS");

			for (File f : files){

				response += f.getName() + " | Last Modified: " + sdf.format(f.lastModified()) + " | Size: " + f.length() + " bytes \n";
			}	
		}else{
			response +="-Please use F or V as argument";
		}
		response += '\0';
		return response ;
	}
	
	//CDIR command
	public static String CDIR(String userDirectory){
		String response = "";
		File file = new File(currentDirectory + "/" + userDirectory);
		
		if (!file.exists()){
			response = "-Can't connect to directory because it does not exist\0";
			return response;
		}

		// update current Directory
		if (file.isDirectory()){
			previousDirectory = currentDirectory;
			currentDirectory += "/" + userDirectory;	
			response = "!Changed working dir to " + currentDirectory + "\0";
		}
		return response;
	}
	
	//KILL command
	public static String KILL(String userFile){
		String response = "";
		File file = new File(currentDirectory + "/" + userFile);
		Path filePath = file.toPath();
		
		try {
			Files.delete(filePath);
			response = "+" + filePath.getFileName() + " deleted";
		} catch (NoSuchFileException e) {
			response = "-Not deleted because there is no such file in the directory";
		} catch (IOException e){
			response = "-Not deleted because of IO error";
		}	
		response += "\0";
		return response;
	}
	
	//NAME command
	public static String NAME(String userFileName){
		String response = "";
		
		File file = new File(currentDirectory + "/" + userFileName);
		
		if (!file.exists()){
			response = "-Can't find " + userFileName;
		}else {
			response = "+File exists. Send TOBE <new-name>";
			oldName = userFileName;
			rename = true;
		}
		
		response += '\0';
		return response;
	}
	
	//TOBE command
	public static String TOBE(String userNewName){
		String response = "";
		
		if (rename){
			File file = new File(currentDirectory + "/" + oldName);
			File newFile = new File(currentDirectory + "/" + userNewName);
			
			if (newFile.isFile()){
				response = "-File wasn't renamed because a file already exists with that name";
			} else{
				file.renameTo(newFile);
				rename = false;
				response = "+" + oldName + " renamed to " + newFile.getName(); 
			}
		} else{
			response = "-File wasn't renamed because you have not specified the file to rename. use NAME <file-name>";
		}
		response += '\0';
		return response;
	}
	
	// RETR command
	public static String RETR(String userFileReq){
		String response= "";
		File file = new File(currentDirectory + "/" + userFileReq);

		if (file.exists()){
			response = file.length() + "";
			fileToSend = currentDirectory + "/" + userFileReq;
			retrieval = true;
		}else{
			response = "-File doesn't exist";
		}
		response += '\0';
		return response;
	}

	//SEND command
	public static void SEND(DataOutputStream outToClient){

		if (retrieval){ // check that a RETR has been sent
			File file = new File(fileToSend);
			byte[] bytes = new byte[(int) file.length()];	
			if (sendType.equals("A")){ // ASCII
				try (BufferedInputStream bufStream = new BufferedInputStream(new FileInputStream(file))){
					outToClient.flush();
					int i = 0;
					// send the bytes of the selected file
					while ((i = bufStream.read(bytes)) >= 0) {
						outToClient.write(bytes, 0, i);
					}
					bufStream.close();
					outToClient.flush();
					totalBytesSent += file.length();	
				} catch (Exception e){
					return;
				}
			} else { // B or C sendType
				try (FileInputStream fStream = new FileInputStream(file)){
					binaryToClient.flush();
					int e;
					// send the bytes of the selected file
					while ((e = fStream.read()) >= 0){
						binaryToClient.write(e);
					}
					fStream.close();
					binaryToClient.flush();
					totalBytesSent += file.length();
				} catch (IOException e){
					return;
				}
				retrieval = false;
			}
		}
		
		return;
	}

	//STOP command
	public static String STOP(){
		String response = "";

		if (retrieval) {
			retrieval = false;
			response = "+ok, RETR aborted";
		}else {
			response = "-no RETR started. Nothing to be done";
		}
		response += '\0';
		return response;
	}

	//STOR command
	public static String STOR(String userMode, String userFileName){
		String response = "";
		storName = userFileName;
		File file = new File(currentDirectory + "/" + userFileName);

		switch (userMode){
			case "NEW":
				if (file.isFile()){
					storMode = "NEW";
					response = "+File exists, will create a new generation of file";
				} else{
					storMode = "CREATE";
					response = "+File does not exist, will create a new file";
				}
				storing = true;
				break;
			case "OLD":
				if (file.isFile()){
					storMode = "OLD";
					response = "+Will write over old file";
				}else{
					storMode = "CREATE";
					response = "+Will create a new file";
				}
				storing = true;
				break;
			case "APP":
				if (file.isFile()){
					storMode = "APP";
					response = "+Will append to file";
				} else{
					storMode = "CREATE";
					response = "+Will create file";
				}
				storing = true;
				break;
			default:
				response = "Invalid request";
		}
		response += '\0';
		return response;
	}

	//SIZE command
	public static String SIZE(String sizeBytes){
		String response = "";
		storSize = Integer.parseInt((sizeBytes));
		File dir = new File(currentDirectory);

		if (dir.getFreeSpace() > storSize){
			response = "+ok, waiting for file";
		} else{
			response = "-Not enopugh room, don't send it";
		}
		response += '\0';
		return response;
	}

	/////////////////////////////////////////////////////////////////// helpers

	// receive file from stor command
	public static String receiveFile(BufferedReader inFromClient) throws IOException{
		String response = "";
		File file = new File(currentDirectory + "/" + storName);

		if ("A".equals(sendType)){ // ASCII
			try (BufferedOutputStream bufStream = new BufferedOutputStream(new FileOutputStream(file, "APP".equals(storMode)))){
				for (int i = 0; i < storSize; i++){
					bufStream.write(inFromClient.read());
				}
				bufStream.flush();
				bufStream.close();
				totalBytesSent += storSize;
			}
		} else{ // Binary or Continous
			try (FileOutputStream fStream = new FileOutputStream(file, "APP".equals(storMode))){
				int e;
				int i = 0;
				byte[] bytes = new byte[(int) storSize];
				while (i < storSize){
					e = binaryFromClient.read(bytes);
					fStream.write(bytes, 0, e);
					i += e;
				}
				fStream.flush();
				fStream.close();
				totalBytesSent += storSize; // accumulate bytes exchanged
			}
		}
		response = "+" + storName + " saved";
		response += '\0';
		return response;
	}

	// check user logged in before doing anything
	public static boolean checkAuthenticated(){
		Boolean isAuthenticated = false;

		if (userAuth && passwordAuth && accountAuth){
			isAuthenticated = true;
		}
		return isAuthenticated;
	}

	// extracts login details from authorization.txt
	public static void checkUserInfo(){
		File file = new File("authorization.txt");
        BufferedReader reader = null;
        String text;
        
		try {
			reader = new BufferedReader(new FileReader(file));
			while ((text = reader.readLine()) != null) {
				String temp = text;
				String[] userDetails = temp.split(" ", -1);
				user = userDetails[0];
				account = userDetails[1];
				password = userDetails[2]; 
			}
		} catch (IOException e){
			System.out.println("Exception sorry\n");
		} finally{
			try{
				if (reader != null) {
					reader.close();
				}
			}catch (IOException e){
				System.out.println("IO Exception\n");
			}
		}
	}

	// sleep for 1 second for testing
	public static void hold(int ms){
		try {
			Thread.sleep(ms);
		} catch(InterruptedException ex) {
			Thread.currentThread().interrupt();
		}
	}
} 

