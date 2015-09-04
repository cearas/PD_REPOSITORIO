package pd_trabalhopratico;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface GetRemoteClienteInfObserverInterface extends Remote{
    public void notifyNewOperationConcluded(String description) throws RemoteException;
}
