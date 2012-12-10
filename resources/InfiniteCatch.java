/**
 * This is an attempt to break out of the sandbox by repeatedly catching the
 * exception that our instrumentation code throws when it runs out of
 * resources to use. The test demonstrates that even if you put your code in a
 * try-catch block, the checks inserted into the bytecodes will re-throw the
 * instruction before the untrusted code can do anything.
 */
public class InfiniteCatch {
    public static String main() throws Exception {
        double x = 10000;
        while(true) {
            x++;
        }
        /*try{
            while(true) x++;
        }catch(Exception a){
            try{
                while(true) x++;
            }catch(Exception b){
                try{
                    while(true) x++;
                }catch(Exception c){
                    try{
                        while(true) x++;
                    }catch(Exception d){
                        try{
                            while(true) x++;
                        }catch(Exception e){
                            try{
                                while(true) x++;
                            }catch(Exception f){
                                return "haha i caught you exception and ran my catch";
                            }finally{
                                return "haha i caught you exception and ran my finally";
                            }
                        }finally{
                            return "haha i caught you exception and ran my finally";
                        }
                    }finally{
                        return "haha i caught you exception and ran my finally";
                    }
                }finally{
                    return "haha i caught you exception and ran my finally";
                }
            }finally{
                return "haha i caught you exception and ran my finally";
            }
        }finally{
            return "haha i caught you exception and ran my finally";
        }*/


    }
}
