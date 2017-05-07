public class DoubleWrapper<Arg1, Arg2> {
    private Arg1 key;
    private Arg2 tag;
    
    public DoubleWrapper(Arg1 key, Arg2 tag) {
        this.key = key;
        this.tag = tag;
    }
    
    public Arg2 getTag() {
        return tag;
    }
    
    public Arg1 getKey() {
        return key;
    }
}
