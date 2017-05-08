package data;
public class DoubleWrapper<Arg1, Arg2> {
    private Arg1 arg1;
    private Arg2 arg2;
    
    public DoubleWrapper(Arg1 arg1, Arg2 arg2) {
        this.arg1 = arg1;
        this.arg2 = arg2;
    }
    
    public Arg1 getArg1() {
        return arg1;
    }
    
    public Arg2 getArg2() {
        return arg2;
    }
}
