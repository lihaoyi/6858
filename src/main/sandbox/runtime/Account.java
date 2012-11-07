package sandbox.runtime;

import sandbox.runtime.Resource;

import java.util.Random;


/**
 * Class that tracks the amount of memory available to a particular execution
 * run. The instances themselves are private, and are accessed/modified through
 * static methods which talk to a ThreadLocal stack.
 */
public class Account {

    final public Resource memory;
    final public sandbox.runtime.Resource bytecodes;

    final public Account parent;
    final private Object key;

    private final static ThreadLocal<Account> current = new ThreadLocal<Account>();
    static {
        current.set(new Account());
    };



    private Account(){
        memory = new sandbox.runtime.Resource("Memory");
        bytecodes = new sandbox.runtime.Resource("Bytecodes");
        this.parent = null;
        this.key = new Object();
    }

    private Account(long maxMemory,
                    long maxBytecodes,
                    Account parent){

        memory = parent.memory.fork(maxMemory);
        bytecodes = parent.bytecodes.fork(maxBytecodes);

        this.parent = parent;
        this.key = new Object();
    }

    public Object pushMem(long subMaxMemory) throws Exception{
        return push(subMaxMemory, this.memory.max - this.memory.current);
    }
    public Object pushBytes(long subMaxBytecodes) throws Exception{
        return push(this.bytecodes.max - this.bytecodes.current, subMaxBytecodes);
    }
    public Object push(long subMaxMemory, long subMaxBytecodes){
        System.out.println("Pushing " + this);

        Account newAccount = new Account(subMaxMemory, subMaxBytecodes, this);
        current.set(newAccount);

        System.out.println("Pushed " + newAccount);
        return newAccount.key;
    }


    public void pop(Object possibleKey) throws Exception{
        
        if (possibleKey == Account.get().key){
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
        return "Account " + key + "\n" + memory + "\n" + bytecodes;
    }

}
