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
    final private long key;

    private final static ThreadLocal<Account> current = new ThreadLocal<Account>(){
        public Account initialValue(){
            return new Account();
        }
    };

    private static final Random rng = new Random();

    private Account(){
        memory = new sandbox.runtime.Resource("Memory");
        bytecodes = new sandbox.runtime.Resource("Bytecodes");
        this.parent = null;
        this.key = rng.nextLong();
    }

    private Account(long maxMemory,
                    long maxBytecodes,
                    Account parent){

        memory = parent.memory.fork(maxMemory);
        bytecodes = parent.bytecodes.fork(maxBytecodes);

        this.parent = parent;
        this.key = rng.nextLong();
    }

    public long pushMem(long subMaxMemory) throws Exception{
        return push(subMaxMemory, this.memory.max - this.memory.current);
    }
    public long pushBytes(long subMaxBytecodes) throws Exception{
        return push(this.bytecodes.max - this.bytecodes.current, subMaxBytecodes);
    }
    public long push(long subMaxMemory, long subMaxBytecodes){
        System.out.println("Pushing " + this);

        Account newAccount = new Account(subMaxMemory, subMaxBytecodes, this);
        current.set(newAccount);

        System.out.println("Pushed " + newAccount);
        return newAccount.key;
    }


    public void pop(long possibleKey) throws Exception{
        
        if (possibleKey == Account.get().key){
            System.out.println("Popping From " + this);

            parent.memory.join(memory);
            parent.bytecodes.join(bytecodes);

            current.set(this.parent);
            System.out.println("To " + parent);
        }
    }

    static boolean b = false;
    public static Account get(){
        if (b) return null;
        else b = true;
        Account A = current.get();
        b = false;
        return A;

    }

    public String toString(){
        return "Account " + key + "\n" + memory + "\n" + bytecodes;
    }

}
