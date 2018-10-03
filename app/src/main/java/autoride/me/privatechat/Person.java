package autoride.me.privatechat;

/**
 * Created by goldenreign on 9/30/2018.
 */

class Person {

    private String personName;
    private String roomName;

    public Person(String personName, String roomName) {
        this.personName = personName;
        this.roomName = roomName;
    }

    public String getPersonName() {
        return personName;
    }

    public void setPersonName(String personName) {
        this.personName = personName;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }
}
