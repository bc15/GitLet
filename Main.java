package gitlet;

import java.io.File;

import java.util.Arrays;

/** Driver class for Gitlet, the tiny stupid version-control system.
 *  @author Bond Chaiprasit
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND> ....
     *  java gitlet.Main (command: add, commit, log, */
    /** The path to the git. */
    private static File git = Utils.join
            (new File(System.getProperty("user.dir")),
            "theGit");

    /** The main method.
     * @param args the args. */
    public static void main(String... args) {


        GitletRepo g = new GitletRepo();
        if (git.exists()) {
            g = Utils.readObject(git, GitletRepo.class);
        }
        if (args.length == 0) {
            System.out.println("Please enter a command.");
        } else if (args[0].equals("add")) {

            g.add(args[1]);

        } else if (args[0].equals("commit")) {

            g.commit(args);

        } else if (args[0].equals("log")) {
            g.log();

        } else if (args[0].equals("init")) {


            g.init();
        } else if (args[0].equals("status")) {

            g.status();
        } else if (args[0].equals("rm")) {
            g.remove(args[1]);

        } else if (args[0].equals("global-log")) {
            g.globalLog();

        } else if (args[0].equals("find")) {

            g.find(args[1]);
        } else {
            mainHelper(g, args);
        }


    }
    /** Main helper.
     * @param g the gitlet.
     * @param args the args. */
    public static void mainHelper(GitletRepo g, String... args) {
        if (args[0].equals("checkout")) {
            String[] com = Arrays.copyOfRange(args, 1, args.length);
            g.checkout(com);

        } else if (args[0].equals("branch")) {
            g.branch(args[1]);

        } else if (args[0].equals("rm-branch")) {
            g.rmBranch(args[1]);

        } else if (args[0].equals("reset")) {
            g.reset(args[1]);
        } else if (args[0].equals("merge")) {

            g.merge(args[1]);
        } else {
            System.out.println("No command with that name exists.");
            return;
        }

    }



}
