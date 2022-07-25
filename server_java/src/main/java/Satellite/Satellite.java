package Satellite;

public class Satellite {
    private String idToken;
    private String macAddress;
    private String room;
    private String name;
    private boolean inUse;
    
    public Satellite(String idToken, String room, String name, boolean inUse) {
        this.idToken = idToken;
        this.room = room;
        this.name = name;
        this.inUse = inUse;
    }
    
    public String getIdToken() {
        return idToken;
    }
    
    public String getRoom() {
        return room;
    }
    
    public String getName() {
        return name;
    }
    
    public boolean inUse() {
        return inUse;
    }
}