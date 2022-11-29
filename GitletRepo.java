package gitlet;

import java.io.File;
import java.io.Serializable;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import java.util.LinkedHashMap;

/** Gitlet Constructor.
 * @author Bond Chaiprasit*/
public class GitletRepo implements Serializable {
    /** The stage of this repo. */
    private Stage addRemove;
    /** Has this repo already merged. */
    private boolean merged = false;

    /** Array of commits. */
    private ArrayList<Commit> commits;

    /** Path to current directory. */
    private File cwd;
    /** Path to commit. */
    private File commit;
    /** Path to blob. */
    private File blob;
    /** Path to stage. */
    private File stage;
    /** Path to branch. */
    private File branch;
    /** Head. */
    private String head;
    /** Path to gitLit. */
    private File gitLit;
    /** Keeps track of the active branch. */
    private String active = "master";
    /** Where merge from. */
    private String mergeFrom;
    /** Merge to. */
    private String mergeTo;

    /** Gitlet constructor. */
    public GitletRepo() {
        commits = new ArrayList<>();
        cwd = new File(System.getProperty("user.dir"));

        head = null;
    }
    /** Get the head.
     * @return String */
    public String getHead() {
        return head;
    }

    /** Init command. */
    public void init() {

        gitLit = Utils.join(cwd, ".gitlet");
        if (gitLit.exists()) {
            System.out.println("A Gitlet version-control system "
                    + "already exists in the current directory.");
            return;
        }
        gitLit.mkdir();

        commit = Utils.join(gitLit, "commits");
        blob = Utils.join(gitLit, "blob");
        stage = Utils.join(gitLit, "stage");
        branch = Utils.join(gitLit, "branch");


        cwd.mkdir();
        gitLit.mkdir();
        commit.mkdir();
        blob.mkdir();
        stage.mkdir();
        branch.mkdir();

        Date first = new Date(0);
        Commit initial = new Commit("initial commit",
                null, null,  new LinkedHashMap<>(), first);
        File c = Utils.join(commit, initial.getSha1());
        Utils.writeObject(c,  initial);
        addRemove = new Stage();
        File s = Utils.join(stage,  Utils.sha1(Utils.serialize(addRemove)));
        Utils.writeObject(s, addRemove);
        commits.add(initial);
        File b = Utils.join(branch, active);
        Utils.writeContents(b, initial.getSha1());
        head = initial.getSha1();

        File n = Utils.join(cwd, "theGit");
        Utils.writeObject(n, this);


    }

    /** Commit command.
     * @param args  */
    public void commit(String... args) {

        if (args.length == 1) {
            System.out.println("Please enter a commit message.");
            return;
        }
        String message = args[1];
        if (addRemove.getFilesToAdd().isEmpty()
                && addRemove.getFilesToRem().isEmpty()) {
            System.out.println("No changes added to the commit.");
            return;
        }
        if (message.isBlank()) {
            System.out.println("Please enter a commit message.");
            return;
        }
        Commit current = getCommit(head);

        LinkedHashMap<String, String> cloned = new LinkedHashMap<>();
        for (String key: current.getBlobs().keySet()) {
            String value = current.getBlobs().get(key);
            cloned.put(key, value);

        }
        Commit newC = new Commit(message, head, null,
                cloned, new Date(System.currentTimeMillis()));

        for (String file: addRemove.getFilesToAdd().keySet()) {
            newC.getBlobs().put(file, addRemove.getFilesToAdd().get(file));
        }
        for (String file: addRemove.getFilesToRem()) {
            newC.getBlobs().remove(file);
        }


        File c = Utils.join(commit, newC.getSha1());
        Utils.writeObject(c, newC);
        commits.add(newC);
        addRemove.clear();
        File s = Utils.join(stage, Utils.sha1(Utils.serialize(addRemove)));
        Utils.writeObject(s,  addRemove);
        File b = Utils.join(branch, active);
        Utils.writeContents(b, newC.getSha1());
        head = newC.getSha1();


        File n = Utils.join(cwd, "theGit");
        Utils.writeObject(n, this);

    }

    /** Get the String of the head and then check all commits to see
     * which commit object have same sha1 and return that object.
     * @return Commit
     * @param com the ID.
     */
    public Commit getCommit(String com) {
        File c = Utils.join(commit, com);
        if (!c.exists()) {
            return null;
        }
        return Utils.readObject(c, Commit.class);
    }


    /** Add command.
     * @param fileToAdd */
    public void add(String fileToAdd) {
        File tAdd = Utils.join(cwd, fileToAdd);

        if (!tAdd.exists()) {
            System.out.println("File does not exist.");
            return;
        }
        String hash = Utils.sha1(Utils.readContents(tAdd));
        Commit current = getCommit(head);



        String prev = current.getBlobs().get(fileToAdd);

        if (prev == null || !hash.equals(prev)) {
            addRemove.getFilesToAdd().put(fileToAdd, hash);
            File s = Utils.join(stage,  Utils.sha1(Utils.serialize(addRemove)));
            Utils.writeObject(s, addRemove);
            File n = new File(".gitlet/blob/" + hash + ".txt");
            Utils.writeContents(n, Utils.readContentsAsString(tAdd));
        } else {
            addRemove.getFilesToAdd().remove(fileToAdd);
            addRemove.getFilesToRem().remove(fileToAdd);
            File s = Utils.join(stage,  Utils.sha1(Utils.serialize(addRemove)));
            Utils.writeObject(s, addRemove);


        }

        File n = Utils.join(cwd, "theGit");
        Utils.writeObject(n, this);

    }

    /** Remove command.
     * @param fileName */
    public void remove(String fileName) {
        Commit current = getCommit(head);
        if (!addRemove.getFilesToAdd().keySet().contains(fileName)
                && !current.getBlobs().keySet().contains(fileName)) {
            System.out.println("No reason to remove the file");
            return;
        }
        addRemove.getFilesToAdd().remove(fileName);

        File inWD = Utils.join(cwd, fileName);
        if (current.getBlobs().keySet().contains(fileName)) {
            addRemove.getFilesToRem().add(fileName);
            inWD.delete();
        }
        File n = Utils.join(cwd, "theGit");
        Utils.writeObject(n, this);
    }
    /** Command to shorten ID.
     * @return String */
    public String shortenID(String iD) {

        return iD.substring(0, 8);
    }
    /** Checkout command.
     * @param command */
    public void checkout(String... command) {
        if (command.length == 1) {
            checkoutBranch(command);




        } else if (command.length == 2) {
            String fileName = command[1];
            Commit h = getCommit(head);
            String fileID = h.getBlobs().get(fileName);
            if (fileID == null) {
                System.out.println("File doesn't exist in head commit.");
                return;
            } else {
                File addCwD = Utils.join(cwd, fileName);
                File inCommit = Utils.join(blob, fileID + ".txt");
                Utils.writeContents(addCwD,
                        Utils.readContentsAsString(inCommit));

            }

        } else if (command.length == 3) {

            checkoutCommitIdFile(command);
        }
        File n = Utils.join(cwd, "theGit");
        Utils.writeObject(n, this);

    }
    /** Checkout branch.
     * @param command the command. */
    public void checkoutBranch(String... command) {
        File b = Utils.join(branch, command[0]);
        if (!b.exists()) {
            System.out.println("No such branch exists.");
            return;
        }
        File a = Utils.join(branch, active);
        if (active.equals(command[0])) {
            System.out.println("No need to checkout the current branch.");
            return;
        }
        active = command[0];
        Commit curr = getCommit(head);
        head = Utils.readContentsAsString(b);
        Commit current = getCommit(head);

        File [] wDir = cwd.listFiles();
        for (File w: wDir) {
            if (w.getName().endsWith(".txt")
                    && current.getBlobs().keySet().contains(w.getName())
                    && !curr.getBlobs().keySet().contains(w.getName())) {
                System.out.println("There is an untracked file in the way; "
                        + "delete it, or add and commit it first.");
                return;
            }
        }

        for (File w: wDir) {
            if (!current.getBlobs().keySet().contains(w.getName())
                    && w.getName().endsWith("txt")) {
                File c = Utils.join(cwd, w.getName());
                c.delete();
            }
        }
        for (String fileName: current.getBlobs().keySet()) {
            String fileID = current.getBlobs().get(fileName);

            File addCwD = Utils.join(cwd, fileName);
            File inCommit = new File(".gitlet/blob/" + fileID + ".txt");

            Utils.writeContents(addCwD, Utils.readContentsAsString(inCommit));

        }
    }
    /** Checkout commitid and file.
     * @param command the command. */
    public void checkoutCommitIdFile(String... command) {
        String commitId = command[0];
        File []c = commit.listFiles();
        for (File a: c) {
            String z = a.getName();
            if (shortenID(a.getName()).equals(commitId)) {
                commitId = a.getName();
                break;
            }
        }
        if (!command[1].equals("--")) {
            System.out.println("Incorrect operands.");
            return;
        }
        String fileName = command[2];
        Commit h = getCommit(commitId);
        if (h == null) {
            System.out.println("No commit with that id exists.");
            return;
        }

        String fileID = h.getBlobs().get(fileName);
        if (fileID == null) {
            System.out.println("File does not exist in that commit.");
            return;
        } else {
            File addCwD = Utils.join(cwd, fileName);
            File inCommit = Utils.join(blob, fileID + ".txt");
            Utils.writeContents(addCwD, Utils.readContentsAsString(inCommit));

        }
    }

    /** Log command. */
    public void log() {
        Commit current = getCommit(head);
        while (current != null) {
            System.out.println("===");
            if (current.getMessage().equals("merged")) {
                System.out.println("commit " + current.getSha1());
                String p1 = shortenID(current.getParent());
                String p2 = shortenID(current.getParent2());

                System.out.println("Merge: " + p1.substring(0,
                        p1.length() - 1) + " "
                        + p2.substring(0, p2.length() - 1));
                SimpleDateFormat date = new
                        SimpleDateFormat("EEE MMM d HH:mm:ss yyyy Z");
                String theDate = date.format(current.getDate());
                System.out.println("Date: " + theDate);
                System.out.println("Merged " + mergeFrom
                        + " into " + mergeTo + ".");
                System.out.println();
                current = getCommit(current.getParent());
                continue;

            }
            System.out.println("commit " + current.getSha1());
            SimpleDateFormat date =
                    new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy Z");
            String theDate = date.format(current.getDate());
            System.out.println("Date: " + theDate);
            System.out.println(current.getMessage());

            if (current.getParent() != null) {
                current = getCommit(current.getParent());
                System.out.println();
            } else {
                break;
            }
        }
    }

    /** Find command.
     * @param message the Message. */
    public void find(String message) {
        boolean found = false;
        for (Commit c: commits) {
            if (c.getMessage().equals(message)) {
                System.out.println(c.getSha1());
                found = true;
            }
        }
        if (!found) {
            System.out.println("Found no commit with that message");
        }
        File n = Utils.join(cwd, "theGit");
        Utils.writeObject(n, this);
    }

    /** Global log command. */
    public void globalLog() {
        File[] c = commit.listFiles();
        for (File tCommit: c) {
            Commit theC = Utils.readObject(tCommit, Commit.class);
            System.out.println("===");
            System.out.println("commit " + theC.getSha1());
            SimpleDateFormat date = new
                    SimpleDateFormat("EEE MMM d HH:mm:ss yyyy Z");
            String theDate = date.format(theC.getDate());
            System.out.println("Date: " + theDate);
            System.out.println(theC.getMessage());
        }
    }

    /** Status comamnd. */
    public void status() {
        if (getHead() == null) {
            System.out.println("Not in an initialized Gitlet directory.");
            return;
        }
        System.out.println("=== Branches ===");
        printBranches();

        System.out.println("=== Staged Files ===");
        for (String stageFile: addRemove.getFilesToAdd().keySet()) {
            System.out.println(stageFile);
        }
        System.out.println();
        System.out.println("=== Removed Files ===");
        for (String r: addRemove.getFilesToRem()) {
            System.out.println(r);
        }
        System.out.println();


        System.out.println("=== Modifications Not Staged For Commit ===");

        System.out.println();
        System.out.println("=== Untracked Files ===");

        System.out.println();




    }
    /** Print branches for status. */
    public void printBranches() {
        File [] branches = branch.listFiles();
        for (File b: branches) {
            if (b.getName().equals(active)) {
                System.out.println("*" + active);
            } else {
                System.out.println(b.getName());
            }
        }
        System.out.println();
    }
    /** Branch command.
     *  @param branchName name of Branch.*/
    public void branch(String branchName) {
        File b = Utils.join(branch, branchName);
        if (b.exists()) {
            System.out.println("A branch with that name already exists ");
            return;
        }
        Utils.writeContents(b, head);


    }

    /** Remove branch command.
     * @param branchName name of branch. */
    public void rmBranch(String branchName) {

        File b = Utils.join(branch, branchName);
        if (!b.exists()) {
            System.out.println("A branch with that name does not exist.");
            return;
        }
        File a = Utils.join(branch, active);
        if (branchName.equals(active)) {
            System.out.println("Cannot remove the current branch.");
            return;
        }
        b.delete();



    }
    /** Reset command.
     * @param commitID commit of ID. */
    public void reset(String commitID) {
        File c = Utils.join(commit, commitID);
        if (!c.exists()) {
            System.out.println("No commit with that id exists.");
            return;
        }

        File [] wDir = cwd.listFiles();
        File b = Utils.join(branch, active);
        Commit current = getCommit(Utils.readContentsAsString(b));
        for (File f: wDir) {
            if (!current.getBlobs().keySet().contains(f.getName())
                && f.getName().endsWith("txt")
                    && !addRemove.getFilesToAdd().keySet()
                    .contains(f.getName())) {
                System.out.println("There is an untracked file in the way;"
                        + " delete it, or add and commit it first.");
            }
        }

        Commit check = getCommit(commitID);
        for (File w: wDir) {
            if (!check.getBlobs().keySet().contains((w.getName()))
                    && w.getName().endsWith(".txt")) {
                w.delete();
            }
        }
        for (String fileName: check.getBlobs().keySet()) {
            String fileID = check.getBlobs().get(fileName);

            File addCwD = Utils.join(cwd, fileName);
            File inCommit = new File(".gitlet/blob/" + fileID + ".txt");

            Utils.writeContents(addCwD,
                    Utils.readContentsAsString(inCommit));

        }
        head = check.getSha1();
        File a = Utils.join(branch, active);
        Utils.writeContents(a, head);
        addRemove.clear();
        File n = Utils.join(cwd, "theGit");
        Utils.writeObject(n, this);




    }

    /** Merge command.
     * @param branchName name of Branch. */
    public void merge(String branchName) {
        Commit latestAncestor = getLatestAncestor(branchName);
        File cBranch = Utils.join(branch, branchName);
        File aBranch = Utils.join(branch, active);
        Commit otherBranch = getCommit(Utils.readContentsAsString(cBranch));
        Commit activeBranch = getCommit(Utils.readContentsAsString(aBranch));
        ArrayList<String> allPossible = fileAll(latestAncestor,
                otherBranch, activeBranch);

        for (String fileName: allPossible) {
            String splitPoint = latestAncestor.getBlobs().get(fileName);
            String tActive = activeBranch.getBlobs().get(fileName);
            String tOther = otherBranch.getBlobs().get(fileName);
            if (tActive == null) {
                tActive = "null";
            }
            if (splitPoint == null) {
                splitPoint = "null";
            }
            if (tOther == null) {
                tOther = "null";
            }
            if (!tOther.equals(splitPoint) && tActive.equals(splitPoint)) {
                Commit current = otherBranch;
                File [] wDir = cwd.listFiles();
                if (!current.getBlobs().keySet().contains(fileName)
                        && fileName.endsWith(("txt"))) {
                    File c = Utils.join(cwd, fileName);
                    c.delete();
                    continue;


                }

                String fileID = current.getBlobs().get(fileName);

                File addCwD = Utils.join(cwd, fileName);
                File inCommit = new File(".gitlet/blob/" + fileID + ".txt");

                Utils.writeContents(addCwD,
                        Utils.readContentsAsString(inCommit));



            }
            if (!tActive.equals(splitPoint)
                    && tOther.equals(splitPoint)) {
                continue;
            } else if (!tActive.equals(splitPoint)
                    && !tOther.equals(splitPoint)) {
                bothModified(tActive, tOther, cBranch, fileName);

            }
            cases(splitPoint, tActive, tOther, fileName);
        }

        mergeAfter(branchName, aBranch, cBranch);


    }
    /** Get all the available files.
     * @param latestAncestor LA.
     * @param otherBranch OB.
     * @param activeBranch AB.
     * @return ArrayList<String> */
    public ArrayList<String> fileAll(Commit latestAncestor,
                                     Commit otherBranch, Commit activeBranch) {
        ArrayList<String> allPossible = new ArrayList<>();
        for (String fileName: latestAncestor.getBlobs().keySet()) {
            allPossible.add(fileName);
        }
        for (String fileName: otherBranch.getBlobs().keySet()) {
            if (!allPossible.contains(fileName)) {
                allPossible.add(fileName);
            }
        }
        for (String fileName: activeBranch.getBlobs().keySet()) {
            if (!allPossible.contains(fileName)) {
                allPossible.add(fileName);
            }
        }
        return allPossible;
    }

    /** Helper for merge.
     * @param splitPoint SP.
     * @param tActive TA.
     * @param tOther TO.
     * @param fileName FN. */
    public void cases(String splitPoint, String tActive,
                      String tOther, String fileName) {
        if (splitPoint == null && tActive == null
                && tOther != null) {
            File wD = Utils.join(cwd, fileName);
            File b = Utils.join(blob, tOther + ".txt");
            Utils.writeContents(wD, Utils.readContentsAsString(b));
        } else if (splitPoint == null && tOther == null
                && tActive != null) {
            File wD = Utils.join(cwd, fileName);
            File b = Utils.join(blob, tActive);
            Utils.writeContents(wD, Utils.readContentsAsString(b));
        } else if (splitPoint.equals(tActive)
                && tOther == null) {
            File wD = Utils.join(cwd, fileName);
            wD.delete();
        }

    }

    /** Helper method for merge.
     * @param tActive ta.
     * @param tOther to.
     * @param cBranch cB.
     * @param fileName fn.*/
    public void bothModified(String tActive, String tOther,
                             File cBranch, String fileName) {
        if (tActive.equals(tOther) && tActive != null) {
            String[] a = new String[3];
            a[0] = Utils.readContentsAsString(cBranch);
            a[1] = "--";
            a[2] = fileName;
            checkout(a);
        } else if (tActive.equals(tOther)) {
            File wD = Utils.join(cwd, fileName);
            wD.delete();
        } else {

            File activeT = new File(".gitlet/blob/"
                    + tActive + ".txt");
            String top = Utils.readContentsAsString(activeT);
            File newT = new File(".gitlet/blob/" + tOther
                    + ".txt");
            String bottom = Utils.readContentsAsString(newT);
            String total = "<<<<<<< HEAD\n" + top + "=======\n"
                    + bottom + ">>>>>>>\n";
            File addedCwD = Utils.join(cwd, fileName);
            Utils.writeContents(addedCwD, total);
            System.out.println("Encountered a merge conflict.");
        }
    }
    /** Actions after merge.
     * @param bName name of Branch.
     * @param activeB File of active.
     * @param otherB File of given Branch.*/
    public void mergeAfter(String bName, File activeB, File otherB) {
        mergeFrom = bName;
        mergeTo = active;
        String parent1 = Utils.readContentsAsString(activeB);
        String parent2 = Utils.readContentsAsString(otherB);
        File [] wD = cwd.listFiles();
        LinkedHashMap<String, String> mergeCommit = new LinkedHashMap<>();

        for (File w: wD) {
            if (w.getName().endsWith("txt")) {
                String sha = Utils.sha1(Utils.readContents(w));
                String name = w.getName();
                mergeCommit.put(name.substring(0, name.length() - 3), sha);
            }
        }
        Commit mCommit = new Commit("merged", parent1,
                parent2, mergeCommit, new Date(System.currentTimeMillis()));
        File c = Utils.join(commit, mCommit.getSha1());
        Utils.writeObject(c, mCommit);
        commits.add(mCommit);
        File b = Utils.join(branch, active);
        Utils.writeContents(b, mCommit.getSha1());
        head = mCommit.getSha1();
        File n = Utils.join(cwd, "theGit");
        Utils.writeObject(n, this);


    }
    /** Command to get latest Ancestor.
     * @param branchName the name of Branch.
     * @return Commit*/
    public Commit getLatestAncestor(String branchName) {
        File cBranch = Utils.join(branch, branchName);
        if (!cBranch.exists()) {
            System.out.println("Branch with that"
                    + " name does not exist.");
            return null;
        }
        File aBranch = Utils.join(branch, active);
        Commit givenBranch = getCommit(Utils.readContentsAsString(cBranch));
        Commit activeBranch = getCommit(Utils.readContentsAsString(aBranch));
        Commit latestCommonAncestor = null;
        if (givenBranch.equals(activeBranch)) {
            return givenBranch;

        }
        ArrayList<String> ancestors = new ArrayList<>();
        while (givenBranch.getParent() != null) {
            ancestors.add(givenBranch.getParent());
            givenBranch = getCommit(givenBranch.getParent());
        }

        while (activeBranch.getParent() != null) {
            if (ancestors.contains(activeBranch.getSha1())) {
                latestCommonAncestor = activeBranch;
                break;
            }
            activeBranch = getCommit(activeBranch.getParent());
        }
        return latestCommonAncestor;
    }


}








