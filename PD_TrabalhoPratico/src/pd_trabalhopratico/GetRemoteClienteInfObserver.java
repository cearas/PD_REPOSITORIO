package pd_trabalhopratico;

import java.io.File;
import java.net.InetAddress;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class GetRemoteClienteInfObserver extends UnicastRemoteObject implements GetRemoteClienteInfObserverInterface{

    public GetRemoteClienteInfObserver() throws RemoteException {};
    public void notifyNewOperationConcluded(String description) throws RemoteException {
        System.out.println(description);
    }
    
    static public void main(String args[])
    {
         try{
            //Cria e lanca o servico 
            GetRemoteClienteInfObserver observer = new GetRemoteClienteInfObserver();
            System.out.println("Servico GetRemoteFileObserver criado e em execucao...");
            
            //Localiza o servico remoto nomeado "GetRemoteFile"
            String objectUrl = "rmi://127.0.0.1/GetInformacaoServidor"; //rmiregistry on localhost
            
            if(args.length > 0)
                objectUrl = "rmi://" + args[0] + "/GetInformacaoServidor"; 
                            
            GetRemoteClienteInfServiceInterface getInftoService = (GetRemoteClienteInfServiceInterface)Naming.lookup(objectUrl);
            
            //adiciona observador no servico remoto
            getInftoService.addObserver(observer);
            
            System.out.println("<Enter> para terminar...");
            System.out.println();
            System.in.read();
            
            getInftoService.removeObserver(observer);
            UnicastRemoteObject.unexportObject(observer, true);
            
        }catch(RemoteException e){
            System.out.println("Erro remoto - " + e);
            System.exit(1);
        }catch(Exception e){
            System.out.println("Erro - " + e);
            System.exit(1);
        }  
    }
    
}
