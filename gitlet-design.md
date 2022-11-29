# Gitlet Design Document

**Name**: Bond Chaiprasit

## Classes and Data Structures

###Class Commit
This class contains a reference to a commit 

**Fields**
1. (String) Instance variable ID which represents a unique commit
2. (Timestamp) Instance variable time stamp
3. (String) Instance variable of the message of ommit
4. (String) Instance variable that references the parent
5. (HashMap<String, String) Instance variable that maps the name of the file to its SHA1

### Class Blob
Contains the contents of a file

### Class Gitlet Repo
The actually Gitlet repo, contains all the methods for the commands

**Fields**
1. (Stage) AddRemove: The stage in the gitlet repo, stage contains files to add and remove
2. (String) head: References the head of the commits
3. (ArrayList<Commit>) comits: ArrayList of all the commits
4. (File) cwd: The current working directory. 
5. (File) commit: Directory containing all commits
6. (File) blob: Directory contaning all the blobs



### Main 
This is where you are executing the code. Could have different cases for the main commands. 

## Algorithms

Commit Class
1. Commit(): initialize a commit object with timestamp, message, parent, Hashmap of file name to SHA1, and SHA1 of the commit 
2. getSha1(): returns the SHA1 of the commit
3. getTime: returns the time of the commit 
4. getBlobs: returns the hashmap
5. getMessage: returns the message of the commit

Blob Class
1. Blob(): Read in the data file and store it

GitLetRepo
1. GitletRepo(): Create a new gitlet repo
2. init(): Create a new gitlet repo, consists of a new current working directory, a direcotry for commits, blobs, and stage. Create an inital commit with timestamp 0 and add it to the commit directory. Set the head and master to that commit. Initialize a stage object and write it into the stage diectory. 
3. commit(String message): get the current head commit, create new commit by cloning the head commit and changing message and timestamp. For every file in toAdd staging area, change the hashmap of new commit. Create new path to this commit and write the commit object into it. Clear staging area. Change head and master. 
4. getCommit(String com): List all the files in the commit directory, iterate through files to see if the Sha1 of file matches the sha1 passed in. If matches, return that commit. 
5. add(String fileToAdd): Check if file exists, if yes, get sha1 of file and head commit. get the sha1 of file by checking in head commit hashmap. If the sha1 of fileToAdd is diff, it means file has been updated so add it to the staging area, create a new blob. 

Main Class
1. Main() where you execute the code by calling on functions in this class
2. init: Creates a new log() with one commit, a commit that contains no files and has the commit message "initial commit"
3. add: Adds a copy of the file to the staging area, which is stored in the Log Class, if the file already exists in the staging area, overwrite the file
4. commit: add all the files in the staging area into the commit instance. Staging area is cleared after a commit
5. log: prints out every commit starting from the most recent
6. global-log: prints out every commit ever made even if it is branched out
7. status: display what branches actually exist and the staging area and modified files that have not been staged for commit 
## Persistence
Pieces of data that is needed across multiple calls to Gitlet: ID of commit


