package timetablegenerator;


public class Time {
    private int day;
    private int session;

    public Time(int day, int session) {
        this.day = day;
        this.session = session;
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public int getSession() {
        return session;
    }

    public void setSession(int session) {
        this.session = session;
    }
    
    
}
