import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;


public class InfiniteCatch {
    public static String main() throws Exception {
        int x = 0;
        try{
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
                                return "haha i caught you exception";
                            }
                        }
                    }
                }
            }
        }


    }
}
