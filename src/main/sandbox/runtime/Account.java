package sandbox.runtime;

import sandbox.lists.NativeWhiteList;
import sandbox.runtime.Resource;

import java.util.Random;


/**
 * Class that tracks the amount of memory available to a particular execution
 * run. The instances themselves are private, and are accessed/modified through
 * static methods which talk to a ThreadLocal stack.
 */
public class Account {

    final public Resource memory;
    final public Resource bytecodes;
    final public NativeWhiteList nativeWhitelist;

    final public Account parent;
    final private Object key;

    private final static ThreadLocal<Account> current = new ThreadLocal<Account>();
    static {
        current.set(new Account());
    };



    private Account(){
        memory = new sandbox.runtime.Resource("Memory");
        bytecodes = new Resource("Bytecodes");
        nativeWhitelist = NativeWhiteList.defaultList;
        this.parent = null;
        this.key = new Object();
    }

    private Account(long maxMemory,
                    long maxBytecodes,
                    String[] nativeWhiteList,
                    Account parent){

        memory = parent.memory.fork(maxMemory);
        bytecodes = parent.bytecodes.fork(maxBytecodes);
        System.out.println("G " + nativeWhiteList);
        nativeWhitelist = parent.nativeWhitelist.fork(nativeWhiteList);
        this.parent = parent;
        this.key = new Object();
    }

    public Object pushMem(long subMaxMemory) throws Exception{
        return push(subMaxMemory, this.memory.max - this.memory.current, null);
    }
    public Object pushBytes(long subMaxBytecodes) throws Exception{
        return push(this.bytecodes.max - this.bytecodes.current, subMaxBytecodes, null);
    }
    public Object push(long subMaxMemory, long subMaxBytecodes, String[] nativeWhiteList){
        System.out.println("Pushing " + this);

        Account newAccount = new Account(subMaxMemory, subMaxBytecodes, nativeWhiteList, this);
        current.set(newAccount);

        System.out.println("Pushed " + newAccount);
        return newAccount.key;
    }

    public void pop(Object possibleKey) throws Exception{

        if (possibleKey == key){
            System.out.println("Popping From " + this);

            parent.memory.join(memory);
            parent.bytecodes.join(bytecodes);

            current.set(this.parent);
            System.out.println("To " + parent);
        }
    }


    public static Account get(){
        return current.get();
    }

    public String toString(){
        return "Account " + key + "\n" + memory + "\n" + bytecodes + "\n" + nativeWhitelist;
    }

}
