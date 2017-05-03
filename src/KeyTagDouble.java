public class KeyTagDouble<Key> {
    private Key key;
    private String tag;
    
    public KeyTagDouble(Key key, String tag) {
        this.key = key;
        this.tag = tag;
    }
    
    public String getTag() {
        return tag;
    }
    
    public Key getKey() {
        return key;
    }
}
