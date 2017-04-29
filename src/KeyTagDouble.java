import org.jaudiotagger.tag.FieldKey;

public class KeyTagDouble {
    private FieldKey key;
    private String tag;
    
    public KeyTagDouble(FieldKey key, String tag) {
        this.key = key;
        this.tag = tag;
    }
    
    public String getTag() {
        return tag;
    }
    
    public FieldKey getKey() {
        return key;
    }
}
