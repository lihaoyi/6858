package sandbox.runtime;

import sandbox.runtime.Resource;
import sandbox.classloader.MyClassLoader;

import java.util.Random;


/**
 * Class that tracks the amount of memory available to a particular execution
 * run. The instances themselves are private, and are accessed/modified through
 * static methods which talk to a ThreadLocal stack.
 */
public class Account {

    final public Resource memory;
    final public Resource instructions;

    final public Account parent;
    final private Object key;

    // TODO(TFK): This should go into a collection of shared global state.
    public static MyClassLoader bcl;

    private final static ThreadLocal<Account> current = new ThreadLocal<Account>();
    static {
        current.set(new Account());
    };



    private Account(){
        memory = new sandbox.runtime.Resource("Memory");
        instructions = new Resource("Instructions");
        this.parent = null;
        this.key = new Object();
    }

    private Account(long maxMemory,
                    long maxInstructions,

                    Account parent){

        memory = parent.memory.fork(maxMemory);
        instructions = parent.instructions.fork(maxInstructions);

        this.parent = parent;
        this.key = new Object();
    }

    public Object pushMem(long subMaxMemory) throws Exception{
        return push(subMaxMemory, this.memory.max - this.memory.current);
    }

    public Object pushBytes(long subMaxInstructions) throws Exception{
        return push(this.instructions.max - this.instructions.current, subMaxInstructions);
    }

    public Object push(long subMaxMemory, long subMaxInstructions){
        System.out.println("Pushing " + this);

        Account newAccount = new Account(subMaxMemory, subMaxInstructions, this);
        current.set(newAccount);

        System.out.println("Pushed " + newAccount);
        return newAccount.key;
    }

    public void pop(Object possibleKey) throws Exception{

        if (possibleKey == key){
            System.out.println("Popping From " + this);

            parent.memory.join(memory);
            parent.instructions.join(instructions);

            current.set(this.parent);
            System.out.println("To " + parent);
        }
    }

    public static Account get(){
        return current.get();
    }

    public String toString(){
        return "Account " + key + "\n" + memory + "\n" + instructions;
    }

}
