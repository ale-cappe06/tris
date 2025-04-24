package model;

public class UserStats {
    private int vittorie;
    private int sconfitte;
    private int pareggi;

    public UserStats(int vittorie, int sconfitte, int pareggi) {
        this.vittorie = vittorie;
        this.sconfitte = sconfitte;
        this.pareggi = pareggi;
    }

    public int getVittorie() {
        return vittorie;
    }

    public int getSconfitte() {
        return sconfitte;
    }

    public int getPareggi() {
        return pareggi;
    }
}
