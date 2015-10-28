package seedstars.goethereum.components;

/**
 * Command Object
 */
public class CommandLine {

    private String line;
    private int length;
    private boolean shortcut;

    public CommandLine() {
        this.line = new String();
        this.length = 0;
        this.shortcut=false;
    }

    public String getCommandLine() {
        return this.line;
    }

    public int getCommandLength() {
        return this.length;
    }

    public void setCommandLine(String line) {
        this.line= line;
        this.length= line.length();
    }

    public boolean inShortcut(){
        return shortcut;
    }

    public void setShortcut(Boolean b){
        this.shortcut=b;
    }

    public void addChar(char c) {
        this.line = this.line + c;
        this.length++;
    }

    public void delChar() {
        if (this.length > 0) {
            this.line = this.line.substring(0, this.line.length() - 1);
            this.length--;
        }
    }


}
