# COMPSYS 725 Assignment 1: SFTP - RFC913

This project implements the Simple File Transfer Protocol described in [RFC 913](https://datatracker.ietf.org/doc/html/rfc913). Details of the commands implemented can be found at this link. The project is written in Java and uses port 6789 by default.

**This readme is formatted using markdown so it is recommended to view in a markdown reader**

Author: Jordy Evans \
UPI: jeva126 \
ID: 789464110 

## List of commands

|Command|USER|ACCT|PASS|TYPE|LIST|CDIR|KILL|NAME|DONE|RETR|STOR|
|:-----:|:--:|:--:|:--:|:--:|:--:|:--:|:--:|:--:|:--:|:--:|:--:|
|Client |Yes |Yes |Yes |Yes |Yes |Yes |Yes |Yes |Yes |Yes |Yes |
|Server |Yes |Yes |Yes |Yes |Yes |Yes |Yes |Yes |Yes |Yes |Yes |

## List of components

### SFTPServer.java
This program runs the server side of the project. Currently designed as a single-thread server so only one client can connect at any given time. The program takes one argument, the port number (use 6789).

### SFTPClient.java
This program is used to run the client side. Does not take any argument. Currently configured to use *localhost* and the port number of 6789.

### authorization.txt
This is a simple plain text file used for user information. *User*, *Account*, and *Password* fields are seperated by a space. 

### files folders
In both the server and client directories, there is a folder called *files*. This is the user directory of the server and client.

## File Structure

The file structure of the project is below:

- A1_working
    - README.md
    - /Client
        - /tests
            - runTest
            - startTest
            - testOne.txt
        - /files
            - clientImage.jpg
            - storMe.txt
            - testClient.txt
        - /client
            - TCPClient.class
        - TCPClient.java
    - /Server
        - /files
            - /moreFiles
                - deleteMe.txt
                - testServer2.txt
            - renameMe.jpg
            - retrieveMe.png
            - testServer.txt
        - /Server
            - TCPServer.class
        - authorization.txt
        - TCPServer.java


## Setup Instructions

**Important**


**It is reccomended that the test is run before anything else. This is to ensure that the correct files required for the test are present.**


**Please scroll down to the test instructions and follow those before running the project manually**

To compile the project, it is recommended that everything is done via command line. No IDEs have been used in development so simply open two terminal windows. One in the client directory and one in the server directory. 

Always start the server before the client.

To run the server, use the command in the same directory as TCPServer.java. 

````javac -d . TCPServer.java````

This command compiles the java class, then enter the command

````java Server.TCPServer 6789````

The 6789 argument determines the port number.

To run the client, do a similar command in the client directory. There is no argument required to run the client. By default it connects to port 6789.

````javac -d . TCPClient.java````

As before, this compiles the java class. Now enter the command

````java client.TCPClient````

## User
There is currently one user established in the *authorization.txt* file. The user information is set up as follows

````userOne accOne passOne````

There is also a ````testUser```` configuration that does not require an account or password for testing purposes only.

## Commands
````!````, ````+```` and ````-```` are used as server response codes. 
* `````!````` indicates the user has logged in
* ````+```` indicates successful command
* ````-```` indicates unsuccessful command

# Use the client-server system

None of the commands will be executed until the user is logged in.

The provided authentication file contains the login information for one user. It is suggested that the user use these to demonstrate the functionality of the system. 

Enter the command:

````USER userOne````

The server will reply with 

````+User-id valid, send account and password````

Next, send the command

````ACC accOne````

The server will reply with 

````+Account valid, send password````

Finally, to verify the login details send the command

````PASS passOne````

The server will then reply with 

````!User logged in ````

The client is now logged in and can perform the rest of the commands outlined above. Make sure to be in the correct type in order to transact with files between client and server.

For example, if you want to exchange anything other than a text file, set the type to Binary mode as below

````TYPE B````

The server will reply with

````+Using Binary mode````

For text files, any mode is ok but do not try to send a binary file while in A mode.

# Test Cases

There are a number of shell scripts included to quickly run a series of commands and assess the response from the server.

These test scripts have been tested using macOS BigSur 11.5.1. Additional testing has been done for the ubuntu. 

## macOS and ubuntu
The test script can be found in the the *client/tests* directory. There are numerous files:

* testOne.txt - A list of inputs to be given to the server
* runTest - A script required to run the series of inputs against the server
* startTest - Run this script to begin the tests. Pass the name of the input list you would like to test with as an argument.

### Start the test

To run the test, similarly to the general setup instructions please start the server, the test script will start the client and iterate through the commands. Follow the complete steps below

* Open a terminal window and start the server

    * open terminal window
    *  ````cd A1_working/Server````
    *  ````javac -d . TCPServer.java````
    *  ````java Server.TCPServer 6789````

* In a new terminal window run the test

    * open a new terminal window
    * ````cd A1_working/client/tests````
    * ````./startTest testOne.txt````

Once completed, you will see the list of commands used printed to the terminal window, and following that you will see the responses from the server. Below is an example   

## Test examples

### testOne.txt

This test should be run without any changes being made to the directories of both the *server* and *client*

For marking purposes, it is recommended that this test is run first.

Once the server has been started manually, running this test will automatically compile and run the client. If run correctly, you will see the initial output on the terminal window:

````
******** YOU HAVE CORRECTLY BEGUN THE TESTING PROCESS ********
List of commands to be run:
testOne.txt

Commands: 
USER invalidUser
USER userOne
ACCT invalidAcc
ACCT accOne
PASS invalidPass
PASS passOne
TYPE Z
TYPE A
TYPE C
TYPE B
LIST F
CDIR hello
CDIR moreFiles
KILL noSuchFile
KILL deleteMe.txt
CDIR ..
LIST V
NAME nosuchFile
TOBE newName.jpg
NAME renameMe.jpg
TOBE newName.jpg
RETR noSuchFile
STOP
RETR retrieveMe.png
SEND
STOR NEW storMe.txt
STOR APP storMe.txt
DONE

````

You will then see the interaction between server and client below:

````
******** RUNNING TESTS NOW ********

Welcome to the server
FROM SERVER: -Invalid user-id, try again
FROM SERVER: +User-id valid, send account and password
FROM SERVER: -Invalid account, try again
FROM SERVER: +Account valid, send password
FROM SERVER: -Wrong password, try again
FROM SERVER: !Logged in as userOne

FROM SERVER: -Type not valid
FROM SERVER: +Using Ascii mode
FROM SERVER: +Using Continuous mode
FROM SERVER: +Using Binary mode

FROM SERVER: +<user directory>/A1_working/Server/files
.DS_Store
moreFiles
test.txt
renameMe.jpg
retrieveMe.png

FROM SERVER: -Can't connect to directory because it does not exist
FROM SERVER: !Changed working dir to <user directory>/A1_working/Server/files/moreFiles
FROM SERVER: -Not deleted because there is no such file in the directory
FROM SERVER: +deleteMe.txt deleted
FROM SERVER: !Changed working dir to <user directory>/A1_working/Server/files/moreFiles/..
FROM SERVER: +<user directory>/A1_working/Server/files/moreFiles/..
.DS_Store | Last Modified: 1630360453013 | Size: 6148 bytes 
moreFiles | Last Modified: 1630360489129 | Size: 160 bytes 
test.txt | Last Modified: 1629501605971 | Size: 10 bytes 
renameMe.jpg | Last Modified: 1629448441139 | Size: 50300 bytes 
retrieveMe.png | Last Modified: 1630355353967 | Size: 29150 bytes 

FROM SERVER: -Can't find nosuchFile
FROM SERVER: -File wasn't renamed because you have not specified the file to rename. use NAME <file-name>
FROM SERVER: +File exists. Send TOBE <new-name>
FROM SERVER: +renameMe.jpg renamed to newName.jpg

FROM SERVER: -File doesn't exist
FROM SERVER: -no RETR started. Nothing to be done
FROM SERVER: 29150
FROM SERVER: +File sent

FROM SERVER: +File does not exist, will create a new file
SIZE 23
FROM SERVER: +ok, waiting for file
FROM SERVER: +Saved
FROM SERVER: +Will append to file
SIZE 23
FROM SERVER: +ok, waiting for file
FROM SERVER: +Saved

FROM SERVER: +Closing Connection. Goodbye 
Total of 29196 bytes sent

````

The purpose of this test is to demonstrate every command within one session. Some invalid arguments
arguments are passed to demonstrate error handling. While the output terminal will not display the
input commands, the test simulates the interaction as follows. In the code snippet below, input
commands have been inserted where they would be sent in the test.

````
******** RUNNING TESTS NOW ********

Welcome to the server
USER invalidUser
FROM SERVER: -Invalid user-id, try again
USER userOne
FROM SERVER: +User-id valid, send account and password
ACCT invalidAcc
FROM SERVER: -Invalid account, try again
ACCT accOne
FROM SERVER: +Account valid, send password
PASS invalidPass
FROM SERVER: -Wrong password, try again
PASS passOne
FROM SERVER: !Logged in as userOne

TYPE Z
FROM SERVER: -Type not valid
TYPE A
FROM SERVER: +Using Ascii mode
TYPE C
FROM SERVER: +Using Continuous mode
TYPE B
FROM SERVER: +Using Binary mode

LIST F
FROM SERVER: +<user directory>/A1_working/Server/files
.DS_Store
moreFiles
test.txt
renameMe.jpg
retrieveMe.png

CDIR hello
FROM SERVER: -Can't connect to directory because it does not exist

CDIR moreFiles
FROM SERVER: !Changed working dir to <user directory>/A1_working/Server/files/moreFiles

KILL noSuchFile
FROM SERVER: -Not deleted because there is no such file in the directory

KILL deleteMe.txt
FROM SERVER: +deleteMe.txt deleted

CDIR ..
FROM SERVER: !Changed working dir to <user directory>/A1_working/Server/files/moreFiles/..

LIST V
FROM SERVER: +<user directory>/A1_working/Server/files/moreFiles/..
.DS_Store | Last Modified: 1630360453013 | Size: 6148 bytes 
moreFiles | Last Modified: 1630360489129 | Size: 160 bytes 
test.txt | Last Modified: 1629501605971 | Size: 10 bytes 
renameMe.jpg | Last Modified: 1629448441139 | Size: 50300 bytes 
retrieveMe.png | Last Modified: 1630355353967 | Size: 29150 bytes 

NAME nosuchFile
FROM SERVER: -Can't find nosuchFile

TOBE newName.jpg
FROM SERVER: -File wasn't renamed because you have not specified the file to rename. use NAME <file-name>

NAME renameMe.jpg
FROM SERVER: +File exists. Send TOBE <new-name>

TOBE newName.jpg
FROM SERVER: +renameMe.jpg renamed to newName.jpg

RETR noSuchFile
FROM SERVER: -File doesn't exist

STOP
FROM SERVER: -no RETR started. Nothing to be done

RETR retrieveMe.png
FROM SERVER: 29150

SEND
FROM SERVER: +File sent

STOR NEW storMe.txt
FROM SERVER: +File does not exist, will create a new file
SIZE 23
FROM SERVER: +ok, waiting for file
FROM SERVER: +Saved

STOR APP storMe.txt
FROM SERVER: +Will append to file
SIZE 23
FROM SERVER: +ok, waiting for file
FROM SERVER: +Saved

DONE
FROM SERVER: +Closing Connection. Goodbye 
Total of 29196 bytes sent

````

#### Changes to file system following testOne

*testOne* uses the demonstrates various file transfer commands. The changes that can be expected in order of execution are as follows:

````KILL deleteMe.txt````

This command will cause the *Server/files/moreFiles/deleteMe.txt* file to be deleted.

````NAME renameMe.jpg````

````TOBE newName.jpg````

These commands will cause the *Server/files/renameMe.jpg* to be renamed to *Server/files/newName.jpg*

````RETR retrieveMe.png````

````SEND````

These commands will result in a file to appear at *client/files/retrieveMe.jpg*. This has been retrieved from *Server/files/retrieveMe.jpg*.

````STOR NEW storMe.txt````

````SIZE 23````

These commands will transfer a text file from *client/files/storMe.txt* to *Server/files/storMe.txt*.
 There is no current files with the same name therefore a new file will be created.
The contents of the file can be seen as ````STORE ME THEN APPEND ME````

````STOR APP storMe.txt````

````SIZE 23````

These commands will append the new incoming text file onto the current one that was
previously stored. This can be certified by checking the text within the file. After these commands the text will
become ````STORE ME THEN APPEND MESTORE ME THEN APPEND ME````. Demonstrating one file be appended to another.












