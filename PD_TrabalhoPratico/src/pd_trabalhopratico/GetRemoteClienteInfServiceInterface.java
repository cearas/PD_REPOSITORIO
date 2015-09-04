package pd_trabalhopratico;

import java.io.File;
import java.net.InetAddress;

public interface GetRemoteClienteInfServiceInterface extends java.rmi.Remote
{
    public void SetCliente(int estado, String nome, String pass, InetAddress Adress, int port, String filename)throws java.rmi.RemoteException;;
    public void SetListaFicheiros(File[]lista)throws java.rmi.RemoteException;;
    public void DeleteListaFicheiros(File[]lista)throws java.rmi.RemoteException;;
    public void reMoveCliente(String nome,int port)throws java.rmi.RemoteException;;
    public void SetRepositorio(InetAddress add, int port)throws java.rmi.RemoteException;;
    public void removeRepositorio(InetAddress add, int port)throws java.rmi.RemoteException;;
    public void addObserver(GetRemoteClienteInfObserverInterface obs) throws java.rmi.RemoteException;
    public void removeObserver(GetRemoteClienteInfObserverInterface observer) throws java.rmi.RemoteException; 
    public void notifyObservers(String msg) throws java.rmi.RemoteException; 
}
