package pd_trabalhopratico;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GetRemoteClienteInfService extends UnicastRemoteObject implements GetRemoteClienteInfServiceInterface{

    public static final String SERVICE_NAME = "GetInformacaoServidor";
    ArrayList <InformacaodoClientenoServico> clientes;
    ArrayList <InformacaoFicheiro> ListaFicheiros;
    ArrayList<String> repositorios;
    ArrayList<GetRemoteClienteInfObserverInterface> observers;
    public GetRemoteClienteInfService() throws RemoteException 
    {
        repositorios= new ArrayList<>();
        clientes = new ArrayList<>();
        ListaFicheiros = new ArrayList<>();
        observers = new ArrayList<>();
    }
    

    @Override
    public synchronized void notifyObservers(String msg) throws RemoteException {
        for(int i=0; i< observers.size();i++)
        {
            try{       
                observers.get(i).notifyNewOperationConcluded(msg);
            }catch(RemoteException e){
                observers.remove(i--);
                System.out.println("- um observador (observador inacessivel).");
            }
        }
        return;
    }
    @Override
    public synchronized void addObserver(GetRemoteClienteInfObserverInterface obs) throws RemoteException {
        if(!observers.contains(obs))
            observers.add(obs);
        return;
    }

    @Override
    public synchronized void removeObserver(GetRemoteClienteInfObserverInterface observer) throws RemoteException {
        if(observers.contains(observer))
            observers.remove(observer);
        return;
    }
    

    @Override
    public void removeRepositorio(InetAddress add, int port) throws RemoteException {
    String aux1 = new String("****************Lista de Repositótios*************\n");
       String adds = ""+add;
       String []ajuda = adds.split("/");
       adds = "/"+ajuda[0];
        for(int i = 0; i< repositorios.size();i++)
        {
               String [] aux = repositorios.get(i).split(" ");
               String [] add_ip = aux[1].split(":");
               if(add_ip[0].equalsIgnoreCase(adds))
               {
                   if(Integer.parseInt(add_ip[1]) == port)
                   {
                       repositorios.remove(i);
                   }
               }
        }
        for(int i =0 ; i< repositorios.size();i++)
        {
            aux1+=repositorios.get(i)+"\n";
        }
        aux1+="**********************************************\n";
        notifyObservers(aux1);
    }
    @Override
    public void SetRepositorio(InetAddress add, int port) throws RemoteException {
        String aux1 = new String("****************Lista de Repositótios*************\n");
        String aux ="Repositório "+add+":"+port;
        repositorios.add(aux);
        for(int i =0 ; i< repositorios.size();i++)
        {
            aux1+=repositorios.get(i)+"/n";
        }
        aux1+="**********************************************\n";
        notifyObservers(aux1);
    }
    @Override
    public void reMoveCliente(String nome, int port) throws RemoteException{
        String aux = new String("****************Lista de Utilizadores*************\n");
        for(int i = 0; i< clientes.size();i++)
        {
            if(clientes.get(i).getNome().equalsIgnoreCase(nome))
            {
                if(clientes.get(i).getPort() == port)
                {
                    clientes.remove(i);
                }
            }
        }
        for(int x = 0 ; x< clientes.size();x++)
        {
           aux+=clientes.get(x)+"\n";
        }
        aux+="**********************************************\n";
        notifyObservers(aux);
    }
    @Override
    
    public void DeleteListaFicheiros(File[]lista) throws RemoteException
    {
        for(int i = 0; i< lista.length; i++)
        {
            for(int x =0; x < ListaFicheiros.size();x++)
                {
                    if(this.ListaFicheiros.get(x).getNome().equalsIgnoreCase(lista[i].getName()))
                    {
                        this.ListaFicheiros.get(x).setN_replicas(this.ListaFicheiros.get(x).getN_replicas()-1);
                        this.ListaFicheiros.get(x).DeleteCaminho(lista[i].getPath());
                    }
                }
        }
        for(int x =0; x < ListaFicheiros.size();x++)
        {
            if(this.ListaFicheiros.get(x).getN_replicas() == 0)
            {
                this.ListaFicheiros.remove(x);
                x=0;
            };
        }
        String aux = new String("****************Lista de Ficheiros*************\n");
        for(int i = 0; i < ListaFicheiros.size();i++)
        {
            aux+= ListaFicheiros.get(i)+"\n";
        }
        aux+="**********************************************\n";
        notifyObservers(aux);
    }
    
    @Override
    public void SetListaFicheiros(File[] lista) 
    {
        boolean jaexiste=false;
        for(int i = 0; i< lista.length; i++)
        {
            try {
                BasicFileAttributes attr = Files.readAttributes(lista[i].toPath(), BasicFileAttributes.class);
                for(int x =0; x < ListaFicheiros.size();x++)
                {
                    if(this.ListaFicheiros.get(x).getNome().equalsIgnoreCase(lista[i].getName()))
                    {
                        if(this.ListaFicheiros.get(x).jaExisteCaminho(lista[i].getPath()))
                        {
                            jaexiste = true;
                        }
                        else
                        {
                            this.ListaFicheiros.get(x).setN_replicas(this.ListaFicheiros.get(x).getN_replicas()+1);
                            this.ListaFicheiros.get(x).setCaminhos(lista[i].getPath());
                            jaexiste = true;
                        }
                    }
                }
                if(!jaexiste)
                {
                    ListaFicheiros.add(new InformacaoFicheiro(lista[i].getName(),lista[i].getPath()," "+attr.size()/1024+" Kb",0,attr.lastModifiedTime().toString(), attr.creationTime().toString()));
                }
                else
                    jaexiste=false;
            
            } catch (IOException ex) {
                Logger.getLogger(GetRemoteClienteInfService.class.getName()).log(Level.SEVERE, null, ex);
            } 
        }
        String aux = new String("****************Lista de Ficheiros*************\n");
        for(int i = 0; i < ListaFicheiros.size();i++)
        {
            aux+= ListaFicheiros.get(i)+"\n";
        }
        aux+="**********************************************\n";
        try {
            notifyObservers(aux);
        } catch (RemoteException ex) {
            Logger.getLogger(GetRemoteClienteInfService.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    @Override
    public void SetCliente(int estado, String nome, String pass, InetAddress Adress, int port, String filename) throws RemoteException 
    {
        String aux = new String("****************Lista de Utilizadores*************\n");
       if(clientes.size() == 0)
       {
           clientes.add(new InformacaodoClientenoServico(nome, pass, filename, estado, port, Adress));
           aux+=clientes.get(0)+"\n";
           aux+="**********************************************\n";
            notifyObservers(aux);
           return;
       }
       for(int i = 0; i< clientes.size();i++)
       {
           if(clientes.get(i).getNome().equalsIgnoreCase(nome))
           {
               if(clientes.get(i).getPort() == port)
               {
                    clientes.get(i).setEstado(estado);
                    clientes.get(i).setFilename(filename);
                    for(int x = 0 ; x< clientes.size();x++)
                    {
                         aux+=clientes.get(x)+"\n";
                    }
                    aux+="**********************************************\n";
                    notifyObservers(aux);
                    return;
               }
           }
       }
       clientes.add(new InformacaodoClientenoServico(nome, pass, filename, estado, port, Adress));
       for(int x = 0 ; x< clientes.size();x++)
        {
           aux+=clientes.get(x)+"\n";
        }
        aux+="**********************************************\n";
            notifyObservers(aux);
        return;
    }
    
    static public void main(String []args)
    {
        try{
            
            Registry r;
            
            try{
                
                System.out.println("Tentativa de lancamento do registry no porto " + Registry.REGISTRY_PORT + "...");
                
                r = LocateRegistry.createRegistry(Registry.REGISTRY_PORT);
                
                System.out.println("Registry lancado!");
                                
            }catch(RemoteException e){
                System.out.println("Registry provavelmente ja' em execucao!");
                r = LocateRegistry.getRegistry();          
            }
            
            /*
             * Cria o servico
             */            
            GetRemoteClienteInfService informacaodoservidor = new GetRemoteClienteInfService();
            
            System.out.println("Servico GetRemoteFile RemoteTime criado e em execucao ("+informacaodoservidor.getRef().remoteToString()+"...");
            
            /*
             * Regista o servico no rmiregistry local para que os clientes possam localiza'-lo, ou seja,
             * obter a sua referencia remota (endereco IP, porto de escuta, etc.).
             */
            
            r.bind(SERVICE_NAME, informacaodoservidor);     
                   
            System.out.println("Servico " + SERVICE_NAME + " registado no registry...");
            
            /*
             * Para terminar um servico RMI do tipo UnicastRemoteObject:
             * 
             *  UnicastRemoteObject.unexportObject(timeService, true);
             */
            
        }catch(RemoteException e){
            System.out.println("Erro remoto - " + e);
            System.exit(1);
        }catch(Exception e){
            System.out.println("Erro - " + e);
            System.exit(1);
        }                
    }

    
}
